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

import com.fasterxml.jackson.annotation.JsonIgnore
import org.panda_lang.reposilite.maven.api.FileType.DIRECTORY
import org.panda_lang.reposilite.maven.api.FileType.FILE
import org.panda_lang.reposilite.shared.FilesUtils
import java.io.InputStream

enum class FileType {
    FILE,
    DIRECTORY
}

sealed class FileDetails(
    val type: FileType,
    val name: String
) : Comparable<FileDetails> {

    override fun compareTo(other: FileDetails): Int =
        type.compareTo(other.type).takeIf { it != 0 } ?: name.compareTo(other.name)

    @JsonIgnore
    fun isReadable(): Boolean =
        FilesUtils.isReadable(name)

}

class DocumentInfo(
    name: String,
    val contentType: String,
    val contentLength: Long,
    @JsonIgnore
    val content: () -> InputStream
) : FileDetails(FILE, name)

class DirectoryInfo(
    name: String,
    val files: List<FileDetails>
) : FileDetails(DIRECTORY, name)