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

import io.javalin.http.HttpCode
import net.dzikoysk.dynamiclogger.Journalist
import net.dzikoysk.dynamiclogger.Logger
import org.panda_lang.reposilite.console.api.ExecutionResponse
import org.panda_lang.reposilite.failure.api.ErrorResponse
import org.panda_lang.reposilite.failure.api.errorResponse
import panda.utilities.StringUtils
import panda.std.Result
import picocli.CommandLine

const val MAX_COMMAND_LENGTH = 1024

class ConsoleFacade internal constructor(
    private val journalist: Journalist,
    internal val console: Console
) : Journalist {

    fun executeCommand(command: String): Result<ExecutionResponse, ErrorResponse> {
        if (StringUtils.isEmpty(command)) {
            return errorResponse(HttpCode.BAD_REQUEST, "Missing command")
        }

        if (command.length > MAX_COMMAND_LENGTH) {
            return errorResponse(HttpCode.BAD_REQUEST, "The given command exceeds allowed length (${command.length} > $MAX_COMMAND_LENGTH)")
        }

        return Result.ok(console.execute(command))
    }

    fun registerCommand(command: ReposiliteCommand): CommandLine =
        console.registerCommand(command)

    fun getCommands(): Map<String, CommandLine> =
        console.getCommands()

    override fun getLogger(): Logger =
        journalist.logger

}