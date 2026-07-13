package dev.code93.qrscanner.core

/**
 * Decodifica el primer código QR presente en una imagen estática
 * (galería, screenshot, descarga) y devuelve su raw text.
 */
public interface QrImageScanning {

    /**
     * @param imageBytes bytes de la imagen (PNG, JPEG).
     * @return el raw text del primer QR encontrado, o `null` si la imagen
     *   no contiene un código QR legible.
     */
    public suspend fun scan(imageBytes: ByteArray): String?
}
