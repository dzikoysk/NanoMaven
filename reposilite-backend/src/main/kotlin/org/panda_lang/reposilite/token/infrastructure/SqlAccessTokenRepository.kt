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

import net.dzikoysk.exposed.shared.UNINITIALIZED_ENTITY_ID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.panda_lang.reposilite.shared.firstAndMap
import org.panda_lang.reposilite.shared.transactionUnit
import org.panda_lang.reposilite.token.AccessTokenRepository
import org.panda_lang.reposilite.token.api.AccessToken
import org.panda_lang.reposilite.token.api.Permission
import org.panda_lang.reposilite.token.api.PermissionType.ACCESS_TOKEN
import org.panda_lang.reposilite.token.api.PermissionType.ROUTE
import org.panda_lang.reposilite.token.api.Route

internal class SqlAccessTokenRepository : AccessTokenRepository {

    init {
        transaction {
            SchemaUtils.create(AccessTokenTable, PermissionToAccessTokenTable, PermissionToRouteTable)
        }
    }

    override fun saveAccessToken(accessToken: AccessToken) =
        transactionUnit {
            AccessTokenTable.deleteWhere { AccessTokenTable.id eq accessToken.id }

            AccessTokenTable.insert {
                if (accessToken.id != UNINITIALIZED_ENTITY_ID) {
                    it[this.id] = accessToken.id
                }

                it[this.alias] = accessToken.alias
                it[this.secret] = accessToken.secret
            }

            accessToken.permissions.forEach { permission ->
                PermissionToAccessTokenTable.insert {
                    it[this.accessTokenId] = accessToken.id
                    it[this.permission] = permission.name
                }
            }

            accessToken.routes.forEach { route ->
                PermissionToRouteTable.insert {
                    it[this.accessTokenId] = accessToken.id
                    it[this.route] = route.path
                    it[this.permission] = permission.name
                }
            }
        }

    override fun deleteAccessToken(accessToken: AccessToken) =
        transactionUnit { AccessTokenTable.deleteWhere { AccessTokenTable.id eq accessToken.id } }

    private fun findAccessTokenPermissionsById(id: Int): Collection<Permission> =
        PermissionToAccessTokenTable.select { PermissionToAccessTokenTable.accessTokenId eq id }
            .map { Permission.of(ACCESS_TOKEN, it[PermissionToAccessTokenTable.permission]) }

    private fun findRoutesById(id: Int): Collection<Route> =
        PermissionToRouteTable.select { PermissionToRouteTable.accessTokenId eq id }
            .map { Pair(it[PermissionToRouteTable.route], it[PermissionToRouteTable.permission]) }
            .groupBy { it.first }
            .map { (route, permissions) -> Route(route, permissions.map { Permission.of(ROUTE, it.second) }) }

    private fun toAccessToken(result: ResultRow): AccessToken =
        result[AccessTokenTable.id].value.let { accessTokenId ->
            AccessToken(
                accessTokenId,
                result[AccessTokenTable.alias],
                result[AccessTokenTable.secret],
                result[AccessTokenTable.createdAt],
                result[AccessTokenTable.description],
                findAccessTokenPermissionsById(accessTokenId),
                findRoutesById(accessTokenId)
            )
        }

    override fun findAccessTokenByAlias(alias: String): AccessToken? =
        transaction { AccessTokenTable.select { AccessTokenTable.alias eq alias }.firstAndMap { toAccessToken(it) } }

    override fun findAll(): Collection<AccessToken> =
        transaction { AccessTokenTable.selectAll().map { toAccessToken(it) } }

    override fun countAccessTokens(): Long =
        transaction { AccessTokenTable.selectAll().count() }

}