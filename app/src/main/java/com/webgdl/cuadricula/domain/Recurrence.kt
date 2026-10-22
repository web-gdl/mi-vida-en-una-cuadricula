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

/** Colores de las marcas de eventos (ARGB): blanco, cian, amarillo, rosa. */
val EVENT_COLORS: List<Int> = listOf(
    0xFFFFFFFF.toInt(),
    0xFF00E5FF.toInt(),
    0xFFFFEB3B.toInt(),
    0xFFFF80AB.toInt(),
)

fun eventColorArgb(index: Int): Int = EVENT_COLORS[index % EVENT_COLORS.size]

/** Un evento periódico a dibujar en la cuadrícula. */
data class EventMarkSpec(
    val timesPerYear: Int,
    val untilAge: Int,
    val color: Int,
    /** Semanas (índices) en que se registró que ocurrió; las negativas se ignoran. */
    val loggedWeeks: List<Int> = emptyList(),
)

/**
 * Semana -> color de la marca: las veces que quedan (futuro) y las registradas (pasado).
 * Si dos eventos caen en la misma semana gana el MENOS frecuente, para que un evento raro
 * (6 reuniones al año) no quede tapado por uno frecuente (42 idas a la playa al año).
 * A igual frecuencia gana el primero de la lista.
 */
fun computeEventMarks(specs: List<EventMarkSpec>, currentWeek: Int, lifeExpectancy: Int): Map<Int, Int> {
    val marks = HashMap<Int, Int>()
    specs.withIndex()
        .sortedWith(
            compareByDescending<IndexedValue<EventMarkSpec>> { it.value.timesPerYear }
                .thenByDescending { it.index }
        )
        .forEach { (_, spec) ->
            Recurrence.occurrenceWeeks(currentWeek, spec.timesPerYear, spec.untilAge, lifeExpectancy)
                .forEach { marks[it] = spec.color }
            spec.loggedWeeks.forEach { if (it >= 0) marks[it] = spec.color }
        }
    return marks
}
