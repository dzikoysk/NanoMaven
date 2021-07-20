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

import net.dzikoysk.cdn.CdnFactory
import net.dzikoysk.dynamiclogger.Journalist
import net.dzikoysk.dynamiclogger.Logger
import org.panda_lang.reposilite.shared.FilesUtils.getExtension
import panda.utilities.ClassUtils
import panda.utilities.StringUtils
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import kotlin.reflect.full.isSubclassOf

class ConfigurationLoader(private val journalist: Journalist) : Journalist {

    fun tryLoad(customConfigurationFile: Path): Configuration {
        return try {
            load(customConfigurationFile)
        } catch (exception: Exception) {
            throw RuntimeException("Cannot load configuration", exception)
        }
    }

    fun load(configurationFile: Path): Configuration {
        require(getExtension(configurationFile.fileName.toString()) == "cdn") { "Custom configuration file does not have '.cdn' extension" }

        val cdn = CdnFactory.createStandard()

        val configuration =
            if (Files.exists(configurationFile))
                cdn.load(
                    String(Files.readAllBytes(configurationFile), StandardCharsets.UTF_8),
                    Configuration::class.java
                )
            else createConfiguration(configurationFile)

        verifyBasePath(configuration)
        verifyProxied(configuration)
        Files.write(configurationFile, cdn.render(configuration).toByteArray(StandardCharsets.UTF_8), CREATE, TRUNCATE_EXISTING)
        loadProperties(configuration)

        return configuration
    }

    private fun createConfiguration(configurationFile: Path): Configuration {
        val legacyConfiguration = configurationFile.resolveSibling(configurationFile.fileName.toString().replace(".cdn", ".yml"))

        if (!Files.exists(legacyConfiguration)) {
            logger.info("Generating default configuration file.")
            return Configuration()
        }

        logger.info("Legacy configuration file has been found")
        val configuration = CdnFactory.createYamlLike().load(String(Files.readAllBytes(configurationFile), StandardCharsets.UTF_8), Configuration::class.java)
        logger.info("YAML configuration has been converted to CDN format")
        Files.delete(legacyConfiguration)

        return configuration
    }

    private fun verifyBasePath(configuration: Configuration) {
        var basePath = configuration.basePath

        if (!StringUtils.isEmpty(basePath)) {
            if (!basePath.startsWith("/")) {
                basePath = "/$basePath"
            }

            if (!basePath.endsWith("/")) {
                basePath += "/"
            }

            configuration.basePath = basePath
        }
    }

    private fun verifyProxied(configuration: Configuration) {
        for (index in configuration.proxied.indices) {
            val proxied = configuration.proxied[index]

            if (proxied.endsWith("/")) {
                configuration.proxied[index] = proxied.substring(0, proxied.length - 1)
            }
        }
    }

    private fun loadProperties(configuration: Configuration) {
        for (declaredField in configuration.javaClass.declaredFields) {
            val custom = System.getProperty("reposilite." + declaredField.name)

            if (StringUtils.isEmpty(custom)) {
                continue
            }

            val type = ClassUtils.getNonPrimitiveClass(declaredField.type).kotlin

            val customValue: Any? =
                if (String::class == type) {
                    custom
                }
                else if (Int::class == type) {
                    custom.toInt()
                }
                else if (Boolean::class == type) {
                    java.lang.Boolean.parseBoolean(custom)
                }
                else if (MutableCollection::class.isSubclassOf(type)) {
                    listOf(*custom.split(",").toTypedArray())
                }
                else {
                    logger.info("Unsupported type: $type for $custom")
                    continue
                }

            try {
                declaredField.isAccessible = true
                declaredField[configuration] = customValue
            } catch (illegalAccessException: IllegalAccessException) {
                throw RuntimeException("Cannot modify configuration value", illegalAccessException)
            }
        }
    }

    override fun getLogger(): Logger =
        journalist.logger

}