package com.webgdl.cuadricula.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.webgdl.cuadricula.MainViewModel
import com.webgdl.cuadricula.UiState
import com.webgdl.cuadricula.data.EventLog
import com.webgdl.cuadricula.data.MAX_LIFE_EXPECTANCY
import com.webgdl.cuadricula.data.RecurringEvent
import com.webgdl.cuadricula.domain.LifeCalc
import com.webgdl.cuadricula.domain.Recurrence
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

private data class EventPreset(val name: String, val timesPerYear: Int)

/** Ejemplos para empezar rápido; se pueden ajustar antes de guardar. */
private val eventPresets = listOf(
    EventPreset("Reunión con amigos", 12),
    EventPreset("Ir a la playa", 2),
    EventPreset("Comida con la familia", 24),
    EventPreset("Leer un libro", 12),
    EventPreset("Viaje", 1),
)

private val numberFormat: NumberFormat = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es"))

/** Eventos periódicos: cuántas veces te quedan de lo que se repite, y registro de cada vez. */
@Composable
fun EventsScreen(
    vm: MainViewModel,
    ui: UiState,
    birth: LocalDate,
    today: LocalDate,
    onShowInGrid: (Long) -> Unit,
) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    val currentWeek = remember(birth, today) { maxOf(0, LifeCalc.weekIndexOf(birth, today)) }
    val logsByEvent = remember(ui.eventLogs) { ui.eventLogs.groupBy { it.eventId } }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text(
            "Eventos periódicos",
            style = MaterialTheme.typography.titleLarge,
            color = Palette.Mint,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            "Lo que se repite también se acaba. ¿Cuántas reuniones con amigos te quedan? " +
                "¿Cuántas veces volverás a la playa? Regístralo cada vez que pase.",
            style = MaterialTheme.typography.bodySmall,
            color = Palette.Muted,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar evento")
        }

        if (ui.events.isEmpty()) {
            Text(
                "Todavía no hay eventos. Prueba con \"Reunión con amigos\" o \"Ir a la playa\".",
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.Muted,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(ui.events, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        logs = logsByEvent[event.id].orEmpty(),
                        currentWeek = currentWeek,
                        lifeExpectancy = ui.settings.lifeExpectancy,
                        birth = birth,
                        today = today,
                        onLog = { date -> vm.logEvent(event.id, date) },
                        onUndo = { vm.undoLastLog(event.id) },
                        onShowInGrid = { onShowInGrid(event.id) },
                        onDelete = { vm.deleteEvent(event.id) },
                    )
                }
            }
        }
    }

    if (showAdd) {
        AddEventDialog(
            onDismiss = { showAdd = false },
            onSave = { name, times, until ->
                vm.addEvent(name, times, until)
                showAdd = false
            },
        )
    }
}

@Composable
private fun EventCard(
    event: RecurringEvent,
    logs: List<EventLog>,
    currentWeek: Int,
    lifeExpectancy: Int,
    birth: LocalDate,
    today: LocalDate,
    onLog: (LocalDate) -> Unit,
    onUndo: () -> Unit,
    onShowInGrid: () -> Unit,
    onDelete: () -> Unit,
) {
    var pickDate by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val remaining = Recurrence.remainingTimes(currentWeek, event.timesPerYear, event.untilAge, lifeExpectancy)
    val thisYear = logs.count { LocalDate.ofEpochDay(it.epochDay).year == today.year }
    val last = logs.maxOfOrNull { it.epochDay }?.let { LocalDate.ofEpochDay(it) }
    val until = if (event.untilAge <= 0) "toda la vida" else "hasta los ${event.untilAge} años"

    InfoCard("${event.timesPerYear} ${if (event.timesPerYear == 1) "vez" else "veces"} al año · $until") {
        Text(
            event.name,
            style = MaterialTheme.typography.titleMedium,
            color = Palette.Text,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            if (remaining == 0) "Ya no quedan veces en tu cuenta." else "Te quedan ${numberFormat.format(remaining)} veces",
            style = MaterialTheme.typography.headlineSmall,
            color = if (remaining == 0) Palette.Magenta else Palette.Mint,
        )
        Text(
            buildString {
                append("Este año: $thisYear de ${event.timesPerYear}")
                append(" · registradas: ${logs.size}")
                if (last != null) append("\nÚltima vez: ${last.pretty()}")
            },
            style = MaterialTheme.typography.bodySmall,
            color = Palette.Muted,
            modifier = Modifier.padding(top = 4.dp),
        )
        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { onLog(today) }, modifier = Modifier.weight(1f)) { Text("Hoy ✓") }
            OutlinedButton(onClick = { pickDate = true }, modifier = Modifier.weight(1f)) { Text("Otra fecha") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onShowInGrid, enabled = remaining > 0) { Text("Ver en cuadrícula") }
            if (logs.isNotEmpty()) TextButton(onClick = onUndo) { Text("Deshacer") }
            TextButton(onClick = { confirmDelete = true }) { Text("Borrar") }
        }
    }

    if (pickDate) {
        BirthDatePickerDialog(
            initial = today,
            onDismiss = { pickDate = false },
            onPicked = {
                pickDate = false
                if (!it.isBefore(birth)) onLog(it)
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("¿Borrar \"${event.name}\"?") },
            text = { Text("También se borran las ${logs.size} veces registradas.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Borrar") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun AddEventDialog(onDismiss: () -> Unit, onSave: (String, Int, Int) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var timesText by rememberSaveable { mutableStateOf("") }
    var untilText by rememberSaveable { mutableStateOf("") }
    val times = timesText.toIntOrNull()
    val until = if (untilText.isBlank()) 0 else untilText.toIntOrNull()
    val valid = name.isNotBlank() && times != null && times in 1..365 &&
        until != null && until in 0..MAX_LIFE_EXPECTANCY

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo evento periódico") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                eventPresets.forEach { p ->
                    AssistChip(
                        onClick = {
                            name = p.name
                            timesText = p.timesPerYear.toString()
                        },
                        label = { Text("${p.name} · ${p.timesPerYear}/año") },
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text("¿Qué se repite?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = timesText,
                    onValueChange = { timesText = it.filter(Char::isDigit).take(3) },
                    label = { Text("Veces al año (1–365)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = untilText,
                    onValueChange = { untilText = it.filter(Char::isDigit).take(3) },
                    label = { Text("¿Hasta qué edad? (vacío = toda la vida)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onSave(name, times ?: 1, until ?: 0) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
