package com.webgdl.cuadricula.domain

/** Separa por comas, recorta, pasa a minúsculas y quita vacíos y repetidos. */
fun parseTags(raw: String): List<String> =
    raw.split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct()

/** Forma canónica para guardar: "trabajo, amor". */
fun normalizeTags(raw: String): String = parseTags(raw).joinToString(", ")
