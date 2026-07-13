package dev.code93.qrscanner.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * Estado del permiso de cámara. KScan asume el permiso concedido, así que la
 * pantalla lo gestiona con este expect/actual (sin librerías de permisos).
 */
@Stable
internal interface CameraPermissionState {
    val isGranted: Boolean
    fun request()
}

@Composable
internal expect fun rememberCameraPermissionState(): CameraPermissionState
