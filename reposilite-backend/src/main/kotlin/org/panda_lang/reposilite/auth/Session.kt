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
package org.panda_lang.reposilite.auth

import org.panda_lang.reposilite.token.api.AccessToken
import org.panda_lang.reposilite.token.api.Permission
import java.nio.file.Path

data class Session internal constructor(
    val path: String,
    val method: SessionMethod,
    val address: String,
    val accessToken: AccessToken,
    val availableResources: List<Path>
) {

    companion object {
        val METHOD_PERMISSIONS = mapOf(
            SessionMethod.HEAD to Permission.READ,
            SessionMethod.GET to Permission.READ,
            SessionMethod.PUT to Permission.WRITE,
            SessionMethod.POST to Permission.WRITE,
            SessionMethod.DELETE to Permission.WRITE
        )
    }

    fun isAuthorized() =
        isManager() || accessToken.hasPermissionTo(path, METHOD_PERMISSIONS[method]!!)

    fun isManager() =
        accessToken.hasPermission(Permission.MANAGER)

    fun getSessionIdentifier() =
        "${accessToken.alias}@$address"

}