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
package org.panda_lang.reposilite.console

import org.panda_lang.reposilite.Reposilite
import org.panda_lang.reposilite.VERSION
import org.panda_lang.reposilite.console.Status.SUCCEEDED
import org.panda_lang.reposilite.shared.TimeUtils.format
import panda.utilities.IOUtils
import panda.utilities.console.Effect.GREEN
import panda.utilities.console.Effect.GREEN_BOLD
import panda.utilities.console.Effect.RED_UNDERLINED
import panda.utilities.console.Effect.RESET
import picocli.CommandLine.Command
import java.io.IOException

@Command(name = "status", description = ["Display summary status of app health"])
internal class StatusCommand(
    private val reposilite: Reposilite,
    private val remoteVersionUrl: String,
) : ReposiliteCommand {

    override fun execute(output: MutableList<String>): Status {
        val latestVersion =
            if (reposilite.testEnv) VERSION
            else getVersion()

        output.add("Reposilite $VERSION Status")
        output.add("  Active: $GREEN_BOLD${reposilite.webServer.isAlive()}$RESET")
        output.add("  Uptime: ${format(reposilite.getUptime() / 1000.0 / 60.0)}min")
        output.add("  Memory usage of process: ${memoryUsage()}")
        output.add("  Exceptions: ${reposilite.failureFacade.getFailures().size}")
        output.add("  Latest version of reposilite: $latestVersion")

        return SUCCEEDED
    }

    private fun getVersion(): String =
        IOUtils.fetchContent(remoteVersionUrl)
            .orElseGet { ioException: IOException -> "$remoteVersionUrl is unavailable: ${ioException.message}" }
            .let { (if (VERSION == it) GREEN else RED_UNDERLINED).toString() + it + RESET }

    private fun memoryUsage(): String =
        format((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0) + "M"

}