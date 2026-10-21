# Mi vida en una cuadrícula

[![Descargar APK](https://img.shields.io/badge/%E2%AC%87%EF%B8%8F%20Descargar-APK%20para%20Android-00F5A0?style=for-the-badge&labelColor=0B1017)](https://github.com/web-gdl/mi-vida-en-una-cuadricula/releases/download/latest/MiVidaEnUnaCuadricula.apk)

**[⬇️ Descargar MiVidaEnUnaCuadricula.apk](https://github.com/web-gdl/mi-vida-en-una-cuadricula/releases/download/latest/MiVidaEnUnaCuadricula.apk)** · Android 8.0 o superior · [página del release](https://github.com/web-gdl/mi-vida-en-una-cuadricula/releases/tag/latest)

Al abrirlo, Android pedirá permitir la instalación de apps de origen desconocido. El APK se actualiza solo con cada cambio en `main`.

App nativa para Android (Kotlin + Jetpack Compose) basada en el video "Vida en semanas": 80 años × 52 semanas = 4 160 cuadros, una fila por año, un cuadro por semana.

## Cómo abrirla
1. Android Studio (Ladybug 2024.2 o más nuevo) → *Open* → carpeta `MiVidaEnUnaCuadricula`.
2. Espera a que Gradle sincronice (genera el wrapper y descarga dependencias).
3. Ejecuta en un dispositivo o emulador con Android 8.0+ (API 26).
4. Pruebas de la lógica de semanas: `./gradlew test`.

## Qué hace
- **Onboarding:** fecha de nacimiento y esperanza de vida editable (30–120 años, 80 por defecto).
- **Cuadrícula:** vividas en verde, semana actual pulsante, futuras en gris, línea "Hoy", etiquetas de edad cada 10 años. Botón *Ampliar* para tocar celdas con precisión.
- **Registro semanal:** al tocar una semana: estado de ánimo (1–5), una nota y marca de *memorable* (ámbar). Flechas para moverse entre semanas.
- **La última vez:** pestaña para anotar momentos que ya ocurrieron por última vez ("alguien te cargó en brazos"); se marcan en magenta en la cuadrícula y aparecen en el detalle de su semana. Las semanas memorables ahora se ven en ámbar.
- **Capas de tu vida** (dentro de Cifras): etapas entre dos edades (escuela, trabajo…) con color propio, pintadas sobre la cuadrícula y en el widget; cada una muestra cuántas semanas te quedan. Se activan o desactivan con el botón *Capas*.
- **Etiquetas:** cada semana admite etiquetas separadas por coma; Cifras muestra las más usadas.
- **Tiempo con personas** (dentro de Cifras): agrega a alguien con los días al año que lo ves y los años que calculas que les quedan; la app muestra los días que te quedan juntos.
- **Cifras:** semanas vividas y restantes, cuánto pesa un año hoy (100 / edad), semanas como pasos (120 por minuto) y calculadora de "cuántas veces te quedan".
- **Widget de inicio:** la cuadrícula completa en la pantalla principal (mantén pulsado el inicio → Widgets → Mi vida en una cuadrícula). Se actualiza al guardar algo en la app y cada 6 horas; al tocarlo abre la app.
- **Ajustes:** cambiar fecha de nacimiento y esperanza de vida, recordatorio cada lunes a las 9:00 y **respaldo**: exportar todo a un archivo JSON e importarlo después (reemplaza los datos actuales, solo si el archivo es válido).

## Regla de la semana
fila = años cumplidos; columna = `min(51, díasDesdeCumpleaños / 7)`. Los 1–2 días sobrantes de cada año caen en la última columna, así cada fila coincide con un año de edad. Ver `domain/LifeCalc.kt`.

## Estructura
```
app/src/main/java/com/webgdl/cuadricula/
  domain/LifeCalc.kt        cálculo de semanas (puro, con tests)
  data/                     Room (semanas, última vez, personas), DataStore (ajustes), Backup
  reminder/Reminder.kt      WorkManager + notificación semanal
  ui/                       tema, LifeGrid (Canvas), pantallas
  widget/GridWidget.kt      widget de inicio
```

## Ideas para más adelante
Filtrar la cuadrícula por etiqueta y la gráfica de horas al día según la edad del video. Para publicar en Play Store haría falta además un APK firmado de release con una llave propia.

## Compilación
GitHub Actions compila en cada push a `main` (`.github/workflows/build-apk.yml`): corre las pruebas, genera el APK de depuración y lo publica en el release `latest`.
