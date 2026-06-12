package dev.code93.emvqr.domain.repository

import android.net.Uri

interface QrImageRepository {
    /** Extrae el rawText del primer QR encontrado en la imagen, o null si no hay. */
    suspend fun decodeFromImage(uri: Uri): String?
}
