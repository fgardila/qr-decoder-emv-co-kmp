import Foundation

/// Composition root: construye las implementaciones concretas y las entrega a
/// los ViewModels por constructor (inyección sobre protocolos).
struct AppContainer {
    let emvDecoder: EmvDecoding
    let qrImageDecoder: QrImageDecoding
    let qrGenerator: QrGenerating

    static func live() -> AppContainer {
        AppContainer(
            emvDecoder: EmvDecoderRepository(),
            qrImageDecoder: VisionQrImageDecoder(),
            qrGenerator: CoreImageQrGenerator()
        )
    }
}
