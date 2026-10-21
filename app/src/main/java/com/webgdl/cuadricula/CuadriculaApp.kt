package com.webgdl.cuadricula

import android.app.Application
import androidx.room.Room
import com.webgdl.cuadricula.data.AppDatabase
import com.webgdl.cuadricula.data.MIGRATION_1_2
import com.webgdl.cuadricula.data.MIGRATION_2_3
import com.webgdl.cuadricula.data.MIGRATION_3_4
import com.webgdl.cuadricula.data.SettingsRepository
import com.webgdl.cuadricula.reminder.ReminderScheduler

class CuadriculaApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "cuadricula.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        // Asegura que el aviso semanal siga programado (p. ej. tras una actualización).
        if (SettingsRepository.reminderEnabled(this)) ReminderScheduler.schedule(this)
    }
}
