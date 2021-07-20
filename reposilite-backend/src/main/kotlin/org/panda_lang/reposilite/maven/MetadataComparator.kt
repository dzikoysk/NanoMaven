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
package org.panda_lang.reposilite.maven

import java.math.BigInteger
import java.util.function.Function
import java.util.function.Predicate
import kotlin.math.max

internal class MetadataComparator<T>(
    private val originalValue: Function<T, String>,
    private val cachedValue: Function<T, Array<String>>,
    private val isDirectory: Predicate<T>
) : Comparator<T> {

    override fun compare(metadata: T, toMetadata: T): Int {
        if (isDirectory.test(metadata) && !isDirectory.test(toMetadata)) {
            return -1
        }
        else if (isDirectory.test(toMetadata) && !isDirectory.test(metadata)) {
            return 1
        }

        val data = cachedValue.apply(metadata)
        val toData = cachedValue.apply(toMetadata)

        for (index in 0 until max(data.size, toData.size)) {
            val fragment = if (index < data.size) data[index] else "0"
            val toFragment = if (index < toData.size) toData[index] else "0"
            var value: Int

            // number to string
            if (isDigit(fragment)) {
                value = 1

                // number to number
                if (isDigit(toFragment)) {
                    value = try {
                        fragment.toLong().compareTo(toFragment.toLong())
                    }
                    catch (numberFormatException: NumberFormatException) {
                        BigInteger(fragment).compareTo(BigInteger(toFragment))
                    }
                }
            }
            else if (isDigit(toFragment)) {
                value = -1
            }
            else {
                value = -fragment.compareTo(toFragment)
            }

            if (value != 0) {
                return -value
            }
        }

        return -originalValue.apply(metadata).compareTo(originalValue.apply(toMetadata))
    }

    private fun isDigit(string: String): Boolean =
        !string.toCharArray().any { !Character.isDigit(it) }

}