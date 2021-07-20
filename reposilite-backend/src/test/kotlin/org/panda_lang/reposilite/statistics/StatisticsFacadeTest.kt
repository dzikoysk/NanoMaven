package org.panda_lang.reposilite.statistics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.panda_lang.reposilite.statistics.api.RecordType.REQUEST
import org.panda_lang.reposilite.statistics.api.RecordType.UNKNOWN

internal class StatisticsFacadeTest : StatisticsSpec() {

    @Test
    fun `should increase records after saving the bulk`() {
        // given: an uri to request
        val uri = "/panda-lang/reposilite"

        // when: the given uri is requested twice
        increaseAndSave(REQUEST, uri)
        increaseAndSave(REQUEST, uri)

        // then: it should be properly stored in repository as a single record
        val records = statisticsFacade.findRecordsByPhrase(REQUEST, uri)
        assertEquals(1, records.size)
        assertEquals(2, records[0].count)
    }

    @Test
    fun `should find record by given phrase`() {
        // given: a requested uri and a phrase to search for
        val uri = "/panda-lang/reposilite"
        val phrase = "reposilite"

        // when: the uri with the given phrase is requested
        increaseAndSave(REQUEST, uri)

        // then: the phrase should be found
        val result = statisticsFacade.findRecordsByPhrase(REQUEST, phrase)
        assertEquals(1, result.size)
        assertEquals(uri, result[0].identifier)
    }

    @Test
    fun `should properly count records and unique records`() {
        // given: two different identifiers
        val first = "/first"
        val second = "/first/second"

        // when: the given identifiers are requested
        increaseAndSave(REQUEST, first)
        increaseAndSave(REQUEST, first)
        increaseAndSave(REQUEST, second)
        increaseAndSave(UNKNOWN, second)

        // then: count should properly respect criteria of uniqueness (type & identifier)
        assertEquals(4, statisticsFacade.countRecords())
        assertEquals(3, statisticsFacade.countUniqueRecords())
    }

}