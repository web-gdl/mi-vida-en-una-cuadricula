package com.webgdl.cuadricula

import android.app.Application
import androidx.room.Room
import com.webgdl.cuadricula.data.AppDatabase
import com.webgdl.cuadricula.data.MIGRATION_1_2
import com.webgdl.cuadricula.data.MIGRATION_2_3
import com.webgdl.cuadricula.data.MIGRATION_3_4
import com.webgdl.cuadricula.data.SettingsRepository

class CuadriculaApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "cuadricula.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
}
