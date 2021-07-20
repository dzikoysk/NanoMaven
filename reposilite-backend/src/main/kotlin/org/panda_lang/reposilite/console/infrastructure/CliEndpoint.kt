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

import io.javalin.websocket.WsConfig
import io.javalin.websocket.WsConnectContext
import io.javalin.websocket.WsMessageContext
import org.panda_lang.reposilite.auth.AuthenticationFacade
import org.panda_lang.reposilite.console.ConsoleFacade
import org.panda_lang.reposilite.shared.CachedLogger
import org.panda_lang.reposilite.token.api.Permission.MANAGER
import org.panda_lang.reposilite.web.ReposiliteContextFactory
import panda.utilities.StringUtils
import java.util.function.Consumer

private const val AUTHORIZATION_PREFIX = "Authorization:"

internal class CliEndpoint(
    private val contextFactory: ReposiliteContextFactory,
    private val authenticationFacade: AuthenticationFacade,
    private val consoleFacade: ConsoleFacade,
    private val cachedLogger: CachedLogger
) : Consumer<WsConfig> {

    override fun accept(wsConfig: WsConfig) {
        wsConfig.onConnect { connectContext: WsConnectContext ->
            wsConfig.onMessage { authContext: WsMessageContext ->
                val context = contextFactory.create(authContext)
                val authMessage = authContext.message()

                if (!authMessage.startsWith(AUTHORIZATION_PREFIX)) {
                    context.logger.info("CLI | Unauthorized CLI access request from ${context.address} (missing credentials)")
                    connectContext.send("Unauthorized connection request")
                    connectContext.session.disconnect()
                    return@onMessage
                }

                val credentials = StringUtils.replaceFirst(authMessage, AUTHORIZATION_PREFIX, "")
                val auth = authenticationFacade.authenticateByCredentials(credentials)

                if (!auth.isOk || !auth.get().hasPermission(MANAGER)) {
                    context.logger.info("CLI | Unauthorized CLI access request from " + context.address)
                    connectContext.send("Unauthorized connection request")
                    connectContext.session.disconnect()
                    return@onMessage
                }

                val username = "${auth.get().alias}@${context.address}"

                wsConfig.onClose {
                    context.logger.info("CLI | $username closed connection")
                    // remove listener
                }

                // TOFIX: Listen
                // ReposiliteWriter.getConsumers().put(connectContext, connectContext::send)
                context.logger.info("CLI | $username accessed remote console")

                wsConfig.onMessage { messageContext: WsMessageContext ->
                    context.logger.info("CLI | " + username + "> " + messageContext.message())
                    consoleFacade.executeCommand(messageContext.message())
                }

                for (message in cachedLogger.getAllLatestMessages()) {
                    connectContext.send(message) // TOFIX: To JSON
                }
            }
        }
    }

}