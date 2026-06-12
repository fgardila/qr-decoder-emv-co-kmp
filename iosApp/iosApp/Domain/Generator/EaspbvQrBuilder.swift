import Foundation

enum QrGenerationError: LocalizedError {
    case missingKeyOrMerchantCode
    case invalidName
    case invalidCity
    case invalidAmount

    var errorDescription: String? {
        switch self {
        case .missingKeyOrMerchantCode: return "Ingresa una llave o un código de comercio"
        case .invalidName: return "Nombre obligatorio, máximo 25 caracteres"
        case .invalidCity: return "Ciudad obligatoria, máximo 15 caracteres"
        case .invalidAmount: return "Monto inválido (solo dígitos y hasta 2 decimales)"
        }
    }
}

/// Construye el rawText de un QR estático según el estándar EASPBV v1.4-2025.
/// Espejo del builder de la app Android: tags 00, 01, 26, 49, 50, 52, 53,
/// 54 (opcional), 58, 59, 60, 62 y 63 (CRC).
enum EaspbvQrBuilder {

    private static let terminalLabel = "TERMQR01"
    private static let purposePurchase = "00"

    static func build(_ params: QrGenerationParams) throws -> String {
        try validate(params)
        let tlv = TlvEncoder.tlv

        var payload = ""
        payload += tlv("00", "01")
        payload += tlv("01", "11")

        if let keyType = params.keyType,
           let keyValue = params.keyValue?.trimmingCharacters(in: .whitespaces),
           !keyValue.isEmpty {
            payload += tlv(
                "26",
                tlv("00", "\(params.network.guidPrefix).LLA") + tlv(keyType.subTag, keyValue)
            )
        }

        payload += tlv(
            "49",
            tlv("00", "\(params.network.guidPrefix).RED") + tlv("01", params.network.networkId)
        )

        if let merchantCode = params.merchantCode?.trimmingCharacters(in: .whitespaces),
           !merchantCode.isEmpty {
            payload += tlv(
                "50",
                tlv("00", "\(params.network.guidPrefix).CU") + tlv("01", merchantCode)
            )
        }

        payload += tlv("52", params.mcc)
        payload += tlv("53", "170")
        if let amount = params.amount?.trimmingCharacters(in: .whitespaces), !amount.isEmpty {
            payload += tlv("54", amount)
        }
        payload += tlv("58", "CO")
        payload += tlv("59", params.merchantName.trimmingCharacters(in: .whitespaces))
        payload += tlv("60", params.merchantCity.trimmingCharacters(in: .whitespaces))
        payload += tlv("62", tlv("07", terminalLabel) + tlv("08", purposePurchase))

        let toChecksum = payload + "6304"
        return toChecksum + Crc16CcittFalse.checksum(toChecksum)
    }

    private static func validate(_ params: QrGenerationParams) throws {
        let hasKey = params.keyType != nil
            && !(params.keyValue?.trimmingCharacters(in: .whitespaces).isEmpty ?? true)
        let hasMerchantCode = !(params.merchantCode?.trimmingCharacters(in: .whitespaces).isEmpty ?? true)
        guard hasKey || hasMerchantCode else { throw QrGenerationError.missingKeyOrMerchantCode }

        let name = params.merchantName.trimmingCharacters(in: .whitespaces)
        guard !name.isEmpty, name.count <= 25 else { throw QrGenerationError.invalidName }

        let city = params.merchantCity.trimmingCharacters(in: .whitespaces)
        guard !city.isEmpty, city.count <= 15 else { throw QrGenerationError.invalidCity }

        if let amount = params.amount?.trimmingCharacters(in: .whitespaces), !amount.isEmpty {
            let pattern = "^\\d{1,10}(\\.\\d{1,2})?$"
            guard amount.range(of: pattern, options: .regularExpression) != nil else {
                throw QrGenerationError.invalidAmount
            }
        }
    }
}
