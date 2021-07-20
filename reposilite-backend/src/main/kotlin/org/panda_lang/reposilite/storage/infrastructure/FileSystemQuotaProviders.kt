package org.panda_lang.reposilite.storage.infrastructure

import io.javalin.http.HttpCode.INSUFFICIENT_STORAGE
import net.dzikoysk.dynamiclogger.Journalist
import org.panda_lang.reposilite.failure.api.ErrorResponse
import panda.std.Result
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * @param rootDirectory root directory of storage space
 * @param maxSize the largest amount of storage available for use, in bytes
 */
internal class FixedQuota(rootDirectory: Path, private val maxSize: Long) : FileSystemStorageProvider(rootDirectory) {


    init {
        if (maxSize <= 0) {
            throw IllegalArgumentException("Max size parameter has to be a value greater than 0")
        }
    }

    override fun canHold(contentLength: Long): Result<*, ErrorResponse> =
        usage()
            .map { it + contentLength }
            .filter({ it < maxSize }, { ErrorResponse(INSUFFICIENT_STORAGE, "Repository cannot hold the given file") })
}

/**
 * @param rootDirectory root directory of storage space
 * @param maxPercentage the maximum percentage of the disk available for use
 */
internal class PercentageQuota(
    private val journalist: Journalist,
    private val rootDirectory: Path,
    private val maxPercentage: Double
) : FileSystemStorageProvider(rootDirectory) {

    init {
        if (maxPercentage > 1 || maxPercentage <= 0) {
            throw IllegalArgumentException("Percentage parameter has to be a value between 0.0 and 1.0")
        }
    }

    override fun canHold(contentLength: Long): Result<*, ErrorResponse> =
        usage()
            .map { it + contentLength }
            .filter({ newUsage ->
                try {
                    val capacity = Files.getFileStore(rootDirectory).usableSpace.toDouble()
                    val percentage = newUsage / capacity
                    percentage < maxPercentage
                } catch (ioException: IOException) {
                    journalist.logger.exception(ioException)
                    false
                }

            }, { ErrorResponse(INSUFFICIENT_STORAGE, "Repository cannot hold the given file") })

}
