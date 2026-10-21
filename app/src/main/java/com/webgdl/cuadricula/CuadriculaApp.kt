package com.webgdl.cuadricula

import android.app.Application
import androidx.room.Room
import com.webgdl.cuadricula.data.AppDatabase
import com.webgdl.cuadricula.data.SettingsRepository

class CuadriculaApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "cuadricula.db").build()
    }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
}
