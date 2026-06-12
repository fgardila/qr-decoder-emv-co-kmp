import SwiftUI

@MainActor
final class GenerateQrViewModel: ObservableObject {
    @Published var network: PaymentNetwork = .redeban
    @Published var keyType: PaymentKeyType? = .alphanumeric
    @Published var keyValue = ""
    @Published var merchantCode = ""
    @Published var merchantName = ""
    @Published var merchantCity = ""
    @Published var amount = ""

    @Published private(set) var generatedImage: UIImage?
    @Published private(set) var generatedRawText: String?
    @Published private(set) var validationMessage: String?

    private let generator: QrGenerating

    init(generator: QrGenerating) {
        self.generator = generator
    }

    func generate() {
        validationMessage = nil
        let params = QrGenerationParams(
            network: network,
            keyType: keyValue.isEmpty ? nil : keyType,
            keyValue: keyValue.isEmpty ? nil : keyValue,
            merchantCode: merchantCode.isEmpty ? nil : merchantCode,
            merchantName: merchantName,
            merchantCity: merchantCity,
            amount: amount.isEmpty ? nil : amount
        )
        do {
            let rawText = try EaspbvQrBuilder.build(params)
            generatedRawText = rawText
            generatedImage = generator.generateImage(from: rawText)
        } catch {
            generatedRawText = nil
            generatedImage = nil
            validationMessage = error.localizedDescription
        }
    }
}
