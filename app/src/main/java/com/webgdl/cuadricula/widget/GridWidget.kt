package com.webgdl.cuadricula.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.RemoteViews
import com.webgdl.cuadricula.CuadriculaApp
import com.webgdl.cuadricula.MainActivity
import com.webgdl.cuadricula.R
import com.webgdl.cuadricula.data.WeekEntry
import com.webgdl.cuadricula.domain.LAYER_COLORS
import com.webgdl.cuadricula.domain.LayerRange
import com.webgdl.cuadricula.domain.LifeCalc
import com.webgdl.cuadricula.domain.layerRowColors
import com.webgdl.cuadricula.domain.WEEKS_PER_ROW
import java.time.LocalDate
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Widget de inicio: la cuadrícula de vida completa, dibujada en un bitmap. */
class GridWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                GridWidget.updateAll(context)
            } finally {
                pending.finish()
            }
        }
    }
}

object GridWidget {
    private const val BITMAP_W = 480
    private const val BITMAP_H = 600

    /** Redibuja todos los widgets colocados. Nunca lanza: un fallo del widget no debe afectar a la app. */
    suspend fun updateAll(context: Context) = withContext(Dispatchers.Default) {
        try {
            val app = context.applicationContext as CuadriculaApp
            val manager = AppWidgetManager.getInstance(app)
            val ids = manager.getAppWidgetIds(ComponentName(app, GridWidgetProvider::class.java))
            if (ids.isEmpty()) return@withContext

            val settings = app.settingsRepository.settings.first()
            val birth = settings.birthDate
            val entries = app.database.weekDao().observeAll().first().associateBy { it.weekIndex }
            val lastTimes = app.database.lastTimeDao().observeAll().first()
            val layers = app.database.layerDao().observeAll().first()

            val open = PendingIntent.getActivity(
                app, 0, Intent(app, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

            val views = RemoteViews(app.packageName, R.layout.widget_grid)
            views.setOnClickPendingIntent(R.id.widget_root, open)

            if (birth == null) {
                views.setTextViewText(R.id.widget_title, "Abre la app para empezar")
                views.setViewVisibility(R.id.widget_image, View.GONE)
            } else {
                val rows = settings.lifeExpectancy
                val stats = LifeCalc.stats(birth, LocalDate.now(), rows)
                val lastTimeWeeks = lastTimes
                    .map { LifeCalc.weekIndexOf(birth, LocalDate.ofEpochDay(it.epochDay)) }
                    .filter { it >= 0 }
                    .toSet()
                val layerColors = if (settings.showLayers && layers.isNotEmpty()) {
                    layerRowColors(
                        rows,
                        layers.map {
                            LayerRange(it.fromAge, it.toAge, LAYER_COLORS[it.colorIndex.coerceIn(0, LAYER_COLORS.size - 1)])
                        },
                    )
                } else {
                    null
                }
                views.setTextViewText(
                    R.id.widget_title,
                    "Semana ${stats.currentWeek + 1} de ${stats.totalWeeks} · ${(stats.percentLived * 100).toInt()} %",
                )
                views.setViewVisibility(R.id.widget_image, View.VISIBLE)
                views.setImageViewBitmap(
                    R.id.widget_image,
                    GridRenderer.render(BITMAP_W, BITMAP_H, rows, stats.currentWeek, entries, lastTimeWeeks, layerColors),
                )
            }
            ids.forEach { manager.updateAppWidget(it, views) }
        } catch (e: Exception) {
            // Se ignora: el widget se reintentará en la próxima actualización.
        }
    }
}

/** Mismos colores que la cuadrícula de la app (sin animación). */
object GridRenderer {
    private val Mint = Color.rgb(0, 245, 160)
    private val MintDim = Color.argb(102, 0, 245, 160)
    private val Magenta = Color.rgb(255, 31, 122)
    private val Amber = Color.rgb(255, 200, 87)
    private val Slate = Color.argb(140, 58, 74, 94)

    private fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha shl 24)

    fun render(
        widthPx: Int,
        heightPx: Int,
        rows: Int,
        currentWeek: Int,
        entries: Map<Int, WeekEntry>,
        lastTimeWeeks: Set<Int>,
        layerRowColors: IntArray?,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        val cell = min(widthPx / WEEKS_PER_ROW.toFloat(), heightPx / rows.toFloat())
        val gridW = cell * WEEKS_PER_ROW
        val gridH = cell * rows
        val left = (widthPx - gridW) / 2f
        val top = (heightPx - gridH) / 2f
        val side = cell * 0.8f
        val inset = (cell - side) / 2f

        for (row in 0 until rows) {
            val y = top + row * cell + inset
            val layer = layerRowColors?.getOrNull(row) ?: 0
            for (col in 0 until WEEKS_PER_ROW) {
                val index = row * WEEKS_PER_ROW + col
                val entry = entries[index]
                paint.color = when {
                    index == currentWeek -> Color.WHITE
                    index in lastTimeWeeks -> Magenta
                    entry?.isMemorable == true -> Amber
                    layer != 0 && entry == null && index < currentWeek -> withAlpha(layer, 190)
                    layer != 0 && entry == null && index > currentWeek -> withAlpha(layer, 100)
                    index < currentWeek -> if (entry != null) Mint else MintDim
                    entry != null -> Color.argb(190, 0, 245, 160)
                    else -> Slate
                }
                val x = left + col * cell + inset
                canvas.drawRect(x, y, x + side, y + side, paint)
            }
        }

        // Línea "Hoy" al comienzo de la fila actual.
        val currentRow = currentWeek / WEEKS_PER_ROW
        if (currentRow < rows) {
            paint.color = Color.WHITE
            val y = top + currentRow * cell
            canvas.drawRect(left, y - 1f, left + gridW, y + 1f, paint)
        }
        return bitmap
    }
}
