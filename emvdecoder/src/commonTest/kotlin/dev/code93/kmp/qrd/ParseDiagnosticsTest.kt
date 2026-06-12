package dev.code93.kmp.qrd

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParseDiagnosticsTest {

    @Test
    fun fullyParsedQrReportsCompleteConsumption() {
        val qr = withCrc(tlv("00", "01") + tlv("53", "170") + tlv("54", "5000"))
        val result = EmvQr.decodeWithDiagnostics(qr)

        assertTrue(result.diagnostics.isFullyParsed)
        assertEquals(qr.length, result.diagnostics.consumedChars)
        assertEquals(qr.length, result.diagnostics.totalChars)
        // 00, 53, 54 y el 63 (CRC) que agrega withCrc
        assertEquals(4, result.diagnostics.parsedTagCount)
        assertEquals("170", result.data.transactionDetailData?.currencyCode)
    }

    @Test
    fun truncatedQrReportsPartialConsumption() {
        val valid = tlv("00", "01")
        val qr = valid + "53XX170" // longitud no numérica: el parser se detiene aquí
        val result = EmvQr.decodeWithDiagnostics(qr)

        assertFalse(result.diagnostics.isFullyParsed)
        assertEquals(valid.length, result.diagnostics.consumedChars)
        assertEquals(qr.length, result.diagnostics.totalChars)
        assertEquals(1, result.diagnostics.parsedTagCount)
        // Lo bien formado igual se decodifica
        assertEquals("01", result.data.conventionsQrCodeEmvCoData?.indicatorEmv)
    }

    @Test
    fun emptyInputIsVacuouslyFullyParsed() {
        val result = EmvQr.decodeWithDiagnostics("")

        assertTrue(result.diagnostics.isFullyParsed)
        assertEquals(0, result.diagnostics.parsedTagCount)
        assertEquals(0, result.diagnostics.totalChars)
    }

    @Test
    fun decodeAndDecodeWithDiagnosticsReturnSameData() {
        val qr = withCrc(tlv("00", "01") + tlv("59", "Tienda"))
        assertEquals(EmvQr.decode(qr), EmvQr.decodeWithDiagnostics(qr).data)
    }
}
