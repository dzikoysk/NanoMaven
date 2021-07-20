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

import org.panda_lang.reposilite.failure.FailureFacade
import java.io.InputStream
import java.util.*

internal class ConsoleThread(
    private val console: Console,
    private val source: InputStream,
    private val failureFacade: FailureFacade
) : Thread() {

    init {
        name = "Reposilite | Console Thread"
        isDaemon = true
    }

    override fun run() {
        val input = Scanner(source)

        if (!input.hasNextLine()) {
            console.logger.warn("Interactive CLI is not available in current environment.")
            console.logger.warn("Solution for Docker users: https://docs.docker.com/engine/reference/run/#foreground")
            return
        }

        do {
            val command = input.nextLine()

            try {
                console.execute(command)
            }
            catch (exception: Exception) {
                failureFacade.throwException("Command: $command", exception)
            }
        } while (!isInterrupted && input.hasNextLine())
    }

}