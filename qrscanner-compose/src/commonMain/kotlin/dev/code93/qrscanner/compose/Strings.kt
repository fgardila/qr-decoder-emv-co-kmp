package dev.code93.qrscanner.compose

/**
 * Textos de la pantalla de escaneo. Constantes en español (convención del
 * proyecto); sin compose-resources en esta primera versión.
 */
internal object QrScannerStrings {
    const val CAMERA_TITLE = "Apunta al código QR"
    const val PERMISSION_TITLE = "Se necesita acceso a la cámara"
    const val PERMISSION_MESSAGE =
        "La cámara se usa únicamente para escanear códigos QR. " +
            "No se capturan ni almacenan imágenes."
    const val PERMISSION_GRANT = "Conceder permiso"
    const val CLOSE = "Cerrar cámara"
    const val TORCH = "Linterna"
    const val GALLERY = "Elegir de la galería"
    const val NO_QR_IN_IMAGE = "No se encontró un código QR en la imagen seleccionada."
}
