# Reglas de R8 para la versión release.
# Room, Compose, AndroidX y los componentes declarados en el manifest (actividad, receptores,
# widget) ya traen o generan sus propias reglas; no hace falta conservar nada más a mano.

# Quitar las llamadas de log en release.
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
