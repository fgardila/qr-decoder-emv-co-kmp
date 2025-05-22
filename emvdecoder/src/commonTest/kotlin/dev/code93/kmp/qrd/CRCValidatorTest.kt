package dev.code93.kmp.qrd

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CRCValidatorTest {

    @Test
    fun `validate should return true for a valid QR code string and CRC`() {
        val validQRCode = "00020101021226280009SAR000000101030032703003SAR520458125303123540510.005802SA5913Dummy Merchant6009RIYADHADD62070503***6304C9AE"
        assertTrue(CRCValidator.validate(validQRCode), "Validation failed for a valid QR code")
    }

    @Test
    fun `validate should return false for a QR code string with an invalid CRC`() {
        val invalidQRCode = "00020101021226280009SAR000000101030032703003SAR520458125303123540510.005802SA5913Dummy Merchant6009RIYADHADD62070503***6304FFFF"
        assertFalse(CRCValidator.validate(invalidQRCode), "Validation passed for an invalid QR code")
    }

    @Test
    fun `validate should return false for a QR code string that is too short`() {
        val shortQRCode = "123"
        assertFalse(CRCValidator.validate(shortQRCode), "Validation passed for a short QR code")
    }

    @Test
    fun `validate should return false for an empty QR code string`() {
        val emptyQRCode = ""
        assertFalse(CRCValidator.validate(emptyQRCode), "Validation passed for an empty QR code")
    }
}
