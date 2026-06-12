import Foundation

/// CRC-16/CCITT-FALSE (polinomio 0x1021, valor inicial 0xFFFF) sobre los bytes
/// UTF-8, como exige el tag 63 del estándar EMVCo (ISO/IEC 13239).
enum Crc16CcittFalse {
    static func checksum(_ data: String) -> String {
        var crc: UInt32 = 0xFFFF
        for byte in Array(data.utf8) {
            let b = UInt32(byte)
            for i in 0..<8 {
                let bit = ((b >> (7 - UInt32(i))) & 1) == 1
                let c15 = ((crc >> 15) & 1) == 1
                crc = (crc << 1) & 0xFFFFFFFF
                if c15 != bit {
                    crc ^= 0x1021
                }
            }
        }
        return String(format: "%04X", crc & 0xFFFF)
    }
}
