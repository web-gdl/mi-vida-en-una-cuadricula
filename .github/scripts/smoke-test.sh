#!/usr/bin/env bash
# Prueba de arranque: instala el APK, abre la app y verifica que siga viva sin errores fatales.
set -euo pipefail

APK="$1"
PKG="com.webgdl.cuadricula"

adb install -r "$APK"
adb logcat -c
adb shell am start -W -n "$PKG/.MainActivity"
sleep 15

if adb logcat -d -s AndroidRuntime:E | grep -q "$PKG"; then
  echo "La app se cerró al arrancar:"
  adb logcat -d -s AndroidRuntime:E
  exit 1
fi

if ! adb shell pidof "$PKG" > /dev/null; then
  echo "La app no está corriendo después de abrirla."
  adb logcat -d | tail -200
  exit 1
fi

echo "Prueba de arranque OK: la app abre y se mantiene en ejecución."
