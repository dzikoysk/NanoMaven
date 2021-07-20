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

package org.panda_lang.reposilite.shared

import net.dzikoysk.exposed.shared.IdentifiableEntity
import net.dzikoysk.exposed.shared.UNINITIALIZED_ENTITY_ID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun <T> transactionUnit(db: Database? = null, statement: Transaction.() -> T) {
    transaction(db, statement)
}

fun <TABLE : Table> TABLE.insertOrUpdate(identifiableEntity: IdentifiableEntity, where: (SqlExpressionBuilder.() -> Op<Boolean>)?, body: TABLE.(UpdateBuilder<Number>) -> Unit) {
    when (identifiableEntity.id) {
        UNINITIALIZED_ENTITY_ID -> insert(body)
        else -> update(where = where, body = body)
    }
}

fun <T, R> Iterable<T>.firstAndMap(transform: (T) -> R): R? =
    this.firstOrNull()?.let(transform)