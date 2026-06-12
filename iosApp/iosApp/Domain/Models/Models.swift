import Foundation

// MARK: - Resultado de decodificación

/// Resultado de decodificar un QR: secciones legibles + integridad + diagnósticos.
struct DecodedQr: Equatable {
    let rawText: String
    let crcValid: Bool
    let sections: [EmvSection]
    let diagnostics: DecodeDiagnostics
}

/// Sección del estándar EASPBV con sus campos no vacíos.
struct EmvSection: Equatable, Identifiable {
    let id = UUID()
    let title: String
    let fields: [EmvField]
}

struct EmvField: Equatable, Identifiable {
    let id = UUID()
    let label: String
    let value: String
}

/// Observabilidad del parsing laxo de la librería.
struct DecodeDiagnostics: Equatable {
    let parsedTagCount: Int
    let consumedChars: Int
    let totalChars: Int
    let isFullyParsed: Bool
}

// MARK: - Generación

/// Redes adquirentes del anexo 1.4 del estándar EASPBV.
enum PaymentNetwork: String, CaseIterable, Identifiable {
    case redeban
    case credibanco

    var id: String { rawValue }

    var networkId: String {
        switch self {
        case .redeban: return "RBM"
        case .credibanco: return "CRB"
        }
    }

    var guidPrefix: String {
        switch self {
        case .redeban: return "CO.COM.RBM"
        case .credibanco: return "CO.COM.CRB"
        }
    }

    var displayName: String {
        switch self {
        case .redeban: return "Redeban"
        case .credibanco: return "Credibanco"
        }
    }
}

/// Tipos de llave de pagos inmediatos (sub-tags del template 26).
enum PaymentKeyType: String, CaseIterable, Identifiable {
    case alphanumeric
    case phone

    var id: String { rawValue }

    var subTag: String {
        switch self {
        case .alphanumeric: return "04"
        case .phone: return "02"
        }
    }

    var displayName: String {
        switch self {
        case .alphanumeric: return "Alfanumérica"
        case .phone: return "Número celular"
        }
    }
}

/// Parámetros mínimos para generar un QR estático de prueba EASPBV v1.4.
struct QrGenerationParams {
    let network: PaymentNetwork
    let keyType: PaymentKeyType?
    let keyValue: String?
    let merchantCode: String?
    let merchantName: String
    let merchantCity: String
    var mcc: String = "0000"
    var amount: String?
}
