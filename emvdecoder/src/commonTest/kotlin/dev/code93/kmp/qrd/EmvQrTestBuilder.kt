package dev.code93.kmp.qrd

/**
 * Utilidades para construir payloads EMVCo TLV (tag + longitud de 2 dígitos + valor)
 * en los tests, con CRC-16 CCITT-FALSE calculado para cerrar el QR (tag 63).
 */
internal fun tlv(tag: String, value: String): String {
    require(value.length <= 99) { "TLV value too long for tag $tag" }
    return tag + value.length.toString().padStart(2, '0') + value
}

internal fun crc16Ccitt(data: String): String {
    var crc = 0xFFFF
    val polynomial = 0x1021
    data.encodeToByteArray().forEach { byte ->
        val b = byte.toInt()
        for (i in 0 until 8) {
            val bit = ((b shr (7 - i)) and 1) == 1
            val c15 = ((crc shr 15) and 1) == 1
            crc = crc shl 1
            if (c15 xor bit) crc = crc xor polynomial
        }
    }
    return (crc and 0xFFFF).toString(16).uppercase().padStart(4, '0')
}

/** Cierra el payload con el tag 63 (CRC) calculado sobre payload + "6304". */
internal fun withCrc(payload: String): String {
    val data = payload + "6304"
    return data + crc16Ccitt(data)
}
