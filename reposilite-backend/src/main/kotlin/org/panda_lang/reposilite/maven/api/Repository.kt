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
package org.panda_lang.reposilite.maven.api

import io.javalin.http.HttpCode.NOT_FOUND
import org.panda_lang.reposilite.failure.api.ErrorResponse
import org.panda_lang.reposilite.failure.api.errorResponse
import org.panda_lang.reposilite.maven.MetadataUtils
import org.panda_lang.reposilite.storage.StorageProvider
import panda.std.Result
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.FileTime

enum class RepositoryVisibility {
    PUBLIC,
    HIDDEN,
    PRIVATE
}

class Repository internal constructor(
    val name: String,
    val visibility: RepositoryVisibility,
    private val storageProvider: StorageProvider,
    val isDeployEnabled: Boolean
) : Comparator<Path> {

    companion object {
        private val REPOSITORIES_PATH = Paths.get("repositories")
    }

    private fun <R> relativize(file: String, consumer: (Path) -> Result<R, ErrorResponse>): Result<R, ErrorResponse> =
        relativize(file)
            ?.let { consumer(it) }
            ?: errorResponse(NOT_FOUND, "Invalid GAV")

    override fun compare(path: Path, toPath: Path): Int =
        relativize(path).compareTo(relativize(toPath))

    fun putFile(file: String, bytes: ByteArray): Result<FileDetails, ErrorResponse> =
        relativize(file) { putFile(it, bytes) }

    fun putFile(file: Path, bytes: ByteArray): Result<FileDetails, ErrorResponse> =
        storageProvider.putFile(relativize(file), bytes)

    fun putFile(file: String, inputStream: InputStream): Result<FileDetails, ErrorResponse> =
        relativize(file) { putFile(it, inputStream) }

    fun putFile(file: Path, inputStream: InputStream): Result<FileDetails, ErrorResponse> =
        storageProvider.putFile(relativize(file), inputStream)

    fun getFile(file: String): Result<InputStream, ErrorResponse> =
        relativize(file) { getFile(it) }

    fun getFile(file: Path): Result<InputStream, ErrorResponse> =
        storageProvider.getFile(relativize(file))

    fun getFileDetails(file: String): Result<FileDetails, ErrorResponse> =
        relativize(file) { getFileDetails(it) }

    fun getFileDetails(file: Path): Result<FileDetails, ErrorResponse> =
        storageProvider.getFileDetails(relativize(file))

    fun removeFile(file: String): Result<*, ErrorResponse> =
        relativize(file) { removeFile(it) }

    fun removeFile(file: Path): Result<*, ErrorResponse> =
        storageProvider.removeFile(relativize(file))

    fun getFiles(directory: String): Result<List<Path>, ErrorResponse> =
        relativize(directory) { getFiles(directory) }

    fun getFiles(directory: Path): Result<List<Path>, ErrorResponse> =
        storageProvider.getFiles(relativize(directory))

    fun getLastModifiedTime(file: String): Result<FileTime, ErrorResponse> =
        relativize(file) { getLastModifiedTime(file) }

    fun getLastModifiedTime(file: Path): Result<FileTime, ErrorResponse> =
        storageProvider.getLastModifiedTime(relativize(file))

    fun getFileSize(file: Path): Result<Long, ErrorResponse> =
        storageProvider.getFileSize(relativize(file))

    fun exists(file: Path): Boolean =
        storageProvider.exists(relativize(file))

    fun isDirectory(file: Path): Boolean =
        storageProvider.isDirectory(relativize(file))

    fun isFull(): Boolean =
        storageProvider.isFull()

    fun getUsage(): Result<Long, ErrorResponse> =
        storageProvider.usage()

    fun canHold(contentLength: Long): Result<*, ErrorResponse> =
        storageProvider.canHold(contentLength)

    fun shutdown() =
        storageProvider.shutdown()

    /**
     * Inserts repository name and repositories directory into the given path
     */
    fun relativize(path: Path): Path {
        var relativePath = path

        if (!relativePath.startsWith(REPOSITORIES_PATH)) {
            if (!relativePath.startsWith(name)) {
                relativePath = Paths.get(name).resolve(relativePath)
            }

            relativePath = REPOSITORIES_PATH.resolve(relativePath)
        }
        else if (relativePath.startsWith(name)) {
            relativePath = REPOSITORIES_PATH.relativize(relativePath)
        }

        return relativePath
    }

    fun relativize(gav: String): Path? =
        MetadataUtils.normalizeUri(gav)?.let { relativize(Paths.get(it)) }

}