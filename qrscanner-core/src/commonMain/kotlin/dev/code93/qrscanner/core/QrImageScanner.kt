package dev.code93.qrscanner.core

import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import org.ncgroup.kscan.BarcodeFormat
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.scanImage

/**
 * Implementación de [QrImageScanning] sobre KScan
 * (ML Kit en Android, Vision en iOS).
 */
public class QrImageScanner : QrImageScanning {

    override suspend fun scan(imageBytes: ByteArray): String? =
        suspendCancellableCoroutine { continuation ->
            scanImage(
                imageBytes = imageBytes,
                codeTypes = listOf(BarcodeFormat.FORMAT_QR_CODE),
            ) { result ->
                if (continuation.isActive) {
                    continuation.resume(result.rawTextOrNull())
                }
            }
        }
}

internal fun BarcodeResult.rawTextOrNull(): String? =
    when (this) {
        is BarcodeResult.OnSuccess -> barcode.data.takeIf { it.isNotEmpty() }
        is BarcodeResult.OnFailed -> null
        BarcodeResult.OnCanceled -> null
    }
