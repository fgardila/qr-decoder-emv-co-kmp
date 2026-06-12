# Reglas de la app. Las librerías (Compose, Hilt, CameraX, ML Kit, ZXing)
# publican sus propias reglas de consumidor; no necesitan keeps manuales aquí.

# kotlinx.serialization: conserva los serializers de las rutas de navegación
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class dev.code93.emvqr.**$$serializer { *; }
-keepclassmembers class dev.code93.emvqr.** {
    *** Companion;
}
-keepclasseswithmembers class dev.code93.emvqr.** {
    kotlinx.serialization.KSerializer serializer(...);
}
