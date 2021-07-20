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
package org.panda_lang.reposilite

import net.dzikoysk.dynamiclogger.backend.AggregatedLogger
import net.dzikoysk.dynamiclogger.slf4j.Slf4jLogger
import org.jetbrains.exposed.sql.Database
import org.panda_lang.reposilite.config.ConfigurationLoader
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.nio.file.Path
import java.nio.file.Paths

const val CONFIGURATION_FILE_NAME = "reposilite.cdn"

@Command(name = "reposilite", version = ["Reposilite $VERSION"])
class ReposiliteLauncher {

    @Option(names = ["--help", "-H"], usageHelp = true, description = ["display help message"])
    private var usageHelpRequested = false

    @Option(names = ["--version", "-V"], versionHelp = true, description = ["display current version of reposilite"])
    private var versionInfoRequested = false

    @Option(names = ["--test-env", "-te"], description = ["enable test mode"])
    private var testEnv = false

    @Option(names = ["--working-directory", "-wd"], description = ["set custom working directory of application instance"])
    private var workingDirectory: String? = null

    @Option(names = ["--config", "-cfg"], description = ["set custom location of configuration file"])
    private var configurationFile: String? = null

    companion object {

        fun create(vararg args: String): Reposilite? {
            val launcher = CommandLine.populateCommand(ReposiliteLauncher(), *args)

            if (launcher.usageHelpRequested) {
                CommandLine.usage(launcher, System.out)
                return null
            }

            if (launcher.versionInfoRequested) {
                println("Reposilite $VERSION")
                return null
            }

            return create(launcher.workingDirectory, launcher.configurationFile, launcher.testEnv)
        }

        fun create(workingDirectoryString: String?, configurationFileName: String?, testEnv: Boolean): Reposilite {
            var workingDirectory = Paths.get("")

            if (workingDirectoryString != null && workingDirectoryString.isNotEmpty()) {
                workingDirectory = Paths.get(workingDirectoryString)
            }

            val configurationFile = workingDirectory.resolve(
                if (configurationFileName == null || configurationFileName.isEmpty()) CONFIGURATION_FILE_NAME
                else configurationFileName
            )

            return createReposilite(configurationFile, workingDirectory, testEnv)
        }

        private fun createReposilite(configurationFile: Path, workingDirectory: Path, testEnv: Boolean): Reposilite {
            val logger = AggregatedLogger(
                Slf4jLogger(LoggerFactory.getLogger(Reposilite::class.java))
            )

            val configurationLoader = ConfigurationLoader(logger)
            val configuration = configurationLoader.tryLoad(configurationFile)

            Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;mode=MySQL", driver = "org.h2.Driver") // TOFIX: SQL schemas requires connection at startup, somehow delegate it later

            return ReposiliteWebConfiguration.createReposilite(logger, configuration, workingDirectory, testEnv)
        }
    }

}

fun main(args: Array<String>) {
    ReposiliteLauncher.create(*args)?.launch()
}