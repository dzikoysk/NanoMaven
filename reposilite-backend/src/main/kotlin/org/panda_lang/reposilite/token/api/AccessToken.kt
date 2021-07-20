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
package org.panda_lang.reposilite.token.api

import net.dzikoysk.exposed.shared.IdentifiableEntity
import net.dzikoysk.exposed.shared.UNINITIALIZED_ENTITY_ID
import java.time.LocalDate

data class AccessToken internal constructor(
    override val id: Int = UNINITIALIZED_ENTITY_ID,
    val alias: String,
    val secret: String,
    val createdAt: LocalDate = LocalDate.now(),
    val description: String = "",
    val permissions: Collection<Permission> = emptyList(),
    val routes: Collection<Route> = emptyList()
) : IdentifiableEntity {

    fun hasPermission(permission: Permission): Boolean =
        permissions.contains(permission)

    fun hasPermissionTo(toPath: String, routePermission: Permission): Boolean =
        routes.any { it.hasPermissionTo(toPath, routePermission) }

}