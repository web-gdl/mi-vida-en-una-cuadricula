package com.webgdl.cuadricula.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webgdl.cuadricula.MainViewModel
import java.time.LocalDate

private enum class Tab(val label: String, val glyph: String) {
    Grid("Vida", "▦"),
    LastTime("Última", "◈"),
    Events("Eventos", "↻"),
    Stats("Cifras", "∑"),
    Settings("Ajustes", "⚙"),
}

@Composable
fun App(vm: MainViewModel) {
    val ui by vm.state.collectAsStateWithLifecycle()
    val birth = ui.settings.birthDate

    when {
        !ui.loaded -> Box(Modifier.fillMaxSize().background(Palette.Bg))
        birth == null -> OnboardingScreen(
            initialLifeExpectancy = ui.settings.lifeExpectancy,
            onDone = { date, life -> vm.saveProfile(date, life) },
        )
        else -> MainScaffold(vm, ui, birth)
    }
}

@Composable
private fun MainScaffold(vm: MainViewModel, ui: com.webgdl.cuadricula.UiState, birth: LocalDate) {
    var tabIndex by rememberSaveable { mutableStateOf(0) }
    val today = remember { LocalDate.now() }

    Scaffold(
        containerColor = Palette.Bg,
        bottomBar = {
            NavigationBar(containerColor = Palette.Surface) {
                Tab.entries.forEachIndexed { i, tab ->
                    NavigationBarItem(
                        selected = tabIndex == i,
                        onClick = { tabIndex = i },
                        icon = { Text(tab.glyph) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Palette.Mint,
                            selectedTextColor = Palette.Mint,
                            unselectedIconColor = Palette.Muted,
                            unselectedTextColor = Palette.Muted,
                            indicatorColor = Palette.SurfaceHigh,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (Tab.entries[tabIndex]) {
                Tab.Grid -> GridScreen(vm, ui, birth, today)
                Tab.LastTime -> LastTimeScreen(vm, ui, birth, today)
                Tab.Events -> EventsScreen(vm, ui, birth, today, onShowInGrid = { id ->
                    vm.showOnlyEvent(id)
                    tabIndex = Tab.Grid.ordinal
                })
                Tab.Stats -> StatsScreen(vm, ui, birth, today)
                Tab.Settings -> SettingsScreen(vm, ui, birth)
            }
        }
    }
}
