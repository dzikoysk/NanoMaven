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

package org.panda_lang.reposilite.statistics.infrastructure

import net.dzikoysk.exposed.upsert.upsert
import org.jetbrains.exposed.sql.Op.Companion.build
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction
import org.panda_lang.reposilite.shared.firstAndMap
import org.panda_lang.reposilite.shared.transactionUnit
import org.panda_lang.reposilite.statistics.StatisticsRepository
import org.panda_lang.reposilite.statistics.api.MAX_IDENTIFIER_LENGTH
import org.panda_lang.reposilite.statistics.api.Record
import org.panda_lang.reposilite.statistics.api.RecordIdentifier
import org.panda_lang.reposilite.statistics.api.RecordType

internal class SqlStatisticsRepository : StatisticsRepository {

    init {
        transaction {
            SchemaUtils.create(StatisticsTable)
        }
    }

    override fun incrementRecord(record: RecordIdentifier, count: Long) =
        transactionUnit {
            rawIncrementRecord(record, count)
        }

    override fun incrementRecords(bulk: Map<RecordIdentifier, Long>) =
        transactionUnit {
            bulk.forEach { rawIncrementRecord(it.key, it.value) }
        }

    private fun rawIncrementRecord(record: RecordIdentifier, count: Long) {
        if (record.identifier.length > MAX_IDENTIFIER_LENGTH) {
            throw UnsupportedOperationException("Identifier '${record.identifier}' exceeds allowed length")
        }

        StatisticsTable.upsert(conflictIndex = StatisticsTable.statisticsTypeWithIdentifierKey,
            insertBody = {
                it[this.type] = record.type.name
                it[this.identifier] = record.identifier
                it[this.count] = count
            },
            updateBody = {
                with(SqlExpressionBuilder) {
                    it[StatisticsTable.count] = StatisticsTable.count + count
                }
            }
        )
    }

    private fun toRecord(row: ResultRow) =
        Record(
            RecordType.valueOf(row[StatisticsTable.type].toUpperCase()),
            row[StatisticsTable.identifier],
            row[StatisticsTable.count]
        )

    override fun findRecordByTypeAndIdentifier(record: RecordIdentifier): Record? =
        transaction {
            StatisticsTable.select { build { StatisticsTable.type eq record.type.name }.and { StatisticsTable.identifier eq record.identifier } }
                .firstAndMap { toRecord(it) }
        }

    override fun findRecordsByPhrase(type: RecordType, phrase: String): List<Record> =
        transaction {
            StatisticsTable.select { build { StatisticsTable.type eq type.name }.and { StatisticsTable.identifier like "%${phrase}%" }}
                .map { toRecord(it) }
        }

    override fun countRecords(): Long =
        transaction {
            with (StatisticsTable.count.sum()) {
                StatisticsTable.slice(this).selectAll().firstAndMap { it[this] }
            }
            ?: 0
        }

    override fun countUniqueRecords(): Long =
        transaction {
            StatisticsTable.selectAll().count()
        }

}