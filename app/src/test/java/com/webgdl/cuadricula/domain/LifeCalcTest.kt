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
}
