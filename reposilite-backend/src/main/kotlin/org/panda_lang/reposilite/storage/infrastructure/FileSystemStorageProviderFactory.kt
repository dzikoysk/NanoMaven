package org.panda_lang.reposilite.storage.infrastructure

import net.dzikoysk.dynamiclogger.Journalist
import org.panda_lang.reposilite.shared.FilesUtils
import java.nio.file.Path

internal object FileSystemStorageProviderFactory {

    fun of(journalist: Journalist, rootDirectory: Path, quota: String): FileSystemStorageProvider =
        if (quota.endsWith("%")) {
            of(journalist, rootDirectory, quota.substring(0, quota.length - 1).toInt() / 100.0)
        } else {
            of(rootDirectory, FilesUtils.displaySizeToBytesCount(quota))
        }

    /**
     * @param rootDirectory root directory of storage space
     * @param maxSize the largest amount of storage available for use, in bytes
     */
    fun of(rootDirectory: Path, maxSize: Long): FileSystemStorageProvider =
        FixedQuota(rootDirectory, maxSize)

    /**
     * @param rootDirectory root directory of storage space
     * @param maxPercentage the maximum percentage of the disk available for use
     */
    fun of(journalist: Journalist, rootDirectory: Path, maxPercentage: Double): FileSystemStorageProvider =
        PercentageQuota(journalist, rootDirectory, maxPercentage)

}