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
package org.panda_lang.reposilite.failure.api

import io.javalin.http.HttpCode
import panda.std.Result

data class ErrorResponse(
    val status: Int,
    val message: String
) {

    constructor(code: HttpCode, message: String) : this(code.status, message)

}

fun <V> errorResponse(code: HttpCode, message: String): Result<V, ErrorResponse> =
    Result.error(ErrorResponse(code.status, message))