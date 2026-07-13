package dev.code93.qrscanner.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
internal actual fun rememberCameraPermissionState(): CameraPermissionState {
    val granted = remember {
        mutableStateOf(
            AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) ==
                AVAuthorizationStatusAuthorized
        )
    }

    return remember {
        object : CameraPermissionState {
            override val isGranted: Boolean get() = granted.value
            override fun request() {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { isGranted ->
                    dispatch_async(dispatch_get_main_queue()) {
                        granted.value = isGranted
                    }
                }
            }
        }
    }
}
