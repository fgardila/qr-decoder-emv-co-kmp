package dev.code93.kmp.qrd

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CRCValidatorTest {

    @Test
    fun validateKnownCrc16CcittVector() {
        // Vector estándar CRC-16/CCITT-FALSE: "123456789" -> 0x29B1
        assertTrue(EmvQr.isCrcValid("123456789" + "29B1"))
    }

    @Test
    fun validateAcceptsLowercaseCrc() {
        assertTrue(EmvQr.isCrcValid("123456789" + "29b1"))
    }

    @Test
    fun validateRejectsWrongCrc() {
        assertFalse(EmvQr.isCrcValid("123456789" + "FFFF"))
    }

    @Test
    fun validateGeneratedQrRoundTrip() {
        val qr = withCrc(tlv("00", "01") + tlv("59", "Tienda Prueba") + tlv("60", "Bogota"))
        assertTrue(EmvQr.isCrcValid(qr))
    }

    @Test
    fun validateRejectsTamperedPayload() {
        val qr = withCrc(tlv("00", "01") + tlv("59", "Tienda Prueba"))
        val tampered = qr.replaceFirst("Tienda", "TIENDA")
        assertFalse(EmvQr.isCrcValid(tampered))
    }

    @Test
    fun validatePayloadWithMultibyteCharacters() {
        // El CRC se calcula sobre los bytes UTF-8 del payload
        val qr = withCrc(tlv("00", "01") + tlv("59", "Café Bogotá"))
        assertTrue(EmvQr.isCrcValid(qr))
    }

    @Test
    fun validateRejectsEmptyString() {
        assertFalse(EmvQr.isCrcValid(""))
    }

    @Test
    fun validateRejectsStringShorterThanCrc() {
        assertFalse(EmvQr.isCrcValid("123"))
    }

    @Test
    fun validateRejectsCrcOnlyString() {
        // 4 chars: payload vacío + "FFFF" no coincide con CRC("") por el valor inicial
        assertFalse(EmvQr.isCrcValid("0000"))
    }
}
