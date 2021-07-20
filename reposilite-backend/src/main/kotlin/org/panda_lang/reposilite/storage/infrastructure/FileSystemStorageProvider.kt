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
import org.panda_lang.reposilite.maven.api.DirectoryInfo
import org.panda_lang.reposilite.maven.api.DocumentInfo
import org.panda_lang.reposilite.maven.api.FileDetails
import org.panda_lang.reposilite.shared.FilesUtils.getMimeType
import org.panda_lang.reposilite.storage.StorageProvider
import org.panda_lang.reposilite.web.api.MimeTypes
import org.panda_lang.reposilite.web.asResult
import panda.std.Result
import panda.std.function.ThrowingBiFunction
import panda.std.function.ThrowingFunction
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE
import java.nio.file.attribute.FileTime
import java.util.stream.Collectors
import kotlin.streams.toList

/**
 * @param rootDirectory root directory of storage space
 */
internal abstract class FileSystemStorageProvider protected constructor(
    private val rootDirectory: Path
 ) : StorageProvider {

    private fun <VALUE> withFile(consumer: () -> Result<VALUE, ErrorResponse>): Result<VALUE, ErrorResponse> =
        try {
            consumer()
        } catch (ioException: IOException) {
            errorResponse(HttpCode.INTERNAL_SERVER_ERROR, ioException.localizedMessage)
        }

    private fun <VALUE> withExistingFile(file: Path, consumer: () -> Result<VALUE, ErrorResponse>): Result<VALUE, ErrorResponse> =
        withFile {
            if (Files.exists(file)) {
                consumer()
            } else {
                errorResponse(HttpCode.NOT_FOUND, "File not found: $file")
            }
        }

    override fun putFile(file: Path, bytes: ByteArray): Result<FileDetails, ErrorResponse> =
        putFile(file, bytes, { it.size }) { input, output ->
            output.write(ByteBuffer.wrap(input))
        }

    override fun putFile(file: Path, inputStream: InputStream): Result<FileDetails, ErrorResponse> =
        putFile(file, inputStream, { it.available() }) { input, output ->
            val buffer = ByteArrayOutputStream()
            var nRead: Int
            val data = ByteArray(1024)

            while (input.read(data, 0, data.size).also { nRead = it } != -1) {
                buffer.write(data, 0, nRead)
            }

            buffer.flush()
            val byteArray = buffer.toByteArray()
            output.write(ByteBuffer.wrap(byteArray))
            byteArray.size
        }

    private fun <T> putFile(
        file: Path,
        input: T,
        measure: ThrowingFunction<T, Int, IOException>,
        writer: ThrowingBiFunction<T, FileChannel, Int, IOException>
    ): Result<FileDetails, ErrorResponse> =
        withFile {
            val size = measure.apply(input).toLong()

            if (canHold(size).isErr) {
                return@withFile errorResponse(HttpCode.INSUFFICIENT_STORAGE, "Not enough storage space available")
            }

            if (file.parent != null && !Files.exists(file.parent)) {
                Files.createDirectories(file.parent)
            }

            if (!Files.exists(file)) {
                Files.createFile(file)
            }

            val fileChannel = FileChannel.open(file, WRITE, CREATE)
            // TOFIX: FS locks are not truly respected, there might be a need to enhanced it with .lock file to be sure if it's respected.
            // In theory people should not really share the same FS through instances.
            // ~ https://github.com/dzikoysk/reposilite/issues/264
            fileChannel.lock()

            val bytesWritten = writer.apply(input, fileChannel).toLong()
            fileChannel.close()

            DocumentInfo(file.fileName.toString(), getMimeType(file.toString(), MimeTypes.OCTET_STREAM), bytesWritten) {
                Files.newInputStream(file)
            }.asResult()
        }

    override fun getFile(file: Path): Result<InputStream, ErrorResponse> =
        withExistingFile(file) {
            if (Files.isDirectory(file)) {
                errorResponse(HttpCode.NO_CONTENT, "Requested file is a directory")
            }
            else {
                Files.newInputStream(file).asResult()
            }
        }

    override fun getFileDetails(file: Path): Result<FileDetails, ErrorResponse> =
        withExistingFile(file) {
            if (Files.isDirectory(file))
                DirectoryInfo(
                    file.fileName.toString(),
                    Files.list(file)
                        .map { getFileDetails(it) }
                        .filter { it.isOk }
                        .map { it.get() }
                        .toList()
                ).asResult()
            else
                DocumentInfo(
                    file.fileName.toString(),
                    getMimeType(file.fileName.toString(), MimeTypes.OCTET_STREAM),
                    Files.size(file),
                    { Files.newInputStream(file) }
                ).asResult()
        }

    override fun removeFile(file: Path): Result<*, ErrorResponse> =
        withExistingFile(file) {
            Files.delete(file).asResult()
        }

    override fun getFiles(directory: Path): Result<List<Path>, ErrorResponse> =
        withExistingFile(directory) {
            Files.walk(directory, 1)
                .filter { it != directory }
                .collect(Collectors.toList())
                .asResult()
        }

    override fun getLastModifiedTime(file: Path): Result<FileTime, ErrorResponse> =
        withExistingFile(file) {
            Files.getLastModifiedTime(file).asResult()
        }

    override fun getFileSize(file: Path): Result<Long, ErrorResponse> =
        withExistingFile(file) {
            if (Files.isDirectory(file)) {
                Files.walk(file)
                    .mapToLong { Files.size(it) }
                    .sum()
                    .asResult()
            } else {
                Files.size(file).asResult()
            }
        }

    override fun exists(file: Path): Boolean =
        Files.exists(file)

    override fun isDirectory(file: Path): Boolean =
        Files.isDirectory(file)

    override fun usage(): Result<Long, ErrorResponse> =
        withFile {
            Files.walk(rootDirectory)
                .filter { !Files.isDirectory(it) }
                .mapToLong { Files.size(it) }
                .sum()
                .asResult()
        }

    override fun isFull(): Boolean =
        canHold(0).isErr

    override fun shutdown() {}

}