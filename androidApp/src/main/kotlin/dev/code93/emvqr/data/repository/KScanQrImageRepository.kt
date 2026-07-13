package dev.code93.emvqr.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.code93.emvqr.domain.repository.QrImageRepository
import dev.code93.qrscanner.core.QrImageScanning
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Decodifica el QR de una imagen de galería delegando en el módulo KMP
 * `:qrscanner-core` (KScan → ML Kit en Android).
 */
class KScanQrImageRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val scanner: QrImageScanning
) : QrImageRepository {

    override suspend fun decodeFromImage(uri: Uri): String? =
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)
                ?.use { it.readBytes() }
                ?.let { scanner.scan(it) }
        }
}
