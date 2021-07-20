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
package org.panda_lang.reposilite.web

import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.http.HttpCode.UNAUTHORIZED
import io.javalin.websocket.WsContext
import net.dzikoysk.dynamiclogger.Journalist
import org.panda_lang.reposilite.auth.AuthenticationFacade
import org.panda_lang.reposilite.auth.SessionMethod
import org.panda_lang.reposilite.failure.api.ErrorResponse
import org.panda_lang.reposilite.failure.api.errorResponse
import org.panda_lang.reposilite.maven.MetadataUtils
import panda.std.Result
import panda.std.Result.ok

@Suppress("MoveLambdaOutsideParentheses")
class ReposiliteContextFactory internal constructor(
    private val journalist: Journalist,
    private val forwardedIpHeader: String,
    private val authenticationFacade: AuthenticationFacade
) {

    fun create(context: Context): Result<ReposiliteContext, ErrorResponse> {
        val normalizedUri = MetadataUtils.normalizeUri(context.req.requestURI) ?: return errorResponse(HttpCode.BAD_REQUEST, "Invalid url")
        val host = context.header(forwardedIpHeader) ?: context.req.remoteAddr

        val session = authenticationFacade.authenticateByHeader(context.headerMap())
            .map {
                authenticationFacade.createSession(normalizedUri, SessionMethod.valueOf(context.method().toUpperCase()), host, it)
            }

        return ok(ReposiliteContext(
            journalist,
            normalizedUri,
            context.method(),
            host,
            context.headerMap(),
            session,
            lazy { context.body() },
            { context.req.inputStream }
        ))
    }

    fun create(context: WsContext): ReposiliteContext {
        return ReposiliteContext(
            journalist,
            context.host(),
            "SOCKET",
            context.header(forwardedIpHeader) ?: context.session.remoteAddress.toString(),
            context.headerMap(),
            errorResponse(UNAUTHORIZED, "WebSocket based context does not support sessions"),
            lazy { throw UnsupportedOperationException("WebSocket based context does not support input stream") },
            { throw UnsupportedOperationException("WebSocket based context does not support input stream") }
        )
    }

}