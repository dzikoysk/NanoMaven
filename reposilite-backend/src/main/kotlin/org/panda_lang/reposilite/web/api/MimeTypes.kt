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
package org.panda_lang.reposilite.web.api

object MimeTypes {

    const val PLAIN = "text/plain"
    const val HTML = "text/html"
    const val XML = "text/xml"
    const val OCTET_STREAM = "application/octet-stream"
    const val JAVASCRIPT = "application/javascript"
    const val JSON = "application/json"
    const val MULTIPART_FORM_DATA = "multipart/form-data"

    private val STANDARD_TYPES: MutableMap<String, String> = HashMap()

    init {
        // Fallback list of basic mime types used by Maven repository
        // ~ https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types

        /* Text */

        STANDARD_TYPES["txt"] = PLAIN
        STANDARD_TYPES["css"] = "text/css"
        STANDARD_TYPES["csv"] = "text/csv"
        STANDARD_TYPES["htm"] = HTML
        STANDARD_TYPES["html"] = HTML
        STANDARD_TYPES["xml"] = PLAIN

        /* Image */

        STANDARD_TYPES["ico"] = "image/vnd.microsoft.icon"
        STANDARD_TYPES["jpeg"] = "image/jpeg"
        STANDARD_TYPES["jpg"] = "image/jpg"
        STANDARD_TYPES["png"] = "image/png"
        STANDARD_TYPES["tif"] = "image/tiff"
        STANDARD_TYPES["tiff"] = "image/tiff"

        /* Font */

        STANDARD_TYPES["otf"] = "font/otf"
        STANDARD_TYPES["ttf"] = "font/ttf"

        /* Application */

        STANDARD_TYPES["bin"] = OCTET_STREAM
        STANDARD_TYPES["bz"] = "application/x-bzip"
        STANDARD_TYPES["bz2"] = "application/x-bzip2"
        STANDARD_TYPES["cdn"] = "application/cdn"
        STANDARD_TYPES["gz"] = "application/gzip"
        STANDARD_TYPES["js"] = JAVASCRIPT
        STANDARD_TYPES["json"] = JSON
        STANDARD_TYPES["mpkg"] = "application/vnd.apple.installer+xml"
        STANDARD_TYPES["jar"] = "application/java-archive"
        STANDARD_TYPES["rar"] = "application/vnd.rar"
        STANDARD_TYPES["sh"] = "application/x-sh"
        STANDARD_TYPES["tar"] = "application/x-tar"
        STANDARD_TYPES["xhtml"] = "application/xhtml+xml"
        STANDARD_TYPES["yaml"] = "application/yaml"
        STANDARD_TYPES["zip"] = "application/zip"
        STANDARD_TYPES["7z"] = "application/x-7z-compressed"
    }

    fun getMimeType(name: String, defaultTemplate: String = PLAIN) =
        STANDARD_TYPES.getOrDefault(name, defaultTemplate)

}