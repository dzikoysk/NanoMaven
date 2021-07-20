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

import org.panda_lang.reposilite.VERSION
import org.panda_lang.reposilite.console.Status.FAILED
import org.panda_lang.reposilite.console.Status.SUCCEEDED
import panda.utilities.text.Joiner
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.ArgSpec
import picocli.CommandLine.Parameters
import java.util.*

@Command(name = "help", aliases = ["?"], helpCommand = true, description = ["List of available commands"])
internal class HelpCommand(private val consoleFacade: ConsoleFacade) : ReposiliteCommand {

    @Parameters(index = "0", paramLabel = "[<command>]", description = ["display usage of the given command"], defaultValue = "")
    private lateinit var requestedCommand: String

    override fun execute(output: MutableList<String>): Status {
        val uniqueCommands: MutableSet<CommandLine> = TreeSet(Comparator.comparing { it.commandName })

        if (requestedCommand.isNotEmpty()) {
            val requested: CommandLine? = consoleFacade.getCommands()[requestedCommand]

            if (requested == null) {
                output.add("Unknown command '$requestedCommand'")
                return FAILED
            }

            output.add(requested.usageMessage)
            return SUCCEEDED
        }

        if (uniqueCommands.isEmpty()) {
            uniqueCommands.addAll(consoleFacade.getCommands().values)
        }

        output.add("Reposilite $VERSION Commands:")

        for (command in uniqueCommands) {
            val specification = command.commandSpec

            output.add("  " + command.commandName
                    + " " + Joiner.on(" ").join(specification.args()) { obj: ArgSpec -> obj.paramLabel() }
                    + " - " + Joiner.on(". ").join(*specification.usageMessage().description()))
        }

        return SUCCEEDED
    }

}