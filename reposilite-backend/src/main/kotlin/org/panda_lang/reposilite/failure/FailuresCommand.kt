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
package org.panda_lang.reposilite.failure

import org.panda_lang.reposilite.console.ReposiliteCommand
import org.panda_lang.reposilite.console.Status
import org.panda_lang.reposilite.console.Status.SUCCEEDED
import picocli.CommandLine.Command

@Command(name = "failures", description = ["Display all recorded exceptions"])
internal class FailuresCommand(private val failureFacade: FailureFacade) : ReposiliteCommand {

    override fun execute(output: MutableList<String>): Status {
        if (!failureFacade.hasFailures()) {
            output.add("No exception has occurred yet")
            return SUCCEEDED
        }

        output.add("")
        output.add("List of cached failures: " + "(" + failureFacade.getFailures().size + ")")
        output.add("")

        failureFacade.getFailures()
            .map { it.split(System.lineSeparator()) }
            .forEach { output.addAll(it) }

        return SUCCEEDED
    }

}