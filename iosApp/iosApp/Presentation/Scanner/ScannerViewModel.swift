import PhotosUI
import SwiftUI

enum ScannerState: Equatable {
    case idle
    case decoding
    case success(DecodedQr)
    case error(noQrFound: Bool)
}

@MainActor
final class ScannerViewModel: ObservableObject {
    @Published private(set) var state: ScannerState = .idle
    @Published var isCameraPresented = false

    private let decoder: EmvDecoding
    private let imageDecoder: QrImageDecoding

    init(decoder: EmvDecoding, imageDecoder: QrImageDecoding) {
        self.decoder = decoder
        self.imageDecoder = imageDecoder
    }

    func onQrScanned(_ rawText: String) {
        state = .success(decoder.decode(rawText: rawText))
    }

    func onImageSelected(_ item: PhotosPickerItem?) {
        guard let item else { return }
        state = .decoding
        Task {
            guard let data = try? await item.loadTransferable(type: Data.self),
                  let image = UIImage(data: data) else {
                state = .error(noQrFound: false)
                return
            }
            if let rawText = await imageDecoder.decodeQr(from: image) {
                state = .success(decoder.decode(rawText: rawText))
            } else {
                state = .error(noQrFound: true)
            }
        }
    }

    func onReset() {
        state = .idle
    }
}
