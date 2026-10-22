package com.webgdl.cuadricula.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class LifeCalcTest {
    private val birth = LocalDate.of(1996, 5, 14)

    @Test fun birthDayIsWeekZero() = assertEquals(0, LifeCalc.weekIndexOf(birth, birth))

    @Test fun beforeBirthIsMinusOne() =
        assertEquals(-1, LifeCalc.weekIndexOf(birth, birth.minusDays(1)))

    @Test fun sevenDaysIsSecondColumn() =
        assertEquals(1, LifeCalc.weekIndexOf(birth, birth.plusDays(7)))

    @Test fun thirtyYearsCase() =
        assertEquals(30 * 52 + 22, LifeCalc.weekIndexOf(birth, LocalDate.of(2026, 10, 21)))

    @Test fun dayBeforeBirthdayIsLastColumn() =
        assertEquals(51, LifeCalc.weekIndexOf(birth, LocalDate.of(1997, 5, 13)))

    @Test fun nextBirthdayStartsNextRow() =
        assertEquals(52, LifeCalc.weekIndexOf(birth, LocalDate.of(1997, 5, 14)))

    @Test fun leapDayBirthIsConsistent() {
        val leap = LocalDate.of(2000, 2, 29)
        assertEquals(52, LifeCalc.weekIndexOf(leap, LocalDate.of(2001, 2, 28)))
    }

    @Test fun weekStartRoundTrip() {
        for (i in listOf(0, 1, 51, 52, 1582, 4159)) {
            assertEquals(i, LifeCalc.weekIndexOf(birth, LifeCalc.weekStart(birth, i)))
            assertEquals(i, LifeCalc.weekIndexOf(birth, LifeCalc.weekEnd(birth, i)))
        }
    }

    @Test fun totalWeeksFor80Years() =
        assertEquals(4160, LifeCalc.stats(birth, LocalDate.of(2026, 10, 21), 80).totalWeeks)

    @Test fun yearWeightMatchesVideo() {
        assertEquals(10f, LifeCalc.stats(birth, birth.plusYears(10), 80).yearWeightPercent, 0.01f)
        assertEquals(2f, LifeCalc.stats(birth, birth.plusYears(50), 80).yearWeightPercent, 0.01f)
    }

    @Test fun remainingWeeksInsideRange() =
        assertEquals(65 * 52 - 1582, LifeCalc.remainingWeeksInRange(1582, 25, 65))

    @Test fun remainingWeeksBeforeRange() =
        assertEquals((65 - 25) * 52, LifeCalc.remainingWeeksInRange(0, 25, 65))

    @Test fun remainingWeeksAfterRange() =
        assertEquals(0, LifeCalc.remainingWeeksInRange(4000, 25, 65))

    @Test fun layerRowColorsFirstWins() {
        val colors = layerRowColors(
            10,
            listOf(LayerRange(2, 5, 111), LayerRange(4, 8, 222)),
        )
        assertEquals(listOf(0, 0, 111, 111, 111, 222, 222, 222, 0, 0), colors.toList())
    }

    @Test fun tagsAreNormalized() {
        assertEquals("trabajo, amor", normalizeTags(" Trabajo, amor ,TRABAJO,, "))
        assertEquals(emptyList<String>(), parseTags(" , ,"))
    }

    @Test fun beachTwiceAYearAtThirty() =
        // A los 30 años justos (semana 1560), 2 veces al año hasta los 80.
        assertEquals(99, Recurrence.remainingTimes(1560, 2, 0, 80))

    @Test fun untilAgeLimitsRemaining() =
        assertEquals((65 * 52 - 1561) * 12 / 52, Recurrence.remainingTimes(1560, 12, 65, 80))

    @Test fun untilAgeAlreadyPassedIsZero() =
        assertEquals(0, Recurrence.remainingTimes(3500, 12, 60, 80))

    @Test fun occurrencesAreFutureAndInsideRange() {
        val weeks = Recurrence.occurrenceWeeks(1560, 2, 0, 80)
        assertEquals(99, weeks.size)
        assertEquals(true, weeks.all { it > 1560 && it < 80 * 52 })
    }

    @Test fun manyPerWeekCollapseToWeeks() {
        val weeks = Recurrence.occurrenceWeeks(0, 365, 0, 1)
        assertEquals(51, weeks.size)
    }

    @Test fun rarestEventWinsOverlaps() {
        // Caso real: vida de 100 años, semana actual 2583, playa 42/año (primero) y amigos 6/año.
        val marks = computeEventMarks(
            listOf(EventMarkSpec(42, 0, 1), EventMarkSpec(6, 0, 2)),
            currentWeek = 2583,
            lifeExpectancy = 100,
        )
        assertEquals(301, marks.values.count { it == 2 })
        assertEquals(1811, marks.values.count { it == 1 })
    }

    @Test fun equalFrequencyFirstWins() {
        val marks = computeEventMarks(listOf(EventMarkSpec(12, 0, 1), EventMarkSpec(12, 0, 2)), 0, 80)
        assertEquals(setOf(1), marks.values.toSet())
    }

    @Test fun loggedWeeksAreMarked() {
        val marks = computeEventMarks(listOf(EventMarkSpec(2, 0, 7, loggedWeeks = listOf(10, -1))), 2583, 100)
        assertEquals(7, marks[10])
        assertEquals(false, marks.containsKey(-1))
    }
}
