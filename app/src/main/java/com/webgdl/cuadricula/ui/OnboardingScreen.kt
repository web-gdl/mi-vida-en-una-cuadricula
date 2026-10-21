package com.webgdl.cuadricula.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.webgdl.cuadricula.data.MAX_LIFE_EXPECTANCY
import com.webgdl.cuadricula.data.MIN_LIFE_EXPECTANCY
import com.webgdl.cuadricula.domain.WEEKS_PER_ROW
import java.time.LocalDate

@Composable
fun OnboardingScreen(
    initialLifeExpectancy: Int,
    onDone: (LocalDate, Int) -> Unit,
) {
    var birthEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var life by rememberSaveable { mutableStateOf(initialLifeExpectancy.toFloat()) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val birth = birthEpochDay?.let { LocalDate.ofEpochDay(it) }
    val years = life.toInt()

    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.Bg)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Mi vida en una cuadrícula", style = MaterialTheme.typography.headlineMedium, color = Palette.Mint)
        Text(
            "Cada fila es un año. Cada cuadro, una semana. " +
                "Una vida de 80 años cabe en 4 160 cuadros: aquí puedes verla entera y llenarla de recuerdos.",
            style = MaterialTheme.typography.bodyLarge,
            color = Palette.Text,
        )

        Text("¿Cuándo naciste?", style = MaterialTheme.typography.titleMedium, color = Palette.Text)
        OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text(birth?.pretty() ?: "Elegir fecha de nacimiento")
        }

        Text("¿Hasta qué edad quieres contar?", style = MaterialTheme.typography.titleMedium, color = Palette.Text)
        Text(
            "$years años = ${years * WEEKS_PER_ROW} semanas",
            style = MaterialTheme.typography.titleLarge,
            color = Palette.Mint,
        )
        Slider(
            value = life,
            onValueChange = { life = it },
            valueRange = MIN_LIFE_EXPECTANCY.toFloat()..MAX_LIFE_EXPECTANCY.toFloat(),
            steps = MAX_LIFE_EXPECTANCY - MIN_LIFE_EXPECTANCY - 1,
            colors = SliderDefaults.colors(thumbColor = Palette.Mint, activeTrackColor = Palette.Mint),
        )
        Text(
            "Es una cifra editable; puedes cambiarla después en Ajustes.",
            style = MaterialTheme.typography.bodySmall,
            color = Palette.Muted,
        )

        Button(
            onClick = { birth?.let { onDone(it, years) } },
            enabled = birth != null,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Ver mi cuadrícula") }
    }

    if (showPicker) {
        BirthDatePickerDialog(
            initial = birth,
            onDismiss = { showPicker = false },
            onPicked = {
                birthEpochDay = it.toEpochDay()
                showPicker = false
            },
        )
    }
}
