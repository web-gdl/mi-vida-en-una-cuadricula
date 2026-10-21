package com.webgdl.cuadricula.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.webgdl.cuadricula.MainViewModel
import com.webgdl.cuadricula.UiState
import com.webgdl.cuadricula.domain.LAYER_COLORS
import com.webgdl.cuadricula.domain.LayerRange
import com.webgdl.cuadricula.domain.LifeCalc
import com.webgdl.cuadricula.domain.layerRowColors
import java.time.LocalDate

@Composable
fun GridScreen(vm: MainViewModel, ui: UiState, birth: LocalDate, today: LocalDate) {
    val rows = ui.settings.lifeExpectancy
    val stats = remember(birth, today, rows) { LifeCalc.stats(birth, today, rows) }
    val lastTimeWeeks = remember(ui.lastTimes, birth) {
        ui.lastTimes
            .map { LifeCalc.weekIndexOf(birth, LocalDate.ofEpochDay(it.epochDay)) }
            .filter { it >= 0 }
            .toSet()
    }
    val showLayers = ui.settings.showLayers && ui.layers.isNotEmpty()
    val layerColors = remember(ui.layers, rows, showLayers) {
        if (showLayers) {
            layerRowColors(
                rows,
                ui.layers.map {
                    LayerRange(it.fromAge, it.toAge, LAYER_COLORS[it.colorIndex.coerceIn(0, LAYER_COLORS.size - 1)])
                },
            )
        } else {
            null
        }
    }
    var zoomed by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    val scroll = rememberScrollState()
    var viewportPx by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Mi vida en una cuadrícula", style = MaterialTheme.typography.titleLarge, color = Palette.Mint)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Semana ${stats.currentWeek + 1} de ${stats.totalWeeks} · ${(stats.percentLived * 100).toInt()} %",
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.Muted,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ui.layers.isNotEmpty()) {
                        FilterChip(
                            selected = ui.settings.showLayers,
                            onClick = { vm.setShowLayers(!ui.settings.showLayers) },
                            label = { Text("Capas") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Palette.SurfaceHigh,
                                selectedLabelColor = Palette.Mint,
                            ),
                        )
                    }
                    FilterChip(
                        selected = zoomed,
                        onClick = { zoomed = !zoomed },
                        label = { Text("Ampliar") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Palette.SurfaceHigh,
                            selectedLabelColor = Palette.Mint,
                        ),
                    )
                }
            }
            Legend()
            if (showLayers) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ui.layers.forEach {
                        LegendItem(
                            Color(LAYER_COLORS[it.colorIndex.coerceIn(0, LAYER_COLORS.size - 1)]).copy(alpha = 0.75f),
                            "${it.name} ${it.fromAge}–${it.toAge}",
                        )
                    }
                }
            }
        }

        Column(
            Modifier
                .weight(1f)
                .onSizeChanged { viewportPx = it.height }
                .verticalScroll(scroll)
        ) {
            LifeGrid(
                rows = rows,
                currentWeek = stats.currentWeek,
                entries = ui.entries,
                lastTimeWeeks = lastTimeWeeks,
                layerRowColors = layerColors,
                zoomed = zoomed,
                verticalScroll = scroll,
                viewportHeightPx = viewportPx,
                onWeekClick = { selected = it },
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }

    selected?.let { week ->
        WeekSheet(
            weekIndex = week,
            birth = birth,
            currentWeek = stats.currentWeek,
            totalWeeks = stats.totalWeeks,
            entries = ui.entries,
            lastTimes = ui.lastTimes.filter {
                LifeCalc.weekIndexOf(birth, LocalDate.ofEpochDay(it.epochDay)) == week
            },
            onSave = { vm.saveEntry(it) },
            onDelete = { vm.deleteEntry(it) },
            onSelect = { selected = it },
            onDismiss = { selected = null },
        )
    }
}

@Composable
private fun Legend() {
    Row(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(Palette.Mint.copy(alpha = 0.4f), "vivida")
        LegendItem(Palette.Mint, "con nota")
        LegendItem(Palette.Amber, "memorable")
        LegendItem(Palette.Magenta, "última vez")
        LegendItem(Palette.Slate.copy(alpha = 0.55f), "futura")
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(8.dp).background(color))
        Text(text, style = MaterialTheme.typography.labelSmall, color = Palette.Muted)
    }
}
