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

import org.panda_lang.reposilite.statistics.StatisticsRepository
import org.panda_lang.reposilite.statistics.api.Record
import org.panda_lang.reposilite.statistics.api.RecordIdentifier
import org.panda_lang.reposilite.statistics.api.RecordType
import java.util.concurrent.ConcurrentHashMap

internal class InMemoryStatisticsRepository : StatisticsRepository {

    private val records: MutableMap<RecordIdentifier, Record> = ConcurrentHashMap()

    override fun incrementRecord(record: RecordIdentifier, count: Long) {
        findRecordByTypeAndIdentifier(record)
            ?.also { records[record] = it.copy(count = it.count + count) }
            ?: createRecord(record, count)
    }

    override fun incrementRecords(bulk: Map<RecordIdentifier, Long>) {
        bulk.forEach { incrementRecord(it.key, it.value) }
    }

    private fun createRecord(identifier: RecordIdentifier, initCount: Long) {
        records[identifier] = Record(
            type = identifier.type,
            identifier = identifier.identifier,
            count = initCount
        )
    }

    override fun findRecordByTypeAndIdentifier(record: RecordIdentifier): Record? =
        records.values.firstOrNull { it.type == record.type && it.identifier == record.identifier }

    override fun findRecordsByPhrase(type: RecordType, phrase: String): List<Record> =
        records.values
            .filter { it.type == type }
            .filter { it.identifier.contains(phrase) }

    override fun countRecords(): Long =
        records
            .map { it.value.count }
            .sum()

    override fun countUniqueRecords(): Long =
        records.size.toLong()

}