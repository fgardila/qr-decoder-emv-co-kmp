import SwiftUI
import QrdKit

/// Pantalla de escaneo compartida (Compose Multiplatform, módulo
/// `:qrscanner-compose`): cámara KScan con linterna y galería integrada.
struct ComposeQrScannerView: UIViewControllerRepresentable {
    let onResult: (String) -> Void
    let onClose: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        QrScannerViewControllerKt.qrScannerViewController(
            onResult: onResult,
            onClose: onClose
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
