package dev.code93.emvqr.domain.generator

/**
 * CRC-16/CCITT-FALSE (polinomio 0x1021, valor inicial 0xFFFF) sobre los bytes
 * UTF-8, como exige el tag 63 del estándar EMVCo (ISO/IEC 13239).
 */
object Crc16CcittFalse {
    private const val POLYNOMIAL = 0x1021
    private const val INITIAL = 0xFFFF

    fun checksum(data: String): String {
        var crc = INITIAL
        data.encodeToByteArray().forEach { byte ->
            val b = byte.toInt()
            for (i in 0 until 8) {
                val bit = ((b shr (7 - i)) and 1) == 1
                val c15 = ((crc shr 15) and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) crc = crc xor POLYNOMIAL
            }
        }
        return (crc and 0xFFFF).toString(16).uppercase().padStart(4, '0')
    }
}
