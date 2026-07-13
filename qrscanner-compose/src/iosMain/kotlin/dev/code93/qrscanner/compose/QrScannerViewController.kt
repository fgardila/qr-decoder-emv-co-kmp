package dev.code93.qrscanner.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Entry point para SwiftUI: envuelve [QrScannerScreen] en un UIViewController
 * (vía Compose Multiplatform) listo para presentar con
 * `UIViewControllerRepresentable`.
 */
public fun qrScannerViewController(
    onResult: (String) -> Unit,
    onClose: () -> Unit,
): UIViewController = ComposeUIViewController {
    MaterialTheme {
        QrScannerScreen(onResult = onResult, onClose = onClose)
    }
}
