package com.webgdl.cuadricula

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.webgdl.cuadricula.data.LastTime
import com.webgdl.cuadricula.data.Person
import com.webgdl.cuadricula.data.UserSettings
import com.webgdl.cuadricula.data.WeekEntry
import com.webgdl.cuadricula.reminder.ReminderScheduler
import com.webgdl.cuadricula.widget.GridWidget
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
    val people: List<Person> = emptyList(),
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val cuadricula = app as CuadriculaApp
    private val settingsRepo = cuadricula.settingsRepository
    private val dao = cuadricula.database.weekDao()
    private val lastTimeDao = cuadricula.database.lastTimeDao()
    private val personDao = cuadricula.database.personDao()

    val state: StateFlow<UiState> = combine(
        settingsRepo.settings,
        dao.observeAll(),
        lastTimeDao.observeAll(),
        personDao.observeAll(),
    ) { s, list, lastTimes, people ->
        UiState(
            loaded = true,
            settings = s,
            entries = list.associateBy { it.weekIndex },
            lastTimes = lastTimes,
            people = people,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun saveProfile(birth: LocalDate, lifeExpectancy: Int) =
        viewModelScope.launch {
            settingsRepo.saveProfile(birth, lifeExpectancy)
            GridWidget.updateAll(cuadricula)
        }

    fun setBirth(birth: LocalDate) = viewModelScope.launch {
        settingsRepo.setBirth(birth)
        GridWidget.updateAll(cuadricula)
    }

    fun setLifeExpectancy(years: Int) = viewModelScope.launch {
        settingsRepo.setLifeExpectancy(years)
        GridWidget.updateAll(cuadricula)
    }

    fun setReminder(enabled: Boolean) = viewModelScope.launch {
        settingsRepo.setReminder(enabled)
        if (enabled) ReminderScheduler.schedule(cuadricula) else ReminderScheduler.cancel(cuadricula)
    }

    fun saveEntry(entry: WeekEntry) = viewModelScope.launch {
        dao.upsert(entry)
        GridWidget.updateAll(cuadricula)
    }

    fun deleteEntry(weekIndex: Int) = viewModelScope.launch {
        dao.delete(weekIndex)
        GridWidget.updateAll(cuadricula)
    }

    fun addLastTime(title: String, date: LocalDate) = viewModelScope.launch {
        lastTimeDao.insert(
            LastTime(title = title.trim(), epochDay = date.toEpochDay(), createdAt = System.currentTimeMillis())
        )
        GridWidget.updateAll(cuadricula)
    }

    fun deleteLastTime(id: Long) = viewModelScope.launch {
        lastTimeDao.delete(id)
        GridWidget.updateAll(cuadricula)
    }

    fun addPerson(name: String, daysPerYear: Int, yearsLeft: Int) = viewModelScope.launch {
        personDao.insert(
            Person(
                name = name.trim(),
                daysPerYear = daysPerYear,
                yearsLeft = yearsLeft,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    fun deletePerson(id: Long) = viewModelScope.launch { personDao.delete(id) }
}
