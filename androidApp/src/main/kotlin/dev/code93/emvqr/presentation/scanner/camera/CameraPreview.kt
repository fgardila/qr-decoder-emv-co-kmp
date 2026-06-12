package dev.code93.emvqr.presentation.scanner.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Preview de CameraX con análisis ML Kit en tiempo real. Emite una sola vez
 * por pantalla: el primer QR con contenido detiene el análisis.
 */
@Composable
fun CameraPreview(
    onQrDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }
    val cameraController = remember { LifecycleCameraController(context) }

    DisposableEffect(Unit) {
        onDispose {
            cameraController.unbind()
            barcodeScanner.close()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            val previewView = PreviewView(viewContext)
            val executor = ContextCompat.getMainExecutor(viewContext)
            var delivered = false

            cameraController.setImageAnalysisAnalyzer(
                executor,
                MlKitAnalyzer(
                    listOf(barcodeScanner),
                    ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                    executor
                ) { result ->
                    val rawValue = result.getValue(barcodeScanner)
                        ?.firstNotNullOfOrNull { it.rawValue }
                    if (rawValue != null && !delivered) {
                        delivered = true
                        cameraController.clearImageAnalysisAnalyzer()
                        onQrDetected(rawValue)
                    }
                }
            )

            cameraController.bindToLifecycle(lifecycleOwner)
            previewView.controller = cameraController
            previewView
        }
    )
}
