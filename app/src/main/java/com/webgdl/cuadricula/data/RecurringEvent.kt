package com.webgdl.cuadricula.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/** Algo que se repite: "reunión con amigos" 12 veces al año, "ir a la playa" 2 veces al año… */
@Entity(tableName = "recurring_events")
data class RecurringEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val timesPerYear: Int,
    /** Edad hasta la que crees que lo seguirás haciendo; 0 = toda la vida (esperanza de vida). */
    val untilAge: Int = 0,
    val createdAt: Long = 0L,
)

/** Una vez que el evento ocurrió de verdad. */
@Entity(tableName = "event_logs")
data class EventLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val epochDay: Long,
)

@Dao
interface EventDao {
    @Query("SELECT * FROM recurring_events ORDER BY createdAt ASC")
    fun observeEvents(): Flow<List<RecurringEvent>>

    @Query("SELECT * FROM event_logs ORDER BY epochDay DESC, id DESC")
    fun observeLogs(): Flow<List<EventLog>>

    @Insert
    suspend fun insertEvent(event: RecurringEvent): Long

    @Insert
    suspend fun insertLog(log: EventLog)

    @Insert
    suspend fun insertLogs(logs: List<EventLog>)

    @Query("DELETE FROM recurring_events WHERE id = :id")
    suspend fun deleteEvent(id: Long)

    @Query("DELETE FROM event_logs WHERE eventId = :eventId")
    suspend fun deleteLogsOf(eventId: Long)

    /** Deshace el registro más reciente de un evento. */
    @Query(
        "DELETE FROM event_logs WHERE id = (SELECT id FROM event_logs WHERE eventId = :eventId " +
            "ORDER BY epochDay DESC, id DESC LIMIT 1)"
    )
    suspend fun deleteLatestLog(eventId: Long)

    @Query("DELETE FROM recurring_events")
    suspend fun clearEvents()

    @Query("DELETE FROM event_logs")
    suspend fun clearLogs()
}

/** v4 -> v5: tablas de eventos periódicos y de sus registros, sin tocar lo existente. */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `recurring_events` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`timesPerYear` INTEGER NOT NULL, " +
                "`untilAge` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `event_logs` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`eventId` INTEGER NOT NULL, " +
                "`epochDay` INTEGER NOT NULL)"
        )
    }
}
