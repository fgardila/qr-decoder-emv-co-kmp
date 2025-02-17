package dev.code93.kmp.qrd.android.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dev.code93.kmp.qrd.android.MyApplicationTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.Triple

class CameraGoogleReaderActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner
    private lateinit var mediaPicker: ActivityResultLauncher<PickVisualMediaRequest>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        mediaPicker = registerForActivityResult(PickVisualMedia()) { uri ->
            if (uri != null) {
                processUriMediaPicker(uri)
            }
        }

        if (allPermissionsGranted()) {
            setContent {
                MyApplicationTheme {
                    CameraScreen(
                        onQrScanned = { qrResult ->
                            val intent = Intent()
                            intent.putExtra(CONTENT, qrResult)
                            setResult(RESULT_OK, intent)
                            finish()
                        },
                        onPickImage = {
                            mediaPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                        }
                    )
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun processUriMediaPicker(uri: Uri) {

        val image = InputImage.fromFilePath(this, uri)

        // Create BarcodeScanning Cliente
        val barcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val qrValue = barcodes.firstOrNull()?.rawValue
                    if (!qrValue.isNullOrEmpty()) {
                        val intent = Intent()
                        intent.putExtra(CONTENT, qrValue)
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        finish()
                    }
                } else {
                    // Mostrar AlertDialog indicando que no se encontraron códigos QR
                    alertDialogState.value = Triple(true, "No se encontro un QR", "Revisa la imagen y asegurate de que contenga un codigo QR. Intenta de nuevo.")
                }
            }
            .addOnFailureListener {
                finish()
            }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        val alertDialogState = mutableStateOf(Triple(false, "", ""))
        private const val TAG = "CameraX-MLKit"
        private const val REQUEST_CODE_PERMISSIONS = 10
        const val CONTENT = "content"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).toTypedArray()

        fun newInstance(context: Context) = Intent(context, CameraGoogleReaderActivity::class.java)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                setContent {
                    MyApplicationTheme {
                        CameraScreen(
                            onQrScanned = { qrResult ->
                                val intent = Intent()
                                intent.putExtra(CONTENT, qrResult)
                                setResult(RESULT_OK, intent)
                                finish()
                            },
                            onPickImage = {
                                mediaPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                            }
                        )
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}

@Composable
fun CameraScreen(onQrScanned: (String?) -> Unit, onPickImage: () -> Unit) {

    val alertDialogState = CameraGoogleReaderActivity.alertDialogState

    val context = LocalContext.current
    val cameraController = LifecycleCameraController(context)
    val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    controller = cameraController
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 60.dp)
                .align(Alignment.Center)
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(2.dp, Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
        )

        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(context)
            ) { result: MlKitAnalyzer.Result? ->
                val barcodes = result?.getValue(barcodeScanner)
                if (!barcodes.isNullOrEmpty()) {
                    onQrScanned(barcodes.firstOrNull()?.rawValue)
                }
            }
        )
        cameraController.bindToLifecycle(LocalContext.current as ComponentActivity)

        Button(
            onClick = onPickImage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(text = "Pick Image")
        }
    }

    if (alertDialogState.value.first) {
        AlertDialog(
            onDismissRequest = {
                alertDialogState.value = alertDialogState.value.copy(first = false)
            },
            title = { Text(text = alertDialogState.value.second) },
            text = { Text(text = alertDialogState.value.third) },
            confirmButton = {
                Button(onClick = {
                    alertDialogState.value = alertDialogState.value.copy(first = false)
                }) {
                    Text("Aceptar")
                }
            }
        )
    }
}