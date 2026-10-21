package com.webgdl.cuadricula.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.webgdl.cuadricula.MainViewModel
import com.webgdl.cuadricula.UiState
import com.webgdl.cuadricula.data.Person

/** "Tiempo con personas": cuántos días al año ves a alguien y cuántos te quedan juntos. */
@Composable
fun PeopleSection(vm: MainViewModel, ui: UiState) {
    var showAdd by rememberSaveable { mutableStateOf(false) }

    Text(
        "Tiempo con personas",
        style = MaterialTheme.typography.titleMedium,
        color = Palette.Magenta,
        modifier = Modifier.padding(top = 8.dp),
    )
    Text(
        "De niño veías a tus papás casi a diario. Hoy, ¿cuántos días al año? " +
            "Pon el ritmo de hoy y mira cuánto te queda.",
        style = MaterialTheme.typography.bodySmall,
        color = Palette.Muted,
    )

    ui.people.forEach { person ->
        PersonCard(person, onDelete = { vm.deletePerson(person.id) })
    }

    OutlinedButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
        Text("Agregar persona")
    }

    if (showAdd) {
        AddPersonDialog(
            onDismiss = { showAdd = false },
            onSave = { name, days, years ->
                vm.addPerson(name, days, years)
                showAdd = false
            },
        )
    }
}

@Composable
private fun PersonCard(person: Person, onDelete: () -> Unit) {
    val total = person.daysLeft
    InfoCard(person.name) {
        Text(
            "$total días",
            style = MaterialTheme.typography.headlineSmall,
            color = Palette.Magenta,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            "${person.daysPerYear} días al año × ${person.yearsLeft} años",
            style = MaterialTheme.typography.bodyMedium,
            color = Palette.Text,
        )
        val summary = when {
            total <= 0 -> "Sin días contados."
            total < 365 -> "Menos de un año completo de días juntos."
            else -> "Equivale a ${total / 365} años de días seguidos."
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(summary, style = MaterialTheme.typography.bodySmall, color = Palette.Muted, modifier = Modifier.weight(1f))
            TextButton(onClick = onDelete) { Text("Borrar") }
        }
    }
}

@Composable
private fun AddPersonDialog(onDismiss: () -> Unit, onSave: (String, Int, Int) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var daysText by rememberSaveable { mutableStateOf("") }
    var yearsText by rememberSaveable { mutableStateOf("") }
    val days = daysText.toIntOrNull()
    val years = yearsText.toIntOrNull()
    val valid = name.isNotBlank() && days != null && days in 0..365 && years != null && years in 0..100

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Con quién?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text("Nombre (mamá, Luis…)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it.filter(Char::isDigit).take(3) },
                    label = { Text("Días al año que la ves hoy (0–365)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = yearsText,
                    onValueChange = { yearsText = it.filter(Char::isDigit).take(3) },
                    label = { Text("Años que calculas que les quedan (0–100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onSave(name, days ?: 0, years ?: 0) },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
