package com.webgdl.cuadricula.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

/** Columnas de la cuadrícula: una fila = un año = 52 semanas, como en el video. */
const val WEEKS_PER_ROW = 52

/** Pasos por minuto usados en el video para convertir semanas en minutos de caminata. */
const val STEPS_PER_MINUTE = 120

data class LifeStats(
    val ageYears: Int,
    /** Índice absoluto (fila * 52 + columna) de la semana en curso. */
    val currentWeek: Int,
    val totalWeeks: Int,
    /** Semanas completas ya vividas (sin contar la actual). */
    val weeksLived: Int,
    /** Semanas que quedan sin contar la actual. */
    val weeksRemaining: Int,
    val percentLived: Float,
    val yearsRemaining: Int,
    /** Cuánto pesa un año hoy: 100 / edad (a los 10 años = 10 %, a los 50 = 2 %). */
    val yearWeightPercent: Float,
    val remainingMinutesAsSteps: Int,
)

object LifeCalc {

    /**
     * Índice absoluto de la semana que contiene [date], o -1 si es anterior al nacimiento.
     * fila = años cumplidos; columna = semanas desde el último cumpleaños (máx. 51, de modo
     * que los 1-2 días sobrantes del año se absorben en la última columna).
     */
    fun weekIndexOf(birth: LocalDate, date: LocalDate): Int {
        if (date.isBefore(birth)) return -1
        var years = ChronoUnit.YEARS.between(birth, date).toInt()
        // Ajuste para nacidos el 29 de febrero y otros bordes de plusYears.
        while (!birth.plusYears((years + 1).toLong()).isAfter(date)) years++
        while (birth.plusYears(years.toLong()).isAfter(date)) years--
        val days = ChronoUnit.DAYS.between(birth.plusYears(years.toLong()), date)
        val col = min((WEEKS_PER_ROW - 1).toLong(), days / 7).toInt()
        return years * WEEKS_PER_ROW + col
    }

    /** Primer día de la semana con índice [index]. */
    fun weekStart(birth: LocalDate, index: Int): LocalDate {
        val row = index / WEEKS_PER_ROW
        val col = index % WEEKS_PER_ROW
        return birth.plusYears(row.toLong()).plusDays(col * 7L)
    }

    /** Último día de la semana [index]; la última columna llega hasta el día antes del siguiente cumpleaños. */
    fun weekEnd(birth: LocalDate, index: Int): LocalDate {
        val col = index % WEEKS_PER_ROW
        return if (col == WEEKS_PER_ROW - 1) {
            birth.plusYears((index / WEEKS_PER_ROW + 1).toLong()).minusDays(1)
        } else {
            weekStart(birth, index).plusDays(6)
        }
    }

    fun stats(birth: LocalDate, today: LocalDate, lifeExpectancyYears: Int): LifeStats {
        val total = lifeExpectancyYears * WEEKS_PER_ROW
        val current = max(0, weekIndexOf(birth, today))
        val age = current / WEEKS_PER_ROW
        val lived = min(current, total)
        val remaining = max(0, total - current - 1)
        return LifeStats(
            ageYears = age,
            currentWeek = current,
            totalWeeks = total,
            weeksLived = lived,
            weeksRemaining = remaining,
            percentLived = if (total == 0) 0f else min(1f, lived.toFloat() / total),
            yearsRemaining = remaining / WEEKS_PER_ROW,
            yearWeightPercent = 100f / max(1, age),
            remainingMinutesAsSteps = remaining / STEPS_PER_MINUTE,
        )
    }
}
