import CoreImage.CIFilterBuiltins
import UIKit

/// Renderiza un rawText como imagen QR con CoreImage (sin dependencias).
struct CoreImageQrGenerator: QrGenerating {

    func generateImage(from rawText: String) -> UIImage? {
        let filter = CIFilter.qrCodeGenerator()
        filter.message = Data(rawText.utf8)
        filter.correctionLevel = "M"

        guard let output = filter.outputImage else { return nil }

        // Escalado nearest-neighbor para módulos nítidos
        let scale: CGFloat = 12
        let scaled = output.transformed(by: CGAffineTransform(scaleX: scale, y: scale))

        let context = CIContext()
        guard let cgImage = context.createCGImage(scaled, from: scaled.extent) else { return nil }
        return UIImage(cgImage: cgImage)
    }
}
