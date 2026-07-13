import UIKit
import QrdKit

/// Detecta y extrae el rawText de un QR dentro de una imagen delegando en el
/// módulo KMP `:qrscanner-core` (KScan → Vision).
struct QrdKitQrImageDecoder: QrImageDecoding {

    func decodeQr(from image: UIImage) async -> String? {
        guard let data = image.jpegData(compressionQuality: 0.9) else { return nil }
        return try? await QrImageScanner().scan(data: data)
    }
}
