package com.webgdl.cuadricula.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/** Un momento que ya ocurrió por última vez ("alguien te cargó en brazos"). */
@Entity(tableName = "last_times")
data class LastTime(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** Fecha aproximada del momento, como epochDay. */
    val epochDay: Long,
    val createdAt: Long = 0L,
)

@Dao
interface LastTimeDao {
    @Query("SELECT * FROM last_times ORDER BY epochDay DESC")
    fun observeAll(): Flow<List<LastTime>>

    @Insert
    suspend fun insert(item: LastTime)

    @Query("DELETE FROM last_times WHERE id = :id")
    suspend fun delete(id: Long)
}

/** v1 -> v2: añade la tabla de "última vez" sin tocar los registros semanales existentes. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `last_times` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`epochDay` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )
    }
}
