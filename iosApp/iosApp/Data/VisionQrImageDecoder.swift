import UIKit
import Vision

/// Detecta y extrae el rawText de un QR dentro de una imagen usando Vision.
struct VisionQrImageDecoder: QrImageDecoding {

    func decodeQr(from image: UIImage) async -> String? {
        guard let cgImage = image.cgImage else { return nil }

        return await withCheckedContinuation { continuation in
            let request = VNDetectBarcodesRequest { request, _ in
                let payload = (request.results as? [VNBarcodeObservation])?
                    .first { $0.symbology == .qr }?
                    .payloadStringValue
                continuation.resume(returning: payload)
            }
            request.symbologies = [.qr]

            let handler = VNImageRequestHandler(cgImage: cgImage)
            DispatchQueue.global(qos: .userInitiated).async {
                do {
                    try handler.perform([request])
                } catch {
                    continuation.resume(returning: nil)
                }
            }
        }
    }
}
