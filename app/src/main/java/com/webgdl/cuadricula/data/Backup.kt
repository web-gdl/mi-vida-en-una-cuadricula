package com.webgdl.cuadricula.data

import androidx.room.withTransaction
import com.webgdl.cuadricula.CuadriculaApp
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

/** Respaldo completo de la app en un archivo JSON legible (versión 1). */
object Backup {
    private const val VERSION = 1

    private class Parsed(
        val birthEpochDay: Long?,
        val lifeExpectancy: Int,
        val entries: List<WeekEntry>,
        val lastTimes: List<LastTime>,
        val people: List<Person>,
    )

    suspend fun export(app: CuadriculaApp): String {
        val settings = app.settingsRepository.settings.first()
        val db = app.database
        val root = JSONObject()
        root.put("version", VERSION)
        settings.birthEpochDay?.let { root.put("birthEpochDay", it) }
        root.put("lifeExpectancy", settings.lifeExpectancy)

        val entries = JSONArray()
        db.weekDao().observeAll().first().forEach {
            entries.put(
                JSONObject()
                    .put("weekIndex", it.weekIndex)
                    .put("note", it.note)
                    .put("mood", it.mood)
                    .put("isMemorable", it.isMemorable)
                    .put("updatedAt", it.updatedAt)
            )
        }
        root.put("entries", entries)

        val lastTimes = JSONArray()
        db.lastTimeDao().observeAll().first().forEach {
            lastTimes.put(
                JSONObject()
                    .put("title", it.title)
                    .put("epochDay", it.epochDay)
                    .put("createdAt", it.createdAt)
            )
        }
        root.put("lastTimes", lastTimes)

        val people = JSONArray()
        db.personDao().observeAll().first().forEach {
            people.put(
                JSONObject()
                    .put("name", it.name)
                    .put("daysPerYear", it.daysPerYear)
                    .put("yearsLeft", it.yearsLeft)
                    .put("createdAt", it.createdAt)
            )
        }
        root.put("people", people)
        return root.toString(2)
    }

    /**
     * Reemplaza todos los datos por los del respaldo. Se valida todo el archivo antes de
     * tocar nada: si es inválido devuelve false y los datos actuales quedan intactos.
     */
    suspend fun import(app: CuadriculaApp, json: String): Boolean {
        val parsed = try {
            parse(json)
        } catch (e: Exception) {
            return false
        }
        val db = app.database
        db.withTransaction {
            db.weekDao().clear()
            db.weekDao().upsertAll(parsed.entries)
            db.lastTimeDao().clear()
            db.lastTimeDao().insertAll(parsed.lastTimes)
            db.personDao().clear()
            db.personDao().insertAll(parsed.people)
        }
        val birth = parsed.birthEpochDay
        if (birth != null) {
            app.settingsRepository.saveProfile(LocalDate.ofEpochDay(birth), parsed.lifeExpectancy)
        } else {
            app.settingsRepository.setLifeExpectancy(parsed.lifeExpectancy)
        }
        return true
    }

    private fun parse(json: String): Parsed {
        val root = JSONObject(json)
        require(root.getInt("version") == VERSION) { "versión no soportada" }

        val entries = root.getJSONArray("entries").let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val index = o.getInt("weekIndex")
                require(index >= 0) { "semana inválida" }
                WeekEntry(
                    weekIndex = index,
                    note = o.optString("note", ""),
                    mood = o.optInt("mood", 0).coerceIn(0, 5),
                    isMemorable = o.optBoolean("isMemorable", false),
                    updatedAt = o.optLong("updatedAt", 0L),
                )
            }
        }
        val lastTimes = root.getJSONArray("lastTimes").let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                LastTime(
                    title = o.getString("title"),
                    epochDay = o.getLong("epochDay"),
                    createdAt = o.optLong("createdAt", 0L),
                )
            }
        }
        val people = root.getJSONArray("people").let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Person(
                    name = o.getString("name"),
                    daysPerYear = o.getInt("daysPerYear").coerceIn(0, 365),
                    yearsLeft = o.getInt("yearsLeft").coerceIn(0, 100),
                    createdAt = o.optLong("createdAt", 0L),
                )
            }
        }
        return Parsed(
            birthEpochDay = if (root.has("birthEpochDay")) root.getLong("birthEpochDay") else null,
            lifeExpectancy = root.getInt("lifeExpectancy").coerceIn(MIN_LIFE_EXPECTANCY, MAX_LIFE_EXPECTANCY),
            entries = entries,
            lastTimes = lastTimes,
            people = people,
        )
    }
}
