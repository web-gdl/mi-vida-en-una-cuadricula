package com.webgdl.cuadricula.domain

/** Colores disponibles para las capas (ARGB opacos). Ninguno se parece a los de estado de la cuadrícula. */
val LAYER_COLORS: List<Int> = listOf(
    0xFF4DA3FF.toInt(), // azul
    0xFFB07CFF.toInt(), // violeta
    0xFFFF8A4C.toInt(), // naranja
    0xFF2EC4B6.toInt(), // turquesa
    0xFFA3D94C.toInt(), // lima
    0xFFE5A0A0.toInt(), // rosa pálido
)

/** Una etapa de la vida entre dos edades: la fila de edad [fromAge] hasta antes de [toAge]. */
data class LayerRange(val fromAge: Int, val toAge: Int, val color: Int)

/**
 * Color de capa para cada fila (edad) de la cuadrícula; 0 = sin capa. Si varias capas se
 * superponen gana la primera de la lista.
 */
fun layerRowColors(rows: Int, layers: List<LayerRange>): IntArray {
    val result = IntArray(rows)
    for (row in 0 until rows) {
        result[row] = layers.firstOrNull { row >= it.fromAge && row < it.toAge }?.color ?: 0
    }
    return result
}
