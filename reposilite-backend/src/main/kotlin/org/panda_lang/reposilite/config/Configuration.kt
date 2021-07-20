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
package org.panda_lang.reposilite.config

import net.dzikoysk.cdn.entity.Contextual
import net.dzikoysk.cdn.entity.Description
import org.panda_lang.reposilite.maven.api.RepositoryVisibility.PRIVATE
import java.io.Serializable

class Configuration : Serializable {

    /* General */

    @Description(
        "# ~~~~~~~~~~~~~~~~~~~~~~ #",
        "#       Reposilite       #",
        "# ~~~~~~~~~~~~~~~~~~~~~~ #",
        "",
        "# Hostname"
    )
    @JvmField
    var hostname = "0.0.0.0"

    @Description("# Port to bind")
    @JvmField
    var port = 80

    @Description("# Custom base path")
    @JvmField
    var basePath = "/"

    @Description(
        "# Any kind of proxy services change real ip.",
        "# The origin ip should be available in one of the headers.",
        "# Nginx: X-Forwarded-For",
        "# Cloudflare: CF-Connecting-IP",
        "# Popular: X-Real-IP"
    )
    @JvmField
    var forwardedIp = "X-Forwarded-For"

    @Description("# Enable Swagger (/swagger-docs) and Swagger UI (/swagger)")
    @JvmField
    var swagger = false

    // @Description("# Run Reposilite using Jakarta Servlet server (not supported yet)")
    // public boolean servlet = false;

    @Description("# Amount of messages stored in cached logger.")
    @JvmField
    var cachedLogSize = 100

    @Description("# Debug")
    @JvmField
    var debugEnabled = false

    /* SSL */

    @Description(
        "",
        "# Support encrypted connections"
    )
    @JvmField
    var sslEnabled = false

    @Description("# SSL port to bind")
    @JvmField
    var sslPort = 443

    @Description(
        "# Key store file to use.",
        "# You can specify absolute path to the given file or use \${WORKING_DIRECTORY} variable."
    )
    @JvmField
    var keyStorePath = "\${WORKING_DIRECTORY}/keystore.jks"

    @Description("# Key store password to use")
    @JvmField
    var keyStorePassword = ""

    @Description("# Redirect http traffic to https")
    @JvmField
    var enforceSsl = false

    /* Repository properties */

    @Description(
        "",
        "# List of supported Maven repositories.",
        "# First directory on the list is the main (primary) repository.",
        "# Tu mark repository as private, add the \"--private\" flag"
    )
    @JvmField
    var repositories: Map<String, RepositoryConfiguration> = mutableMapOf(
        "releases" to RepositoryConfiguration(),
        "snapshots" to RepositoryConfiguration(),
        "private" to RepositoryConfiguration().also { it.visibility = PRIVATE.name.toLowerCase() }
    )

    @Contextual
    class RepositoryConfiguration : Serializable {

        @Description("# Supported visibilities: public, hidden, private")
        @JvmField
        var visibility = "public"

        @Description(
            "# Used storage type. Supported storage providers:",
            "# - fs",
            "# - s3 bucket-name region"
        )
        @JvmField
        var storageProvider = "fs"

        @Description(
            "# Control the maximum amount of data stored in this repository",
            "# Supported formats: 90%, 500MB, 10GB"
        )
        @JvmField
        var diskQuota = "10GB"

        @Description("# Accept deployment connections")
        @JvmField
        var deployEnabled = true

        @Description("# Does this repository accept redeployment of the same artifact version")
        @JvmField
        var redeploy = false

    }

    /* Proxy */

    @Description(
        "",
        "# List of proxied repositories.",
        "# Reposilite will search for an artifact in remote repositories listed below,",
        "# if the requested artifact was not found."
    )
    @JvmField
    var proxied = mutableListOf<String>()

    @Description("# Reposilite can store proxied artifacts locally to reduce response time and improve stability")
    @JvmField
    var storeProxied = true

    @Description(
        "# Proxying is disabled by default in private repositories because of the security policy.",
        "# Enabling this feature may expose private data like i.e. artifact name used in your company."
    )
    @JvmField
    var proxyPrivate = false

    @Description("# How long Reposilite can wait for establishing the connection with a remote host. (In seconds)")
    @JvmField
    var proxyConnectTimeout = 3

    @Description(
        "# How long Reposilite can read data from remote proxy. (In seconds)",
        "# Increasing this value may be required in case of proxying slow remote repositories."
    )
    @JvmField
    var proxyReadTimeout = 15

    /* Frontend properties */

    @Description(
        "",
        "# Title displayed by frontend"
    )
    @JvmField
    var title = "#onlypanda"

    @Description("# Description displayed by frontend")
    @JvmField
    var description = "Public Maven repository hosted through the Reposilite"

    @Description("# Accent color used by frontend")
    @JvmField
    var accentColor = "#2fd4aa"

}