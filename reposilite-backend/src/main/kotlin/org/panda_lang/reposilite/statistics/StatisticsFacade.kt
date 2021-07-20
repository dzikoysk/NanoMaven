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
package org.panda_lang.reposilite.statistics

import net.dzikoysk.dynamiclogger.Journalist
import net.dzikoysk.dynamiclogger.Logger
import org.panda_lang.reposilite.statistics.api.Record
import org.panda_lang.reposilite.statistics.api.RecordIdentifier
import org.panda_lang.reposilite.statistics.api.RecordType
import java.util.concurrent.ConcurrentHashMap

class StatisticsFacade internal constructor(
    private val journalist: Journalist,
    private val statisticsRepository: StatisticsRepository
) : Journalist {

    private val recordsBulk: ConcurrentHashMap<RecordIdentifier, Long> = ConcurrentHashMap()

    fun increaseRecord(type: RecordType, uri: String) =
        recordsBulk.merge(RecordIdentifier(type, uri), 1) { cached, value -> cached + value }

    fun saveRecordsBulk() =
        recordsBulk.toMap().also {
            recordsBulk.clear() // read doesn't lock, so there is a possibility of dropping a few records between toMap and clear. Might be improved in the future
            statisticsRepository.incrementRecords(it)
            logger.debug("[Statistics] Saved bulk with ${it.size} records")
        }

    fun findRecordsByPhrase(type: RecordType, phrase: String): List<Record> =
        statisticsRepository.findRecordsByPhrase(type, phrase)

    fun countUniqueRecords(): Long =
        statisticsRepository.countUniqueRecords()

    fun countRecords(): Long =
        statisticsRepository.countRecords()

    override fun getLogger(): Logger =
        journalist.logger

}