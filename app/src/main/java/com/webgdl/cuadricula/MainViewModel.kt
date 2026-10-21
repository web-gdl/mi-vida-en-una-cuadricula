package com.webgdl.cuadricula

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.webgdl.cuadricula.data.LastTime
import com.webgdl.cuadricula.data.UserSettings
import com.webgdl.cuadricula.data.WeekEntry
import com.webgdl.cuadricula.reminder.ReminderScheduler
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val loaded: Boolean = false,
    val settings: UserSettings = UserSettings(),
    val entries: Map<Int, WeekEntry> = emptyMap(),
    val lastTimes: List<LastTime> = emptyList(),
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val cuadricula = app as CuadriculaApp
    private val settingsRepo = cuadricula.settingsRepository
    private val dao = cuadricula.database.weekDao()
    private val lastTimeDao = cuadricula.database.lastTimeDao()

    val state: StateFlow<UiState> = combine(
        settingsRepo.settings,
        dao.observeAll(),
        lastTimeDao.observeAll(),
    ) { s, list, lastTimes ->
        UiState(
            loaded = true,
            settings = s,
            entries = list.associateBy { it.weekIndex },
            lastTimes = lastTimes,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun saveProfile(birth: LocalDate, lifeExpectancy: Int) =
        viewModelScope.launch { settingsRepo.saveProfile(birth, lifeExpectancy) }

    fun setBirth(birth: LocalDate) = viewModelScope.launch { settingsRepo.setBirth(birth) }

    fun setLifeExpectancy(years: Int) = viewModelScope.launch { settingsRepo.setLifeExpectancy(years) }

    fun setReminder(enabled: Boolean) = viewModelScope.launch {
        settingsRepo.setReminder(enabled)
        if (enabled) ReminderScheduler.schedule(cuadricula) else ReminderScheduler.cancel(cuadricula)
    }

    fun saveEntry(entry: WeekEntry) = viewModelScope.launch { dao.upsert(entry) }

    fun deleteEntry(weekIndex: Int) = viewModelScope.launch { dao.delete(weekIndex) }

    fun addLastTime(title: String, date: LocalDate) = viewModelScope.launch {
        lastTimeDao.insert(
            LastTime(title = title.trim(), epochDay = date.toEpochDay(), createdAt = System.currentTimeMillis())
        )
    }

    fun deleteLastTime(id: Long) = viewModelScope.launch { lastTimeDao.delete(id) }
}
