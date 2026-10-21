package com.webgdl.cuadricula.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.webgdl.cuadricula.MainViewModel
import com.webgdl.cuadricula.UiState
import com.webgdl.cuadricula.data.Layer
import com.webgdl.cuadricula.data.MAX_LIFE_EXPECTANCY
import com.webgdl.cuadricula.domain.LAYER_COLORS
import com.webgdl.cuadricula.domain.LifeCalc
import com.webgdl.cuadricula.domain.WEEKS_PER_ROW

private data class Preset(val name: String, val from: Int, val to: Int)

/** Etapas de ejemplo (como "Trabajo" en el video) para empezar rápido. */
private val presets = listOf(
    Preset("Escuela", 5, 18),
    Preset("Trabajo", 25, 65),
    Preset("Jubilación", 65, 80),
)

/** "Capas de tu vida": etapas entre dos edades, pintadas con un color sobre la cuadrícula. */
@Composable
fun LayersSection(vm: MainViewModel, ui: UiState, currentWeek: Int) {
    var showAdd by rememberSaveable { mutableStateOf(false) }

    Text(
        "Capas de tu vida",
        style = MaterialTheme.typography.titleMedium,
        color = Palette.Mint,
        modifier = Modifier.padding(top = 8.dp),
    )
    Text(
        "Pinta etapas como \"Trabajo\" entre dos edades y mira cuántas semanas te quedan de cada una.",
        style = MaterialTheme.typography.bodySmall,
        color = Palette.Muted,
    )

    ui.layers.forEach { layer -> LayerCard(layer, currentWeek, onDelete = { vm.deleteLayer(layer.id) }) }

    OutlinedButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
        Text("Agregar capa")
    }

    if (showAdd) {
        AddLayerDialog(
            nextColorIndex = ui.layers.size % LAYER_COLORS.size,
            onDismiss = { showAdd = false },
            onSave = { name, color, from, to ->
                vm.addLayer(name, color, from, to)
                showAdd = false
            },
        )
    }
}

@Composable
private fun LayerCard(layer: Layer, currentWeek: Int, onDelete: () -> Unit) {
    val color = Color(LAYER_COLORS[layer.colorIndex.coerceIn(0, LAYER_COLORS.size - 1)])
    val remaining = LifeCalc.remainingWeeksInRange(currentWeek, layer.fromAge, layer.toAge)
    InfoCard("${layer.fromAge}–${layer.toAge} años") {
        Text(
            layer.name,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            modifier = Modifier.padding(top = 4.dp),
        )
        val summary = if (remaining == 0) {
            "Esta etapa ya pasó."
        } else {
            "Te quedan $remaining semanas (≈ ${remaining / WEEKS_PER_ROW} años)."
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                summary,
                style = MaterialTheme.typography.bodySmall,
                color = Palette.Muted,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onDelete) { Text("Borrar") }
        }
    }
}

@Composable
private fun AddLayerDialog(
    nextColorIndex: Int,
    onDismiss: () -> Unit,
    onSave: (String, Int, Int, Int) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var fromText by rememberSaveable { mutableStateOf("") }
    var toText by rememberSaveable { mutableStateOf("") }
    var colorIndex by rememberSaveable { mutableIntStateOf(nextColorIndex) }
    val from = fromText.toIntOrNull()
    val to = toText.toIntOrNull()
    val valid = name.isNotBlank() && from != null && to != null &&
        from >= 0 && to <= MAX_LIFE_EXPECTANCY && from < to

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva capa") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                presets.forEach { p ->
                    AssistChip(
                        onClick = {
                            name = p.name
                            fromText = p.from.toString()
                            toText = p.to.toString()
                        },
                        label = { Text("${p.name} ${p.from}–${p.to}") },
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(30) },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = fromText,
                        onValueChange = { fromText = it.filter(Char::isDigit).take(3) },
                        label = { Text("Desde (edad)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = toText,
                        onValueChange = { toText = it.filter(Char::isDigit).take(3) },
                        label = { Text("Hasta (edad)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text("Color", style = MaterialTheme.typography.labelMedium, color = Palette.Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LAYER_COLORS.forEachIndexed { i, c ->
                        Row(
                            Modifier
                                .size(32.dp)
                                .background(Color(c), CircleShape)
                                .border(
                                    width = if (i == colorIndex) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape,
                                )
                                .clickable { colorIndex = i },
                        ) {}
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onSave(name, colorIndex, from ?: 0, to ?: 0) },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
