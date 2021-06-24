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
import org.panda_lang.reposilite.failure.api.ErrorResponse
import org.panda_lang.reposilite.failure.api.errorResponse
import org.panda_lang.reposilite.maven.api.FileDetailsResponse
import org.panda_lang.reposilite.shared.MimeTypes.MIME_OCTET_STREAM
import org.panda_lang.reposilite.shared.MimeTypes.MIME_PLAIN
import org.panda_lang.reposilite.shared.utils.FilesUtils.getMimeType
import org.panda_lang.reposilite.storage.StorageProvider
import org.panda_lang.utilities.commons.function.Result
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
import java.time.LocalDate

internal class S3StorageProvider(private val bucket: String, region: String) : StorageProvider {

    private val s3: S3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(AnonymousCredentialsProvider.create())
        .build()

    override fun putFile(file: Path, bytes: ByteArray): Result<FileDetailsResponse, ErrorResponse> {
        val builder = PutObjectRequest.builder()
        builder.bucket(bucket)
        builder.key(file.toString().replace('\\', '/'))
        builder.contentType(getMimeType(file.toString(), MIME_PLAIN))
        builder.contentLength(bytes.size.toLong())

        return try {
            s3.putObject(
                builder.build(),
                RequestBody.fromBytes(bytes)
            )

            Result.ok(
                FileDetailsResponse(
                    FileDetailsResponse.FILE,
                    file.fileName.toString(),
                    FileDetailsResponse.DATE_FORMAT.format(LocalDate.now()),
                    getMimeType(file.fileName.toString(), MIME_OCTET_STREAM),
                    bytes.size.toLong()
                )
            )
        }
        catch (exception: Exception) {
            exception.printStackTrace()
            errorResponse(HttpCode.INTERNAL_SERVER_ERROR, "Failed to write $file")
        }
    }

    override fun putFile(file: Path, inputStream: InputStream): Result<FileDetailsResponse, ErrorResponse> {
        return try {
            val builder = PutObjectRequest.builder()
            builder.bucket(bucket)
            builder.key(file.toString().replace('\\', '/'))
            builder.contentType(getMimeType(file.toString(), MIME_PLAIN))

            val length = inputStream.available().toLong()
            builder.contentLength(length)

            s3.putObject(
                builder.build(),
                RequestBody.fromInputStream(inputStream, inputStream.available().toLong())
            )

            Result.ok(
                FileDetailsResponse(
                    FileDetailsResponse.FILE,
                    file.fileName.toString(),
                    FileDetailsResponse.DATE_FORMAT.format(LocalDate.now()),
                    getMimeType(file.fileName.toString(), MIME_OCTET_STREAM),
                    length
                )
            )
        }
        catch (ioException: IOException) {
            ioException.printStackTrace()
            errorResponse(HttpCode.INTERNAL_SERVER_ERROR, "Failed to write $file")
        }
    }

    override fun getFile(file: Path): Result<ByteArray, ErrorResponse> {
        return try {
            val request = GetObjectRequest.builder()
            request.bucket(bucket)
            request.key(file.toString().replace('\\', '/'))

            val response = s3.getObject(request.build())
            val bytes = ByteArray(Math.toIntExact(response.response().contentLength()))
            val read = response.read(bytes)
            // TOFIX: verify - read not used?

            Result.ok(bytes)
        }
        catch (noSuchKeyException: NoSuchKeyException) {
            errorResponse(HttpCode.NOT_FOUND, "File not found: $file")
        }
        catch (ioException: IOException) {
            errorResponse(HttpCode.NOT_FOUND, "File not found: $file")
        }
    }

    override fun getFileDetails(file: Path): Result<FileDetailsResponse, ErrorResponse> {
        if (file.toString() == "") {
            return Result.ok(
                FileDetailsResponse(
                    FileDetailsResponse.DIRECTORY,
                    "",
                    "WHATEVER",
                    "application/octet-stream",
                    0
                )
            )
        }

        return head(file)
            ?.let {
                FileDetailsResponse(
                    FileDetailsResponse.FILE,
                    file.fileName.toString(),
                    FileDetailsResponse.DATE_FORMAT.format(LocalDate.now()),
                    getMimeType(file.fileName.toString(), "application/octet-stream"),
                    it.contentLength()
                )
            }
            ?.let { Result.ok(it) }
            ?: errorResponse(HttpCode.NOT_FOUND, "File not found: $file")
    }

    override fun removeFile(file: Path): Result<Unit, ErrorResponse> {
        val request = DeleteObjectRequest.builder()
        request.bucket(bucket)
        request.key(file.toString().replace('\\', '/'))
        s3.deleteObject(request.build())
        return Result.ok(Unit)
    }

    override fun getFiles(directory: Path): Result<List<Path>, ErrorResponse> {
        return try {
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

            Result.ok(paths)
        }
        catch (exception: Exception) {
            errorResponse(HttpCode.INTERNAL_SERVER_ERROR, exception.localizedMessage)
        }
    }

    override fun getLastModifiedTime(file: Path): Result<FileTime, ErrorResponse> =
        head(file)
            ?.let { Result.ok(FileTime.from(it.lastModified())) }
            ?: getFiles(file)
                .map { files -> files.firstOrNull() }
                .mapErr { ErrorResponse(HttpCode.NOT_FOUND, "File not found: $file") }
                .flatMap { getLastModifiedTime(file.resolve(it!!.getName(0))) }

    override fun getFileSize(file: Path): Result<Long, ErrorResponse> =
        head(file)
            ?.let { Result.ok(it.contentLength()) }
            ?: errorResponse(HttpCode.NOT_FOUND, "File not found: $file")

    private fun head(file: Path): HeadObjectResponse? {
        try {
            val request = HeadObjectRequest.builder()
            request.bucket(bucket)
            request.key(file.toString().replace('\\', '/'))

            return s3.headObject(request.build())
        }
        catch (ignored: NoSuchKeyException) {
            // ignored
        }
        catch (exception: Exception) {
            exception.printStackTrace()
        }

        return null
    }

    override fun exists(file: Path): Boolean {
        val response = head(file)
        return response != null
    }

    override fun isDirectory(file: Path): Boolean =
        with(getFile(file)) {
            isOk && get().isNotEmpty() && !exists(file)
        }

    override fun isFull(): Boolean {
        TODO("Not yet implemented")
    }

    override fun usage(): Long {
        TODO("Not yet implemented")
    }

    override fun canHold(contentLength: Long): Boolean {
        return true
    }

}