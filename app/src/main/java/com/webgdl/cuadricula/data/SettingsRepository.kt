package com.webgdl.cuadricula.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

const val DEFAULT_LIFE_EXPECTANCY = 80
const val MIN_LIFE_EXPECTANCY = 30
const val MAX_LIFE_EXPECTANCY = 120

data class UserSettings(
    /** Fecha de nacimiento como epochDay; null = aún no configurada (onboarding). */
    val birthEpochDay: Long? = null,
    val lifeExpectancy: Int = DEFAULT_LIFE_EXPECTANCY,
    val reminderEnabled: Boolean = false,
    val showLayers: Boolean = true,
) {
    val birthDate: LocalDate? get() = birthEpochDay?.let { LocalDate.ofEpochDay(it) }
}

/**
 * Ajustes guardados con SharedPreferences (incluido en Android): evita sumar DataStore y
 * protobuf al APK. Expone un Flow que emite cada vez que cambia un valor.
 */
class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = prefsOf(context)

    val settings: Flow<UserSettings> = callbackFlow {
        trySend(read())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(read()) }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.conflate()

    private fun read() = UserSettings(
        birthEpochDay = if (prefs.contains(KEY_BIRTH)) prefs.getLong(KEY_BIRTH, 0L) else null,
        lifeExpectancy = prefs.getInt(KEY_LIFE, DEFAULT_LIFE_EXPECTANCY)
            .coerceIn(MIN_LIFE_EXPECTANCY, MAX_LIFE_EXPECTANCY),
        reminderEnabled = prefs.getBoolean(KEY_REMINDER, false),
        showLayers = prefs.getBoolean(KEY_SHOW_LAYERS, true),
    )

    fun saveProfile(birth: LocalDate, lifeExpectancy: Int) {
        prefs.edit()
            .putLong(KEY_BIRTH, birth.toEpochDay())
            .putInt(KEY_LIFE, lifeExpectancy.coerceIn(MIN_LIFE_EXPECTANCY, MAX_LIFE_EXPECTANCY))
            .apply()
    }

    fun setBirth(birth: LocalDate) {
        prefs.edit().putLong(KEY_BIRTH, birth.toEpochDay()).apply()
    }

    fun setLifeExpectancy(years: Int) {
        prefs.edit().putInt(KEY_LIFE, years.coerceIn(MIN_LIFE_EXPECTANCY, MAX_LIFE_EXPECTANCY)).apply()
    }

    fun setReminder(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER, enabled).apply()
    }

    fun setShowLayers(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_LAYERS, show).apply()
    }

    companion object {
        private const val NAME = "settings"
        private const val KEY_BIRTH = "birth_epoch_day"
        private const val KEY_LIFE = "life_expectancy"
        private const val KEY_REMINDER = "reminder_enabled"
        private const val KEY_SHOW_LAYERS = "show_layers"

        private fun prefsOf(context: Context): SharedPreferences =
            context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

        /** Lectura directa para receptores que corren sin la app abierta (p. ej. al reiniciar). */
        fun reminderEnabled(context: Context): Boolean = prefsOf(context).getBoolean(KEY_REMINDER, false)
    }
}
