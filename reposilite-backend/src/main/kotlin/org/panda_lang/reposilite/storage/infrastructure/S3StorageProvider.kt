/*
 * Copyright (c) 2021 dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.panda_lang.reposilite.storage.infrastructure

import io.javalin.http.HttpCode
import io.javalin.http.HttpCode.NOT_FOUND
import net.dzikoysk.dynamiclogger.Journalist
import net.dzikoysk.dynamiclogger.Logger
import org.panda_lang.reposilite.failure.api.ErrorResponse
import org.panda_lang.reposilite.failure.api.errorResponse
import org.panda_lang.reposilite.maven.api.DirectoryInfo
import org.panda_lang.reposilite.maven.api.DocumentInfo
import org.panda_lang.reposilite.maven.api.FileDetails
import org.panda_lang.reposilite.maven.api.FileType.DIRECTORY
import org.panda_lang.reposilite.shared.FilesUtils.getMimeType
import org.panda_lang.reposilite.storage.StorageProvider
import org.panda_lang.reposilite.web.api.MimeTypes.OCTET_STREAM
import org.panda_lang.reposilite.web.api.MimeTypes.PLAIN
import org.panda_lang.reposilite.web.asResult
import panda.std.Result
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.ListObjectsRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.FileTime

internal class S3StorageProvider(
    private val journalist: Journalist,
    private val bucket: String,
    region: String
) : StorageProvider, Journalist {

    private val s3: S3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(AnonymousCredentialsProvider.create())
        .build()

    override fun putFile(file: Path, bytes: ByteArray): Result<FileDetails, ErrorResponse> =
        try {
            val builder = PutObjectRequest.builder()
            builder.bucket(bucket)
            builder.key(file.toString().replace('\\', '/'))
            builder.contentType(getMimeType(file.toString(), PLAIN))
            builder.contentLength(bytes.size.toLong())
            s3.putObject(builder.build(), RequestBody.fromBytes(bytes))

            DocumentInfo(
                file.fileName.toString(),
                getMimeType(file.fileName.toString(), OCTET_STREAM),
                bytes.size.toLong(),
                { getFile(file).get() }
            ).asResult()
        }
        catch (exception: Exception) {
            logger.exception(exception)
            errorResponse(HttpCode.INTERNAL_SERVER_ERROR, "Failed to write $file")
        }

    override fun putFile(file: Path, inputStream: InputStream): Result<FileDetails, ErrorResponse> =
        try {
            val builder = PutObjectRequest.builder()
            builder.bucket(bucket)
            builder.key(file.toString().replace('\\', '/'))
            builder.contentType(getMimeType(file.toString(), PLAIN))

            val length = inputStream.available().toLong()
            builder.contentLength(length)

            s3.putObject(
                builder.build(),
                RequestBody.fromInputStream(inputStream, inputStream.available().toLong())
            )

            DocumentInfo(
                file.fileName.toString(),
                getMimeType(file.fileName.toString(), OCTET_STREAM),
                length,
                { inputStream }
            ).asResult()
        }
        catch (ioException: IOException) {
            logger.exception(ioException)
            errorResponse(HttpCode.INTERNAL_SERVER_ERROR, "Failed to write $file")
        }

    override fun getFile(file: Path): Result<InputStream, ErrorResponse> =
        try {
            val request = GetObjectRequest.builder()
            request.bucket(bucket)
            request.key(file.toString().replace('\\', '/'))

            s3.getObject(request.build()).asResult()
            // val bytes = ByteArray(Math.toIntExact(response.response().contentLength()))
            // response.read(bytes)
        }
        catch (noSuchKeyException: NoSuchKeyException) {
            errorResponse(NOT_FOUND, "File not found: $file")
        }
        catch (ioException: IOException) {
            errorResponse(NOT_FOUND, "File not found: $file")
        }

    override fun getFileDetails(file: Path): Result<FileDetails, ErrorResponse> =
        if (file.toString() == "") { // why ""?
            getFiles(file)
                .map { it.map { path -> getFileDetails(path) } }
                .map { DirectoryInfo(file.fileName.toString(), it.map { result -> result.get() /* We should somehow flat map these errors maybe */}) }
        }
        else {
            head(file)?.let {
                DocumentInfo(
                    file.fileName.toString(),
                    getMimeType(file.fileName.toString(), OCTET_STREAM),
                    it.contentLength(),
                    { getFile(file).get() }
                ).asResult()
            }
            ?: errorResponse(NOT_FOUND, "File not found: $file")
        }

    override fun removeFile(file: Path): Result<*, ErrorResponse> =
        with(DeleteObjectRequest.builder()) {
            bucket(bucket)
            key(file.toString().replace('\\', '/'))
            s3.deleteObject(build())
        }.asResult()

    override fun getFiles(directory: Path): Result<List<Path>, ErrorResponse> =
        try {
            val request = ListObjectsRequest.builder()
            request.bucket(bucket)

            val directoryString = directory.toString().replace('\\', '/')
            request.prefix(directoryString)
            //            request.delimiter("/");

            val response = s3.listObjects(request.build())
            val paths: MutableList<Path> = ArrayList()

            for (content in response.contents()) {
                val sub = content.key().substring(directoryString.length)
                paths.add(Paths.get(sub))
            }

            paths.asResult()
        }
        catch (exception: Exception) {
            errorResponse(HttpCode.INTERNAL_SERVER_ERROR, exception.localizedMessage)
        }

    override fun getLastModifiedTime(file: Path): Result<FileTime, ErrorResponse> =
        head(file)
            ?.let { FileTime.from(it.lastModified()).asResult() }
            ?: getFiles(file)
                .map { files -> files.firstOrNull() }
                .mapErr { ErrorResponse(NOT_FOUND, "File not found: $file") }
                .flatMap { getLastModifiedTime(file.resolve(it!!.getName(0))) }

    override fun getFileSize(file: Path): Result<Long, ErrorResponse> =
        head(file)
            ?.contentLength()
            ?.asResult()
            ?: errorResponse(NOT_FOUND, "File not found: $file")

    private fun head(file: Path): HeadObjectResponse? =
        try {
            val request = HeadObjectRequest.builder()
            request.bucket(bucket)
            request.key(file.toString().replace('\\', '/'))
            s3.headObject(request.build())
        }
        catch (ignored: NoSuchKeyException) {
            // ignored
            null
        }
        catch (exception: Exception) {
            exception.printStackTrace()
            null
        }

    override fun exists(file: Path): Boolean =
        head(file) != null

    override fun isDirectory(file: Path): Boolean =
        getFileDetails(file)
            .map { it.type == DIRECTORY }
            .orElseGet { false }
        /*
        with(getFile(file)) {
            isOk && get().available() > 0 && !exists(file)
        }
         */

    override fun isFull(): Boolean {
        TODO("Not yet implemented")
    }

    override fun usage(): Result<Long, ErrorResponse> {
        TODO("Not yet implemented")
    }

    override fun canHold(contentLength: Long): Result<*, ErrorResponse> =
        true.asResult()

    override fun getLogger(): Logger =
        journalist.logger

}