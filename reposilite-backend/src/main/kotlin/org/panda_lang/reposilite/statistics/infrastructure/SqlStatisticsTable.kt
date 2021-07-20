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

import net.dzikoysk.exposed.upsert.withUnique
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.panda_lang.reposilite.statistics.api.MAX_IDENTIFIER_LENGTH

internal object StatisticsTable : Table("statistics") {

    val type: Column<String> = varchar("type", 24)
    val identifier: Column<String> = varchar("identifier", MAX_IDENTIFIER_LENGTH)
    val count: Column<Long> = long("count")

    val statisticsTypeIdx = index("statistics_type_idx", columns = arrayOf(type))
    val statisticsTypeWithIdentifierKey = withUnique("statistics_type_identifier_key", type, identifier)

}
