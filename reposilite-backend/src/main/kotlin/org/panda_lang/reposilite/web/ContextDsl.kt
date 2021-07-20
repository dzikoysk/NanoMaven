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
import org.panda_lang.reposilite.auth.Session
import org.panda_lang.reposilite.failure.api.ErrorResponse
import panda.std.Result

class ContextDsl(val ctx: Context, val context: ReposiliteContext) {

    /**
     * JSON response to send at the end of the dsl call
     */
    var response: Result<out Any?, ErrorResponse>? = null

    /**
     * Request was created by either anonymous user or through authenticated token
     */
    fun accessed(init: Session?.() -> Unit) {
        init(context.session.orElseGet { null })
    }

    /**
     * Request was created by valid access token
     */
    fun authenticated(init: Session.() -> Unit) {
        context.session
            .onError { ctx.error(it) }
            .peek { init(it) }
    }

    /**
     * Request was created by valid access token and the token has access to the requested path
     */
    fun authorized(init: Session.() -> Unit) {
        authenticated {
            if (isAuthorized()) {
                init(this)
            } else {
                ctx.error(ErrorResponse(HttpCode.UNAUTHORIZED, ""))
            }
        }
    }

    /**
     * Get first available splat or empty string
     */
    fun wildcard(defaultValue: String = ""): String =
        ctx.splat(0)
            .takeIf { it?.isNotEmpty() ?: false }
            ?: defaultValue

    fun parameter(name: String): String =
        ctx.pathParam(name)

    internal fun handleResult(result: Result<out Any?, ErrorResponse>?) {
        result
            ?.mapErr { ctx.json(it) }
            ?.map { it?.let { ctx.json(it) } }
    }

}

fun context(contextFactory: ReposiliteContextFactory, ctx: Context, init: ContextDsl.() -> Unit) {
    contextFactory.create(ctx)
        .onError { ctx.json(it) }
        .map { ContextDsl(ctx, it) }
        .peek { init(it) }
        .peek { it.handleResult(it.response) }
}
