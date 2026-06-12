import UIKit
import Vision

/// Detecta y extrae el rawText de un QR dentro de una imagen usando Vision.
struct VisionQrImageDecoder: QrImageDecoding {

    /// `perform` es síncrono: se ejecuta fuera del main thread y se leen los
    /// resultados al terminar. Sin continuations — un único camino de retorno
    /// (usar el completion handler del request + catch del perform produce
    /// doble resume cuando Vision falla, p. ej. en el simulador).
    func decodeQr(from image: UIImage) async -> String? {
        guard let cgImage = image.cgImage else { return nil }

        return await Task.detached(priority: .userInitiated) {
            let request = VNDetectBarcodesRequest()
            request.symbologies = [.qr]

            let handler = VNImageRequestHandler(cgImage: cgImage)
            do {
                try handler.perform([request])
            } catch {
                return nil
            }
            return request.results?
                .first { $0.symbology == .qr }?
                .payloadStringValue
        }.value
    }
}
