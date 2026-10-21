package com.webgdl.cuadricula.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Registro de una semana de vida. [weekIndex] = fila * 52 + columna. */
@Entity(tableName = "week_entries")
data class WeekEntry(
    @PrimaryKey val weekIndex: Int,
    val note: String = "",
    /** 0 = sin valorar, 1..5 = de peor a mejor. */
    val mood: Int = 0,
    val isMemorable: Boolean = false,
    val updatedAt: Long = 0L,
)

@Dao
interface WeekDao {
    @Query("SELECT * FROM week_entries")
    fun observeAll(): Flow<List<WeekEntry>>

    @Upsert
    suspend fun upsert(entry: WeekEntry)

    @Query("DELETE FROM week_entries WHERE weekIndex = :weekIndex")
    suspend fun delete(weekIndex: Int)
}

@Database(entities = [WeekEntry::class, LastTime::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weekDao(): WeekDao
    abstract fun lastTimeDao(): LastTimeDao
}
