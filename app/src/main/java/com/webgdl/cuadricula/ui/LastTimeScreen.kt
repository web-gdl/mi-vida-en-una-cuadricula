package com.webgdl.cuadricula.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.webgdl.cuadricula.MainViewModel
import com.webgdl.cuadricula.UiState
import com.webgdl.cuadricula.data.LastTime
import com.webgdl.cuadricula.domain.LifeCalc
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Ejemplos tomados del video para que sea fácil empezar. */
private val suggestions = listOf(
    "Alguien me cargó en brazos",
    "Una tarde jugando en la calle",
    "Dormí en el cuarto de mi infancia",
)

@Composable
fun LastTimeScreen(vm: MainViewModel, ui: UiState, birth: LocalDate, today: LocalDate) {
    var showAdd by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text(
            "La última vez",
            style = MaterialTheme.typography.titleLarge,
            color = Palette.Magenta,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            "Hay cosas que ya hiciste por última vez y nadie te avisó. " +
                "Anótalas: cada una se marca en magenta en tu cuadrícula.",
            style = MaterialTheme.typography.bodySmall,
            color = Palette.Muted,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar un momento")
        }

        if (ui.lastTimes.isEmpty()) {
            Text(
                "Todavía no hay ninguno. ¿Cuándo fue la última vez que alguien te cargó en brazos?",
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.Muted,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(ui.lastTimes, key = { it.id }) { item ->
                    LastTimeCard(item, birth, today, onDelete = { vm.deleteLastTime(item.id) })
                }
            }
        }
    }

    if (showAdd) {
        AddLastTimeDialog(
            birth = birth,
            today = today,
            onDismiss = { showAdd = false },
            onSave = { title, date ->
                vm.addLastTime(title, date)
                showAdd = false
            },
        )
    }
}

@Composable
private fun LastTimeCard(item: LastTime, birth: LocalDate, today: LocalDate, onDelete: () -> Unit) {
    val date = LocalDate.ofEpochDay(item.epochDay)
    val age = ChronoUnit.YEARS.between(birth, date).toInt()
    val ago = ChronoUnit.YEARS.between(date, today).toInt()
    InfoCard(title = date.pretty()) {
        Text(
            item.title,
            style = MaterialTheme.typography.titleMedium,
            color = Palette.Magenta,
            modifier = Modifier.padding(top = 4.dp),
        )
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "A los $age años · " + if (ago <= 0) "este año" else "hace $ago años",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.Muted,
            )
            TextButton(onClick = onDelete) { Text("Borrar") }
        }
    }
}

@Composable
private fun AddLastTimeDialog(
    birth: LocalDate,
    today: LocalDate,
    onDismiss: () -> Unit,
    onSave: (String, LocalDate) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var epochDay by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val date = LocalDate.ofEpochDay(epochDay)
    val valid = title.isNotBlank() && LifeCalc.weekIndexOf(birth, date) >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Qué fue la última vez?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(120) },
                    label = { Text("Momento") },
                    modifier = Modifier.fillMaxWidth(),
                )
                suggestions.forEach { s ->
                    AssistChip(onClick = { title = s }, label = { Text(s) })
                }
                Text("¿Más o menos cuándo?", style = MaterialTheme.typography.labelMedium, color = Palette.Muted)
                OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(date.pretty())
                }
                if (LifeCalc.weekIndexOf(birth, date) < 0) {
                    Text(
                        "La fecha es anterior a tu nacimiento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Palette.Magenta,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, date) }, enabled = valid) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )

    if (showPicker) {
        BirthDatePickerDialog(
            initial = date,
            onDismiss = { showPicker = false },
            onPicked = {
                epochDay = it.toEpochDay()
                showPicker = false
            },
        )
    }
}
