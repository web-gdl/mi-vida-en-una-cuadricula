package com.webgdl.cuadricula.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/** Una persona con quien quieres contar tu tiempo (papás, hermanos, amigos…). */
@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Días al año que la ves hoy. */
    val daysPerYear: Int,
    /** Años que calculas que les quedan juntos. */
    val yearsLeft: Int,
    val createdAt: Long = 0L,
) {
    val daysLeft: Int get() = daysPerYear * yearsLeft
}

@Dao
interface PersonDao {
    @Query("SELECT * FROM people ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<Person>>

    @Insert
    suspend fun insert(person: Person)

    @Query("DELETE FROM people WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert
    suspend fun insertAll(people: List<Person>)

    @Query("DELETE FROM people")
    suspend fun clear()
}

/** v2 -> v3: añade la tabla de personas sin tocar lo existente. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `people` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`daysPerYear` INTEGER NOT NULL, " +
                "`yearsLeft` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )
    }
}
