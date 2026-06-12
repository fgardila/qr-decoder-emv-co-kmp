import UIKit

/// Decodificación EMV del rawText de un QR.
protocol EmvDecoding {
    func decode(rawText: String) -> DecodedQr
}

/// Extracción del rawText de un QR contenido en una imagen.
protocol QrImageDecoding {
    func decodeQr(from image: UIImage) async -> String?
}

/// Render de un rawText como imagen QR.
protocol QrGenerating {
    func generateImage(from rawText: String) -> UIImage?
}
