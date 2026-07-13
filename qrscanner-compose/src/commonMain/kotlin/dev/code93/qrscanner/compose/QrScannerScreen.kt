package dev.code93.qrscanner.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import dev.code93.qrscanner.core.QrImageScanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ncgroup.kscan.BarcodeFormat
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerController
import org.ncgroup.kscan.ScannerView

private const val GALLERY_ERROR_VISIBLE_MS = 3000L

/**
 * Pantalla de escaneo de QR multiplataforma: cámara (KScan) con linterna,
 * gestión del permiso de cámara e importación desde galería (Calf) decodificada
 * a raw text.
 *
 * @param onResult recibe el raw text del QR; se invoca una sola vez.
 * @param onClose invocado al pulsar cerrar.
 * @param showGalleryButton oculta el botón de galería si la app ya ofrece su
 *   propio picker (como los demos de este repo en su pantalla principal).
 */
@Composable
public fun QrScannerScreen(
    onResult: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    showGalleryButton: Boolean = true,
) {
    val permission = rememberCameraPermissionState()
    LaunchedEffect(Unit) {
        if (!permission.isGranted) permission.request()
    }

    var delivered by remember { mutableStateOf(false) }
    fun deliver(rawText: String) {
        if (!delivered) {
            delivered = true
            onResult(rawText)
        }
    }

    val scope = rememberCoroutineScope()
    val imageScanner = remember { QrImageScanner() }
    var galleryError by remember { mutableStateOf(false) }
    val platformContext = LocalPlatformContext.current
    val galleryLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Single,
    ) { files ->
        val file = files.firstOrNull() ?: return@rememberFilePickerLauncher
        scope.launch {
            val rawText = imageScanner.scan(file.readByteArray(platformContext))
            if (rawText != null) deliver(rawText) else galleryError = true
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (permission.isGranted) {
            val scannerController = remember { ScannerController() }
            ScannerView(
                modifier = Modifier.fillMaxSize(),
                codeTypes = listOf(BarcodeFormat.FORMAT_QR_CODE),
                scannerUiOptions = null,
                scannerController = scannerController,
            ) { result ->
                if (result is BarcodeResult.OnSuccess) deliver(result.barcode.data)
            }

            ScannerOverlay(
                torchEnabled = scannerController.torchEnabled,
                onToggleTorch = { scannerController.setTorch(!scannerController.torchEnabled) },
                showGalleryButton = showGalleryButton,
                onOpenGallery = { galleryLauncher.launch() },
            )
        } else {
            PermissionRationale(
                onRequest = permission::request,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .safeDrawingPadding()
                .padding(8.dp)
        ) {
            Icon(
                imageVector = ScannerIcons.Close,
                contentDescription = QrScannerStrings.CLOSE,
                tint = if (permission.isGranted) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }

        if (galleryError) {
            LaunchedEffect(galleryError) {
                delay(GALLERY_ERROR_VISIBLE_MS)
                galleryError = false
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding()
                    .padding(bottom = 120.dp)
            ) {
                Text(
                    text = QrScannerStrings.NO_QR_IN_IMAGE,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ScannerOverlay(
    torchEnabled: Boolean,
    onToggleTorch: () -> Unit,
    showGalleryButton: Boolean,
    onOpenGallery: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Text(
            text = QrScannerStrings.CAMERA_TITLE,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(250.dp)
                .border(2.dp, Color.White, RoundedCornerShape(16.dp))
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            OverlayButton(
                onClick = onToggleTorch,
                imageVector = if (torchEnabled) ScannerIcons.FlashOff else ScannerIcons.FlashOn,
                contentDescription = QrScannerStrings.TORCH
            )
            if (showGalleryButton) {
                OverlayButton(
                    onClick = onOpenGallery,
                    imageVector = ScannerIcons.Gallery,
                    contentDescription = QrScannerStrings.GALLERY
                )
            }
        }
    }
}

@Composable
private fun OverlayButton(
    onClick: () -> Unit,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}

@Composable
private fun PermissionRationale(
    onRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = QrScannerStrings.PERMISSION_TITLE,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = QrScannerStrings.PERMISSION_MESSAGE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRequest) {
                Text(QrScannerStrings.PERMISSION_GRANT)
            }
        }
    }
}
