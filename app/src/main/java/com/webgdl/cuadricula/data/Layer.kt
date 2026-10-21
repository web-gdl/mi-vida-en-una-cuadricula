package com.webgdl.cuadricula.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/** Una etapa de la vida (escuela, trabajo…) pintada con un color sobre las filas de ese rango de edad. */
@Entity(tableName = "layers")
data class Layer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Índice en [com.webgdl.cuadricula.domain.LAYER_COLORS]. */
    val colorIndex: Int,
    val fromAge: Int,
    val toAge: Int,
    val createdAt: Long = 0L,
)

@Dao
interface LayerDao {
    @Query("SELECT * FROM layers ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<Layer>>

    @Insert
    suspend fun insert(layer: Layer)

    @Insert
    suspend fun insertAll(layers: List<Layer>)

    @Query("DELETE FROM layers WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM layers")
    suspend fun clear()
}

/** v3 -> v4: tabla de capas y columna de etiquetas en las semanas, sin tocar los datos existentes. */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `layers` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`colorIndex` INTEGER NOT NULL, " +
                "`fromAge` INTEGER NOT NULL, " +
                "`toAge` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )
        db.execSQL("ALTER TABLE `week_entries` ADD COLUMN `tags` TEXT NOT NULL DEFAULT ''")
    }
}
