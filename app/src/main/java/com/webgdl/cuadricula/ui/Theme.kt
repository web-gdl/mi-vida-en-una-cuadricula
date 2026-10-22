package com.webgdl.cuadricula.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.webgdl.cuadricula.domain.eventColorArgb

/** Paleta tomada del video: fondo casi negro, verde menta (vivido), magenta (lo que no vuelve). */
object Palette {
    val Bg = Color(0xFF0B1017)
    val Surface = Color(0xFF121A24)
    val SurfaceHigh = Color(0xFF1B2634)
    val Mint = Color(0xFF00F5A0)
    val Magenta = Color(0xFFFF1F7A)
    val Amber = Color(0xFFFFC857)
    val Slate = Color(0xFF3A4A5E)
    val Text = Color(0xFFE6EDF3)
    val Muted = Color(0xFF8A98A8)

    /** Color de la marca del evento periódico en la posición [index] (ver domain/Recurrence.kt). */
    fun eventColor(index: Int): Color = Color(eventColorArgb(index))
}

private val Mono = FontFamily.Monospace
private val base = Typography()

private val MonoTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Mono),
    displayMedium = base.displayMedium.copy(fontFamily = Mono),
    displaySmall = base.displaySmall.copy(fontFamily = Mono),
    headlineLarge = base.headlineLarge.copy(fontFamily = Mono),
    headlineMedium = base.headlineMedium.copy(fontFamily = Mono),
    headlineSmall = base.headlineSmall.copy(fontFamily = Mono),
    titleLarge = base.titleLarge.copy(fontFamily = Mono),
    titleMedium = base.titleMedium.copy(fontFamily = Mono),
    titleSmall = base.titleSmall.copy(fontFamily = Mono),
    bodyLarge = base.bodyLarge.copy(fontFamily = Mono),
    bodyMedium = base.bodyMedium.copy(fontFamily = Mono),
    bodySmall = base.bodySmall.copy(fontFamily = Mono),
    labelLarge = base.labelLarge.copy(fontFamily = Mono),
    labelMedium = base.labelMedium.copy(fontFamily = Mono),
    labelSmall = base.labelSmall.copy(fontFamily = Mono),
)

@Composable
fun CuadriculaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Palette.Mint,
            onPrimary = Palette.Bg,
            secondary = Palette.Magenta,
            onSecondary = Palette.Bg,
            background = Palette.Bg,
            onBackground = Palette.Text,
            surface = Palette.Surface,
            onSurface = Palette.Text,
            surfaceVariant = Palette.SurfaceHigh,
            onSurfaceVariant = Palette.Muted,
            surfaceContainerHigh = Palette.SurfaceHigh,
            surfaceContainer = Palette.Surface,
            outline = Palette.Slate,
        ),
        typography = MonoTypography,
        content = content,
    )
}
