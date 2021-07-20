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
package org.panda_lang.reposilite.shared

import com.google.common.hash.Hashing
import org.panda_lang.reposilite.Reposilite
import org.panda_lang.reposilite.maven.api.Repository
import org.panda_lang.reposilite.web.api.MimeTypes
import panda.utilities.IOUtils
import panda.utilities.StringUtils
import java.io.Closeable
import java.io.IOException
import java.nio.file.Path
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.util.regex.Pattern
import kotlin.math.abs

object FilesUtils {

    private val DISPLAY_SIZE_PATTERN = Pattern.compile("([0-9]+)(([KkMmGg])[Bb])")

    private const val KB_FACTOR: Long = 1024
    private const val MB_FACTOR = 1024 * KB_FACTOR
    private const val GB_FACTOR = 1024 * MB_FACTOR

    private val READABLE_CONTENT = arrayOf(
        ".xml",
        ".pom",
        ".txt",
        ".json",
        ".cdn",
        ".yaml",
        ".yml"
    )

    fun displaySizeToBytesCount(displaySize: String): Long {
        val match = DISPLAY_SIZE_PATTERN.matcher(displaySize)

        if (!match.matches() || match.groupCount() != 3) {
            return displaySize.toLong()
        }

        val value = match.group(1).toLong()

        return when (match.group(2).toUpperCase()) {
            "GB" -> value * GB_FACTOR
            "MB" -> value * MB_FACTOR
            "KB" -> value * KB_FACTOR
            else -> throw NumberFormatException("Wrong format")
        }
    }

    // Source
    // ~ https://stackoverflow.com/a/3758880/3426515
    fun humanReadableByteCount(bytes: Long): String {
        val absB =
            if (bytes == Long.MIN_VALUE) Long.MAX_VALUE
            else abs(bytes)

        if (absB < 1024) {
            return "$bytes B"
        }

        var value = absB
        val characterIterator: CharacterIterator = StringCharacterIterator("KMGTPE")
        var i = 40

        while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
            value = value shr 10
            characterIterator.next()
            i -= 10
        }

        value *= java.lang.Long.signum(bytes).toLong()
        return String.format("%.1f %ciB", value / 1024.0, characterIterator.current())
    }

    fun isReadable(name: String): Boolean =
        READABLE_CONTENT.any { extension -> name.endsWith(extension) }

    fun getMimeType(path: String, defaultType: String): String =
        MimeTypes.getMimeType(getExtension(path), defaultType)

    fun writeFileChecksums(repository: Repository, path: Path, bytes: ByteArray?) {
        val relativePath = repository.relativize(path)

        val md5 = relativePath.resolveSibling(relativePath.fileName.toString() + ".md5")
        repository.putFile(md5, Hashing.md5().hashBytes(bytes).asBytes())

        val sha1 = relativePath.resolveSibling(relativePath.fileName.toString() + ".sha1")
        val sha256 = relativePath.resolveSibling(relativePath.fileName.toString() + ".sha256")
        val sha512 = relativePath.resolveSibling(relativePath.fileName.toString() + ".sha512")
        repository.putFile(sha1, Hashing.sha1().hashBytes(bytes).asBytes())
        repository.putFile(sha256, Hashing.sha256().hashBytes(bytes).asBytes())
        repository.putFile(sha512, Hashing.sha512().hashBytes(bytes).asBytes())
    }

    fun close(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (ignored: IOException) {
                // idc
            }
        }
    }

    fun toNames(files: List<Path>): List<String> = files
        .map { it.fileName.toString() }
        .toList()

    fun getExtension(name: String): String {
        val occurrence = name.lastIndexOf(".")
        return if (occurrence == -1) StringUtils.EMPTY else name.substring(occurrence + 1)
    }

    fun getResource(name: String): String =
        IOUtils.convertStreamToString(Reposilite::class.java.getResourceAsStream(name))
            .orElseThrow { RuntimeException("Cannot load resource $name", it) }

}