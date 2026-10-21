package com.webgdl.cuadricula.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.webgdl.cuadricula.MainViewModel
import com.webgdl.cuadricula.UiState
import com.webgdl.cuadricula.data.MAX_LIFE_EXPECTANCY
import com.webgdl.cuadricula.data.MIN_LIFE_EXPECTANCY
import com.webgdl.cuadricula.domain.WEEKS_PER_ROW
import java.time.LocalDate

@Composable
fun SettingsScreen(vm: MainViewModel, ui: UiState, birth: LocalDate) {
    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }
    var lifeSlider by remember { mutableFloatStateOf(ui.settings.lifeExpectancy.toFloat()) }

    // Si el valor guardado cambia desde fuera, reflejarlo en el slider.
    LaunchedEffect(ui.settings.lifeExpectancy) { lifeSlider = ui.settings.lifeExpectancy.toFloat() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> vm.setReminder(granted) }

    fun onReminderToggle(enabled: Boolean) {
        if (!enabled) {
            vm.setReminder(false)
            return
        }
        val needsPermission = Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            vm.setReminder(true)
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Ajustes", style = MaterialTheme.typography.titleLarge, color = Palette.Mint)

        InfoCard("Fecha de nacimiento") {
            OutlinedButton(
                onClick = { showPicker = true },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text(birth.pretty()) }
        }

        InfoCard("Esperanza de vida (editable)") {
            val years = lifeSlider.toInt()
            Text(
                "$years años = ${years * WEEKS_PER_ROW} semanas",
                style = MaterialTheme.typography.titleMedium,
                color = Palette.Text,
                modifier = Modifier.padding(top = 4.dp),
            )
            Slider(
                value = lifeSlider,
                onValueChange = { lifeSlider = it },
                onValueChangeFinished = { vm.setLifeExpectancy(lifeSlider.toInt()) },
                valueRange = MIN_LIFE_EXPECTANCY.toFloat()..MAX_LIFE_EXPECTANCY.toFloat(),
                steps = MAX_LIFE_EXPECTANCY - MIN_LIFE_EXPECTANCY - 1,
                colors = SliderDefaults.colors(thumbColor = Palette.Mint, activeTrackColor = Palette.Mint),
            )
            Text(
                "Cambiarla solo redibuja la cuadrícula; tus notas se conservan.",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.Muted,
            )
        }

        InfoCard("Recordatorio semanal") {
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Cada lunes a las 9:00", color = Palette.Text)
                Switch(
                    checked = ui.settings.reminderEnabled,
                    onCheckedChange = { onReminderToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Palette.Mint,
                        checkedTrackColor = Palette.Mint.copy(alpha = 0.4f),
                    ),
                )
            }
        }
    }

    if (showPicker) {
        BirthDatePickerDialog(
            initial = birth,
            onDismiss = { showPicker = false },
            onPicked = {
                vm.setBirth(it)
                showPicker = false
            },
        )
    }
}
