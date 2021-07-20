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

package org.panda_lang.reposilite.failure.application

import io.javalin.Javalin
import net.dzikoysk.dynamiclogger.Journalist
import org.panda_lang.reposilite.console.ConsoleFacade
import org.panda_lang.reposilite.failure.FailureFacade
import org.panda_lang.reposilite.failure.FailuresCommand
import org.panda_lang.reposilite.failure.infrastructure.FailureHandler
import org.panda_lang.reposilite.web.api.Routes

internal object FailureWebConfiguration {

    fun createFacade(journalist: Journalist) =
        FailureFacade(journalist)

    fun initialize(consoleFacade: ConsoleFacade, failureFacade: FailureFacade) {
        consoleFacade.registerCommand(FailuresCommand(failureFacade))
    }

    fun routing(): List<Routes> =
        listOf()

    fun javalin(javalin: Javalin, failureFacade: FailureFacade) {
        javalin.exception(Exception::class.java, FailureHandler(failureFacade))
    }

}