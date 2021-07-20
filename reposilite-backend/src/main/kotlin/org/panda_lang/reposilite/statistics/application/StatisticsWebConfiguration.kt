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

package org.panda_lang.reposilite.statistics.application

import net.dzikoysk.dynamiclogger.Journalist
import org.panda_lang.reposilite.console.ConsoleFacade
import org.panda_lang.reposilite.statistics.StatisticsFacade
import org.panda_lang.reposilite.statistics.StatsCommand
import org.panda_lang.reposilite.statistics.infrastructure.SqlStatisticsRepository
import org.panda_lang.reposilite.statistics.infrastructure.StatisticsHandler
import org.panda_lang.reposilite.web.api.Routes
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MINUTES

internal object StatisticsWebConfiguration {

    fun createFacade(journalist: Journalist): StatisticsFacade =
        StatisticsFacade(journalist, SqlStatisticsRepository())

    fun initialize(statisticsFacade: StatisticsFacade, consoleFacade: ConsoleFacade) {
        val scheduler = Executors.newSingleThreadScheduledExecutor() // Maybe use some shared ThreadPool to avoid Thread creation
        scheduler.scheduleWithFixedDelay({ statisticsFacade.saveRecordsBulk() }, 1, 1, MINUTES)

        consoleFacade.registerCommand(StatsCommand(statisticsFacade))
    }

    fun routing(statisticsFacade: StatisticsFacade): Set<Routes> =
        setOf(
            StatisticsHandler(statisticsFacade)
        )

}