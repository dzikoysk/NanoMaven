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

package org.panda_lang.reposilite.failure

import net.dzikoysk.dynamiclogger.Journalist
import net.dzikoysk.dynamiclogger.Logger
import panda.std.function.ThrowingRunnable
import panda.utilities.ArrayUtils
import panda.utilities.StringUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService

class FailureFacade internal constructor(private val journalist: Journalist) : Journalist {

    private val exceptions: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun throwException(id: String, throwable: Throwable) {
        logger.error(id)
        logger.exception(throwable)

        exceptions
            .add(arrayOf(
                "failure $id",
                throwException(throwable)
            )
            .joinToString { System.lineSeparator() }
            .trim())
    }

    private fun throwException(throwable: Throwable?): String {
        if (throwable == null) {
            return StringUtils.EMPTY
        }

        return arrayOf(
            "  by ${throwable.javaClass.simpleName}: ${throwable.message}",
            "  at " + ArrayUtils.get(throwable.stackTrace, 0)
                .map { it.toString() }
                .orElseGet("<unknown stacktrace>"),
            throwException(throwable.cause)
        ).joinToString { System.lineSeparator() }
    }

    fun <E : Exception?> ofChecked(runnable: ThrowingRunnable<E>): Runnable {
        return Runnable { run(runnable) }
    }

    fun <E : Exception?> executeChecked(service: ExecutorService, runnable: ThrowingRunnable<E>) {
        service.execute { run(runnable) }
    }

    private fun run(runnable: ThrowingRunnable<*>) {
        try {
            runnable.run()
        } catch (exception: Exception) {
            throwException("Exception occurred during the task execution", exception)
        }
    }

    fun hasFailures() =
        exceptions.isNotEmpty()

    fun getFailures(): Collection<String> =
        exceptions

    override fun getLogger(): Logger =
        journalist.logger

}
