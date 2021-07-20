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
package org.panda_lang.reposilite.console.infrastructure

import com.dzikoysk.openapi.annotations.HttpMethod
import com.dzikoysk.openapi.annotations.OpenApi
import com.dzikoysk.openapi.annotations.OpenApiContent
import com.dzikoysk.openapi.annotations.OpenApiParam
import com.dzikoysk.openapi.annotations.OpenApiResponse
import io.javalin.http.HttpCode.UNAUTHORIZED
import org.panda_lang.reposilite.console.ConsoleFacade
import org.panda_lang.reposilite.console.MAX_COMMAND_LENGTH
import org.panda_lang.reposilite.console.api.ExecutionResponse
import org.panda_lang.reposilite.failure.api.ErrorResponse
import org.panda_lang.reposilite.failure.api.errorResponse
import org.panda_lang.reposilite.web.api.Route
import org.panda_lang.reposilite.web.api.RouteMethod.POST
import org.panda_lang.reposilite.web.api.Routes

private const val ROUTE = "/api/execute"

internal class RemoteExecutionEndpoint(private val consoleFacade: ConsoleFacade) : Routes {

    @OpenApi(
        path = ROUTE,
        methods = [HttpMethod.POST],
        summary = "Remote command execution",
        description = "Execute command using POST request. The commands are the same as in the console and can be listed using the 'help' command.",
        tags = ["Cli"],
        headers = [OpenApiParam(name = "Authorization", description = "Alias and token provided as basic auth credentials", required = true)],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "Status of the executed command",
                content = [OpenApiContent(from = ExecutionResponse::class)]
            ),
            OpenApiResponse(
                status = "400",
                description = "Error message related to the invalid command format (0 < command length < $MAX_COMMAND_LENGTH)",
                content = [OpenApiContent(from = ErrorResponse::class)]
            ),
            OpenApiResponse(
                status = "401",
                description = "Error message related to the unauthorized access",
                content = [OpenApiContent(from = ErrorResponse::class)]
            )
        ]
    )
    private val executeCommand = Route(ROUTE, POST) {
        context.logger.info("REMOTE EXECUTION ${context.uri} from ${context.address}")

        authenticated {
            if (!isManager()) {
                response = errorResponse(UNAUTHORIZED, "Authenticated user is not a manager")
                return@authenticated
            }

            context.logger.info("${accessToken.alias} (${context.address}) requested command: ${context.body.value}")
            response = consoleFacade.executeCommand(context.body.value)
        }
    }

    override val routes = setOf(executeCommand)

}