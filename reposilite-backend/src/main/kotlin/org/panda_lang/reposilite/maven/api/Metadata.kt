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
package org.panda_lang.reposilite.maven

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

internal const val METADATA_FILE_NAME = "maven-metadata"
internal const val METADATA_FILE = "$METADATA_FILE_NAME.xml"

@JacksonXmlRootElement(localName = "metadata")
internal data class Metadata(
    val groupId: String? = null,
    val artifactId: String? = null,
    val version: String? = null,
    val versioning: Versioning? = Versioning()
)

@JacksonXmlRootElement(localName = "versioning")
internal data class Versioning(
    val release: String? = null,
    val latest: String? = null,
    @JacksonXmlElementWrapper(localName = "versions")
    @JacksonXmlProperty(localName = "version")
    val versions: Collection<String>? = emptyList(),
    val snapshot: Snapshot? = null,
    @JacksonXmlElementWrapper(localName = "snapshotVersions")
    @JacksonXmlProperty(localName = "snapshotVersion")
    val snapshotVersions: Collection<SnapshotVersion>? = emptyList(),
    val lastUpdated: String? = null
)

@JacksonXmlRootElement(localName = "snapshot")
internal data class Snapshot(
    val timestamp: String? = null,
    val buildNumber: String? = null
)

@JacksonXmlRootElement(localName = "snapshotVersion")
internal data class SnapshotVersion(
    val extension: String? = null,
    val value: String? = null,
    val updated: String? = null
)