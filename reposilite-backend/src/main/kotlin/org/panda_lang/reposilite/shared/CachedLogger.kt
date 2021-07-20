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

import net.dzikoysk.dynamiclogger.Channel
import net.dzikoysk.dynamiclogger.backend.DefaultLogger
import org.apache.commons.collections4.QueueUtils
import org.apache.commons.collections4.queue.CircularFifoQueue
import java.util.*

class CachedLogger(threshold: Channel, cacheSize: Int) : DefaultLogger(threshold) {

    private val latestMessages: Map<Channel, Queue<Pair<Long, String>>> = Channel.getPredefinedChannels().associateWith {
        QueueUtils.synchronizedQueue(CircularFifoQueue(cacheSize))
    }

    override fun internalLog(channel: Channel, message: String) {
        latestMessages[channel]!!.add(Pair(System.nanoTime(), message))
    }

    fun getAllLatestMessages(): List<LogEntry> =
        latestMessages.entries
            .flatMap { (channel, queue) ->
                synchronized(queue) { // synchronized CircularFifoQueue requires synchronized iterations
                    queue.map { LogEntry(it.first, channel, it.second) }
                }
            }
            .associateBy { it.time }
            .toSortedMap()
            .values
            .toList()


    fun getLatestMessages(channel: Channel): List<LogEntry> =
        latestMessages[channel]!!
            .map { LogEntry(it.first, channel, it.second) }
            .toList()

}

data class LogEntry(
    val time: Long,
    val channel: Channel,
    val message: String
)