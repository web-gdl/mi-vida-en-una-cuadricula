# Mi vida en una cuadrícula

App nativa para Android (Kotlin + Jetpack Compose) basada en el video "Vida en semanas": 80 años × 52 semanas = 4 160 cuadros, una fila por año, un cuadro por semana.

## Cómo abrirla
1. Android Studio (Ladybug 2024.2 o más nuevo) → *Open* → carpeta `MiVidaEnUnaCuadricula`.
2. Espera a que Gradle sincronice (genera el wrapper y descarga dependencias).
3. Ejecuta en un dispositivo o emulador con Android 8.0+ (API 26).
4. Pruebas de la lógica de semanas: `./gradlew test`.

## Qué hace (v1.0)
- **Onboarding:** fecha de nacimiento y esperanza de vida editable (30–120 años, 80 por defecto).
- **Cuadrícula:** vividas en verde, semana actual pulsante, futuras en gris, línea "Hoy", etiquetas de edad cada 10 años. Botón *Ampliar* para tocar celdas con precisión.
- **Registro semanal:** al tocar una semana: estado de ánimo (1–5), una nota y marca de *memorable* (ámbar). Flechas para moverse entre semanas.
- **La última vez:** pestaña para anotar momentos que ya ocurrieron por última vez ("alguien te cargó en brazos"); se marcan en magenta en la cuadrícula y aparecen en el detalle de su semana. Las semanas memorables ahora se ven en ámbar.
- **Cifras:** semanas vividas y restantes, cuánto pesa un año hoy (100 / edad), semanas como pasos (120 por minuto) y calculadora de "cuántas veces te quedan".
- **Ajustes:** cambiar fecha de nacimiento y esperanza de vida, recordatorio cada lunes a las 9:00.

## Regla de la semana
fila = años cumplidos; columna = `min(51, díasDesdeCumpleaños / 7)`. Los 1–2 días sobrantes de cada año caen en la última columna, así cada fila coincide con un año de edad. Ver `domain/LifeCalc.kt`.

## Estructura
```
app/src/main/java/com/webgdl/cuadricula/
  domain/LifeCalc.kt        cálculo de semanas (puro, con tests)
  data/                     Room (WeekEntry) y DataStore (ajustes)
  reminder/Reminder.kt      WorkManager + notificación semanal
  ui/                       tema, LifeGrid (Canvas), pantallas
```

## Pendiente (versiones siguientes)
Capas por categoría (trabajo/sueño), módulo "Tiempo con personas", widget de inicio, exportar/importar, etiquetas.

## Nota
Esta versión se escribió sin poder compilar en el entorno de desarrollo (sin Android SDK). Si Gradle marca algún error al sincronizar, es lo primero que hay que revisar.
