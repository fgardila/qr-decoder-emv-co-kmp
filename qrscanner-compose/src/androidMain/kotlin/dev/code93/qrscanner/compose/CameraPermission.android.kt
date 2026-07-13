package dev.code93.qrscanner.compose

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
internal actual fun rememberCameraPermissionState(): CameraPermissionState {
    val context = LocalContext.current
    val granted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> granted.value = isGranted }

    return remember {
        object : CameraPermissionState {
            override val isGranted: Boolean get() = granted.value
            override fun request() {
                launcher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
