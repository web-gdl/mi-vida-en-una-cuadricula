package com.webgdl.cuadricula.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.webgdl.cuadricula.data.LastTime
import com.webgdl.cuadricula.data.WeekEntry
import com.webgdl.cuadricula.domain.LifeCalc
import com.webgdl.cuadricula.domain.WEEKS_PER_ROW
import java.time.LocalDate

private val moodFaces = listOf("😞", "🙁", "😐", "🙂", "😄")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSheet(
    weekIndex: Int,
    birth: LocalDate,
    currentWeek: Int,
    totalWeeks: Int,
    entries: Map<Int, WeekEntry>,
    lastTimes: List<LastTime>,
    onSave: (WeekEntry) -> Unit,
    onDelete: (Int) -> Unit,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val existing = entries[weekIndex]
    var note by remember(weekIndex) { mutableStateOf(existing?.note ?: "") }
    var mood by remember(weekIndex) { mutableIntStateOf(existing?.mood ?: 0) }
    var memorable by remember(weekIndex) { mutableStateOf(existing?.isMemorable ?: false) }

    // Guarda lo escrito (o borra el registro si quedó vacío).
    fun persist() {
        if (note.isBlank() && mood == 0 && !memorable) {
            if (existing != null) onDelete(weekIndex)
        } else {
            onSave(WeekEntry(weekIndex, note.trim(), mood, memorable, System.currentTimeMillis()))
        }
    }

    fun goTo(target: Int) {
        if (target in 0 until totalWeeks) {
            persist()
            onSelect(target)
        }
    }

    val row = weekIndex / WEEKS_PER_ROW
    val col = weekIndex % WEEKS_PER_ROW
    val status = when {
        weekIndex < currentWeek -> "Semana pasada"
        weekIndex == currentWeek -> "Semana actual"
        else -> "Semana futura"
    }

    ModalBottomSheet(
        onDismissRequest = {
            persist()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Palette.Surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = { goTo(weekIndex - 1) }, enabled = weekIndex > 0) { Text("◀") }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Semana ${col + 1} · edad $row",
                        style = MaterialTheme.typography.titleMedium,
                        color = Palette.Mint,
                    )
                    Text(
                        "${LifeCalc.weekStart(birth, weekIndex).pretty()} – ${LifeCalc.weekEnd(birth, weekIndex).pretty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Palette.Muted,
                    )
                    Text(status, style = MaterialTheme.typography.labelSmall, color = Palette.Muted)
                }
                TextButton(onClick = { goTo(weekIndex + 1) }, enabled = weekIndex < totalWeeks - 1) { Text("▶") }
            }

            lastTimes.forEach {
                Text(
                    "● Última vez: ${it.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.Magenta,
                )
            }

            Text("¿Cómo fue?", style = MaterialTheme.typography.labelMedium, color = Palette.Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                moodFaces.forEachIndexed { i, face ->
                    FilterChip(
                        selected = mood == i + 1,
                        onClick = { mood = if (mood == i + 1) 0 else i + 1 },
                        label = { Text(face) },
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Una línea sobre esta semana") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Semana memorable", color = Palette.Text)
                Switch(
                    checked = memorable,
                    onCheckedChange = { memorable = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Palette.Amber,
                        checkedTrackColor = Palette.Amber.copy(alpha = 0.4f),
                    ),
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        note = ""
                        mood = 0
                        memorable = false
                        if (existing != null) onDelete(weekIndex)
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Borrar") }
                Button(
                    onClick = {
                        persist()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Guardar") }
            }
        }
    }
}
