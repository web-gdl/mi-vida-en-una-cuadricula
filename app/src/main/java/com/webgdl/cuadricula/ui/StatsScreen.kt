package com.webgdl.cuadricula.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.webgdl.cuadricula.MainViewModel
import com.webgdl.cuadricula.UiState
import com.webgdl.cuadricula.domain.LifeCalc
import com.webgdl.cuadricula.domain.parseTags
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

private val nf: NumberFormat = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es"))

private fun Int.fmt(): String = nf.format(this)

@Composable
fun StatsScreen(vm: MainViewModel, ui: UiState, birth: LocalDate, today: LocalDate) {
    val stats = remember(birth, today, ui.settings.lifeExpectancy) {
        LifeCalc.stats(birth, today, ui.settings.lifeExpectancy)
    }
    val memorable = ui.entries.values.count { it.isMemorable }
    val topTags = remember(ui.entries) {
        ui.entries.values
            .flatMap { parseTags(it.tags) }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(8)
            .map { it.key to it.value }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Las cifras de tu cuadrícula", style = MaterialTheme.typography.titleLarge, color = Palette.Mint)

        InfoCard("Semanas vividas") {
            Text(
                "${stats.weeksLived.fmt()} de ${stats.totalWeeks.fmt()}",
                style = MaterialTheme.typography.headlineSmall,
                color = Palette.Text,
            )
            LinearProgressIndicator(
                progress = { stats.percentLived },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(8.dp),
                color = Palette.Mint,
                trackColor = Palette.Slate,
            )
            Text(
                "${(stats.percentLived * 100).toInt()} % de tu cuadrícula",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.Muted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        InfoCard("Semanas que te quedan") {
            Text(
                stats.weeksRemaining.fmt(),
                style = MaterialTheme.typography.headlineSmall,
                color = Palette.Text,
            )
            Text(
                "≈ ${stats.yearsRemaining} años, es decir, ${stats.yearsRemaining} veranos más.",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.Muted,
            )
        }

        InfoCard("Cuánto pesa un año hoy") {
            Text(
                "1 año = ${"%.1f".format(Locale.US, stats.yearWeightPercent)} % de tu vida",
                style = MaterialTheme.typography.titleMedium,
                color = Palette.Text,
            )
            Text(
                "A los 10 años pesaba el 10 %; a los 50 pesará el 2 %. Por eso los veranos vuelan.",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.Muted,
            )
        }

        InfoCard("Si cada semana fuera un paso") {
            Text(
                "${stats.weeksRemaining.fmt()} pasos = ${stats.remainingMinutesAsSteps} minutos",
                style = MaterialTheme.typography.titleMedium,
                color = Palette.Text,
            )
            Text(
                "A 120 pasos por minuto, lo que te queda se camina en menos de lo que dura una película.",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.Muted,
            )
        }

        LayersSection(vm, ui, stats.currentWeek)

        PeopleSection(vm, ui)

        InfoCard("Tu registro") {
            Text(
                "${ui.entries.size} semanas con registro · $memorable memorables",
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.Text,
            )
            if (topTags.isNotEmpty()) {
                Text(
                    "Etiquetas más usadas",
                    style = MaterialTheme.typography.labelMedium,
                    color = Palette.Muted,
                    modifier = Modifier.padding(top = 10.dp),
                )
                topTags.forEach { (tag, count) ->
                    Text(
                        "#$tag · $count ${if (count == 1) "semana" else "semanas"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Palette.Mint,
                    )
                }
            }
        }

        InfoCard("Séneca, hacia el año 49") {
            Text(
                "«No tenemos poco tiempo: perdemos mucho.»",
                style = MaterialTheme.typography.titleMedium,
                color = Palette.Text,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "De la brevedad de la vida",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.Muted,
            )
            Text(
                "¿Con quién las vas a pasar?",
                style = MaterialTheme.typography.titleLarge,
                color = Palette.Mint,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
