import Foundation

/// Codificador TLV del estándar EMVCo: tag de 2 caracteres + longitud de
/// 2 dígitos + valor. Los templates anidados usan el mismo formato.
enum TlvEncoder {
    static func tlv(_ tag: String, _ value: String) -> String {
        precondition(tag.count == 2, "El tag debe tener 2 caracteres: \(tag)")
        precondition((1...99).contains(value.count), "El valor del tag \(tag) debe tener entre 1 y 99 caracteres")
        return tag + String(format: "%02d", value.count) + value
    }
}
