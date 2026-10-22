#!/usr/bin/env bash
# Prueba en emulador.
#  1. Si existe la versión publicada (previous.apk), la instala y la abre: crea la base de datos vieja.
#  2. Instala la versión nueva ENCIMA (como lo haría un usuario) y la abre: corre las migraciones.
#  3. Falla si la app se cierra o no queda en ejecución.
#  4. Recorrido de la interfaz con datos de ejemplo y capturas de pantalla en screenshots/
#     (informativo: si un paso del recorrido no encuentra un botón, no hace fallar la prueba).
set -euo pipefail

APK="$1"
PREVIOUS="${2:-}"
PKG="com.webgdl.cuadricula"
SCRIPTS="$(dirname "$0")"
mkdir -p screenshots

check_alive() {
  local label="$1"
  if adb logcat -d -s AndroidRuntime:E | grep -q "$PKG"; then
    echo "[$label] La app se cerró:"
    adb logcat -d -s AndroidRuntime:E
    exit 1
  fi
  if ! adb shell pidof "$PKG" > /dev/null; then
    echo "[$label] La app no está corriendo después de abrirla."
    adb logcat -d | tail -200
    exit 1
  fi
  echo "[$label] OK: la app abre y se mantiene en ejecución."
}

shot() {
  sleep 2
  adb exec-out screencap -p > "screenshots/$1.png" || true
}

swipe_up() {
  local size w h
  size="$(adb shell wm size | grep -oE '[0-9]+x[0-9]+' | tail -1)"
  w="${size%x*}"; h="${size#*x}"
  adb shell input swipe $((w / 2)) $((h * 3 / 4)) $((w / 2)) $((h / 4)) 300
  sleep 1
}

find_xy() {
  adb shell uiautomator dump /sdcard/ui.xml > /dev/null 2>&1 || true
  adb pull /sdcard/ui.xml /tmp/ui.xml > /dev/null 2>&1 || true
  python3 "$SCRIPTS/find_node.py" /tmp/ui.xml "$1" 2>/dev/null || true
}

# Toca el elemento con ese texto; si no se ve, desplaza hacia abajo hasta 4 veces.
tap() {
  local xy attempt
  for attempt in 0 1 2 3 4; do
    xy="$(find_xy "$1")"
    if [ -n "$xy" ]; then
      # shellcheck disable=SC2086
      adb shell input tap $xy
      sleep 2
      return 0
    fi
    [ "$attempt" -lt 4 ] && swipe_up
  done
  echo "  (recorrido) no encontré: $1"
  return 1
}

if [ -n "$PREVIOUS" ] && [ -f "$PREVIOUS" ]; then
  adb install "$PREVIOUS"
  adb logcat -c
  adb shell am start -W -n "$PKG/.MainActivity"
  sleep 10
  check_alive "versión publicada"
  adb shell am force-stop "$PKG"
fi

adb install -r "$APK"
adb logcat -c
adb shell am start -W -n "$PKG/.MainActivity"
sleep 15
check_alive "versión nueva"

# --- Recorrido con instalación limpia y datos de ejemplo ---
set +e
adb uninstall "$PKG" > /dev/null
adb install "$APK" > /dev/null
adb shell am start -W -n "$PKG/.MainActivity" > /dev/null
sleep 6
shot 01-inicio
tap "Elegir fecha de nacimiento"
tap "text input mode" || tap "entrada de texto"
tap "@edit" && adb shell input text "01151990"
sleep 1
tap "Aceptar"
shot 02-fecha
tap "Ver mi cuadrícula"
sleep 3
shot 03-cuadricula-vacia
tap "Cifras"
tap "Agregar capa" && tap "Trabajo 25" && tap "Guardar"
tap "Eventos"
tap "Agregar evento" && tap "Ir a la playa" && tap "Guardar"
tap "Agregar evento" && tap "Reunión con amigos" && tap "Guardar"
tap "Hoy"
shot 04-eventos
tap "Vida"
sleep 3
shot 05-cuadricula-con-eventos
tap "Ver solo este" > /dev/null 2>&1
tap "Eventos" && tap "Ver solo este"
sleep 3
shot 06-cuadricula-solo-un-evento
set -e

check_alive "recorrido"
ls -l screenshots
