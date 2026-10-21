package com.webgdl.cuadricula.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webgdl.cuadricula.data.WeekEntry
import com.webgdl.cuadricula.domain.WEEKS_PER_ROW

/**
 * Cuadrícula de vida: una fila por año, 52 celdas por fila (una por semana).
 * Verde = vivida, verde brillante = con nota, ámbar = memorable, magenta = última vez, gris = futura,
 * celda pulsante = semana actual, línea blanca = "Hoy".
 */
@Composable
fun LifeGrid(
    rows: Int,
    currentWeek: Int,
    entries: Map<Int, WeekEntry>,
    lastTimeWeeks: Set<Int>,
    layerRowColors: IntArray?,
    zoomed: Boolean,
    verticalScroll: ScrollState,
    viewportHeightPx: Int,
    onWeekClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val labelStyle = remember {
        TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Palette.Muted)
    }
    val hoyStyle = remember {
        TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Palette.Text)
    }
    val ageLabels = remember(rows) {
        (0..rows step 10).map { it to measurer.measure(AnnotatedString(it.toString()), labelStyle) }
    }
    val hoyLayout = remember { measurer.measure(AnnotatedString("Hoy"), hoyStyle) }

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha",
    )

    val leftPad = with(density) { 28.dp.toPx() }
    val rightPad = with(density) { 6.dp.toPx() }
    val topPad = with(density) { 14.dp.toPx() }
    val currentRow = currentWeek / WEEKS_PER_ROW

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val availablePx = with(density) { maxWidth.toPx() }
        val cell = if (zoomed) {
            with(density) { 18.dp.toPx() }
        } else {
            (availablePx - leftPad - rightPad) / WEEKS_PER_ROW
        }
        val widthPx = leftPad + rightPad + cell * WEEKS_PER_ROW
        val heightPx = topPad + cell * rows + with(density) { 8.dp.toPx() }

        // Al abrir, llevar la vista cerca de la fila actual.
        // Si la fila actual ya se ve desde arriba, no se desplaza (así se ven los años 0..10);
        // si queda fuera de pantalla, se centra en la pantalla.
        LaunchedEffect(cell, rows, viewportHeightPx > 0) {
            if (viewportHeightPx > 0) {
                val rowBottom = topPad + (currentRow + 2) * cell
                val target = if (rowBottom <= viewportHeightPx) {
                    0
                } else {
                    (topPad + currentRow * cell - viewportHeightPx / 2f).toInt().coerceAtLeast(0)
                }
                verticalScroll.scrollTo(target)
            }
        }

        val canvasModifier = Modifier
            .width(with(density) { widthPx.toDp() })
            .height(with(density) { heightPx.toDp() })
            .pointerInput(rows, cell, leftPad, topPad) {
                detectTapGestures { offset ->
                    val col = ((offset.x - leftPad) / cell).toInt()
                    val row = ((offset.y - topPad) / cell).toInt()
                    if (offset.x >= leftPad && offset.y >= topPad &&
                        col in 0 until WEEKS_PER_ROW && row in 0 until rows
                    ) {
                        onWeekClick(row * WEEKS_PER_ROW + col)
                    }
                }
            }

        val scrollModifier = if (zoomed) Modifier.horizontalScroll(rememberScrollState()) else Modifier

        Canvas(scrollModifier.then(canvasModifier)) {
            val side = cell * 0.82f
            val inset = (cell - side) / 2f

            // Etiquetas de edad cada 10 años.
            ageLabels.forEach { (age, layout) ->
                val y = topPad + age * cell - layout.size.height / 2f
                drawText(layout, topLeft = Offset(leftPad - layout.size.width - 6f, y.coerceAtLeast(0f)))
            }

            for (row in 0 until rows) {
                val y = topPad + row * cell + inset
                val layer = layerRowColors?.getOrNull(row) ?: 0
                for (col in 0 until WEEKS_PER_ROW) {
                    val index = row * WEEKS_PER_ROW + col
                    val entry = entries[index]
                    val color: Color = when {
                        index == currentWeek -> Palette.Mint.copy(alpha = pulse)
                        index in lastTimeWeeks -> Palette.Magenta
                        entry?.isMemorable == true -> Palette.Amber
                        layer != 0 && entry == null && index < currentWeek ->
                            Color(layer).copy(alpha = 0.75f)
                        layer != 0 && entry == null && index > currentWeek ->
                            Color(layer).copy(alpha = 0.40f)
                        index < currentWeek ->
                            if (entry != null) Palette.Mint else Palette.Mint.copy(alpha = 0.40f)
                        entry != null -> Palette.Mint.copy(alpha = 0.75f)
                        else -> Palette.Slate.copy(alpha = 0.55f)
                    }
                    drawRect(
                        color = color,
                        topLeft = Offset(leftPad + col * cell + inset, y),
                        size = Size(side, side),
                    )
                }
            }

            // Línea "Hoy" al comienzo de la fila actual.
            if (currentRow < rows) {
                val y = topPad + currentRow * cell
                drawLine(
                    color = Color.White,
                    start = Offset(leftPad, y),
                    end = Offset(leftPad + cell * WEEKS_PER_ROW, y),
                    strokeWidth = 2f,
                )
                drawRect(
                    color = Palette.Bg.copy(alpha = 0.85f),
                    topLeft = Offset(leftPad + 2f, (y - hoyLayout.size.height - 2f).coerceAtLeast(0f)),
                    size = Size(hoyLayout.size.width + 8f, hoyLayout.size.height.toFloat()),
                )
                drawText(
                    hoyLayout,
                    topLeft = Offset(leftPad + 6f, (y - hoyLayout.size.height - 2f).coerceAtLeast(0f)),
                )
            }
        }
    }
}
