package com.webgdl.cuadricula.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

const val DEFAULT_LIFE_EXPECTANCY = 80
const val MIN_LIFE_EXPECTANCY = 30
const val MAX_LIFE_EXPECTANCY = 120

data class UserSettings(
    /** Fecha de nacimiento como epochDay; null = aún no configurada (onboarding). */
    val birthEpochDay: Long? = null,
    val lifeExpectancy: Int = DEFAULT_LIFE_EXPECTANCY,
    val reminderEnabled: Boolean = false,
) {
    val birthDate: LocalDate? get() = birthEpochDay?.let { LocalDate.ofEpochDay(it) }
}

class SettingsRepository(private val context: Context) {
    private object Keys {
        val BIRTH = longPreferencesKey("birth_epoch_day")
        val LIFE = intPreferencesKey("life_expectancy")
        val REMINDER = booleanPreferencesKey("reminder_enabled")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            birthEpochDay = p[Keys.BIRTH],
            lifeExpectancy = (p[Keys.LIFE] ?: DEFAULT_LIFE_EXPECTANCY)
                .coerceIn(MIN_LIFE_EXPECTANCY, MAX_LIFE_EXPECTANCY),
            reminderEnabled = p[Keys.REMINDER] ?: false,
        )
    }

    suspend fun saveProfile(birth: LocalDate, lifeExpectancy: Int) {
        context.dataStore.edit {
            it[Keys.BIRTH] = birth.toEpochDay()
            it[Keys.LIFE] = lifeExpectancy.coerceIn(MIN_LIFE_EXPECTANCY, MAX_LIFE_EXPECTANCY)
        }
    }

    suspend fun setBirth(birth: LocalDate) {
        context.dataStore.edit { it[Keys.BIRTH] = birth.toEpochDay() }
    }

    suspend fun setLifeExpectancy(years: Int) {
        context.dataStore.edit {
            it[Keys.LIFE] = years.coerceIn(MIN_LIFE_EXPECTANCY, MAX_LIFE_EXPECTANCY)
        }
    }

    suspend fun setReminder(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMINDER] = enabled }
    }
}
