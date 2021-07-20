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

import java.util.*

object TimeUtils {

    fun getUptime(uptime: Long): Double {
        val current = System.currentTimeMillis() - uptime
        return current / 1000.0
    }

    fun format(time: Double): String {
        return String.format(Locale.US, "%.2f", time)
    }

}