package org.panda_lang.reposilite.statistics

import net.dzikoysk.dynamiclogger.backend.InMemoryLogger
import org.panda_lang.reposilite.statistics.api.RecordType
import org.panda_lang.reposilite.statistics.infrastructure.InMemoryStatisticsRepository

internal open class StatisticsSpec {

    protected val logger = InMemoryLogger()
    protected val statisticsFacade = StatisticsFacade(logger, InMemoryStatisticsRepository())

    protected fun increaseAndSave(type: RecordType, identifier: String) {
        statisticsFacade.increaseRecord(type, identifier)
        statisticsFacade.saveRecordsBulk()
    }

}