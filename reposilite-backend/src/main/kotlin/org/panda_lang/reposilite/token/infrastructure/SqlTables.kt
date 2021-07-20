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

package org.panda_lang.reposilite.token.infrastructure

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import java.time.LocalDate

typealias Id = EntityID<Int>

object AccessTokenTable : IntIdTable("access_token") {

    val alias: Column<String> = varchar("alias", 255)
    val secret: Column<String> = varchar("secret", 512)
    val createdAt: Column<LocalDate> = date("createdAt")
    val description: Column<String> = text("description")

    init {
        uniqueIndex(alias)
    }

}

object PermissionToAccessTokenTable : Table("permission_access_token") {

    val accessTokenId: Column<Id> = reference("access_token_id", AccessTokenTable.id, onDelete = CASCADE, onUpdate = CASCADE)
    val permission: Column<String> = varchar("permission", 64)

    init {
        index(columns = arrayOf(accessTokenId))
        uniqueIndex(accessTokenId, permission)
    }

}

object PermissionToRouteTable : Table("permission_route") {

    val accessTokenId: Column<Id> = reference("access_token_id", AccessTokenTable.id, onDelete = CASCADE, onUpdate = CASCADE)
    val route: Column<String> = varchar("path", 2048)
    val permission: Column<String> = varchar("permission", 64)

    init {
        index(columns = arrayOf(accessTokenId, route))
        uniqueIndex(accessTokenId, route, permission)
    }

}