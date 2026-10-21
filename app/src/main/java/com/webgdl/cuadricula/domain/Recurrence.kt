package com.webgdl.cuadricula.domain

import kotlin.math.max
import kotlin.math.min

/** Cálculos para eventos que se repiten N veces al año. */
object Recurrence {

    /** Semana (exclusiva) en que deja de contar: a [untilAge] años, o al final de la vida si es 0. */
    fun endWeek(untilAge: Int, lifeExpectancy: Int): Int {
        val age = if (untilAge <= 0) lifeExpectancy else min(untilAge, lifeExpectancy)
        return age * WEEKS_PER_ROW
    }

    /** Veces que quedan, contando desde la semana siguiente a [currentWeek]. */
    fun remainingTimes(currentWeek: Int, timesPerYear: Int, untilAge: Int, lifeExpectancy: Int): Int {
        if (timesPerYear <= 0) return 0
        val weeks = max(0, endWeek(untilAge, lifeExpectancy) - (currentWeek + 1))
        return (weeks.toLong() * timesPerYear / WEEKS_PER_ROW).toInt()
    }

    /**
     * Semanas futuras donde caería cada una de las veces que quedan, repartidas de forma pareja
     * (como las "idas al mar" del video). Si hay más de una por semana, la semana aparece una vez.
     */
    fun occurrenceWeeks(currentWeek: Int, timesPerYear: Int, untilAge: Int, lifeExpectancy: Int): Set<Int> {
        val n = remainingTimes(currentWeek, timesPerYear, untilAge, lifeExpectancy)
        val end = endWeek(untilAge, lifeExpectancy)
        val result = HashSet<Int>()
        for (i in 0 until n) {
            val week = currentWeek + 1 + (i.toLong() * WEEKS_PER_ROW / timesPerYear).toInt()
            if (week < end) result.add(week)
        }
        return result
    }
}
