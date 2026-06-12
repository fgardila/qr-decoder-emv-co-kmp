package dev.code93.emvqr.data.repository

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.code93.emvqr.domain.repository.QrImageRepository
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class MlKitQrImageRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : QrImageRepository {

    override suspend fun decodeFromImage(uri: Uri): String? =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromFilePath(context, uri)
            BarcodeScanning.getClient().process(image)
                .addOnSuccessListener { barcodes ->
                    continuation.resume(barcodes.firstNotNullOfOrNull { it.rawValue })
                }
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
}
