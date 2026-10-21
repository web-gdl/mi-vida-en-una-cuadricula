#!/usr/bin/env bash
# Prueba de arranque en emulador.
#  1. Si existe la versión publicada (previous.apk), la instala y la abre: crea la base de datos vieja.
#  2. Instala la versión nueva ENCIMA (como lo haría un usuario) y la abre: corre las migraciones.
#  3. Falla si la app se cierra o no queda en ejecución.
set -euo pipefail

APK="$1"
PREVIOUS="${2:-}"
PKG="com.webgdl.cuadricula"

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
