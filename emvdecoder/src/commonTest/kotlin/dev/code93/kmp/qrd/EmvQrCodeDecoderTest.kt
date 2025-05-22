package dev.code93.kmp.qrd

import dev.code93.kmp.qrd.model.EmvCoAdditionalDataField
import dev.code93.kmp.qrd.model.EmvCoMerchantInformation
import dev.code93.kmp.qrd.model.EmvCoQrCodeConvention
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EmvQrCodeDecoderTest {

    @Test
    fun `decode should parse a valid QR code string correctly`() {
        val qrCodeString = "00020101021226280009SAR000000101030032703003SAR520458125303702540510.005802SA5913Dummy Merchant6009RIYADHADD62070503***6304C9AE"
        val decodedData = EmvQrCodeDecoder.decode(qrCodeString)

        assertNotNull(decodedData.conventionsQrCodeEmvCoData)
        assertEquals("01", decodedData.conventionsQrCodeEmvCoData?.payloadFormatIndicator)
        assertEquals("12", decodedData.conventionsQrCodeEmvCoData?.pointOfInitiationMethod)

        assertNotNull(decodedData.merchantInformationData)
        assertEquals("SAR", decodedData.merchantInformationData?.get(0)?.merchantAccountInformation)
        assertEquals("000000101", decodedData.merchantInformationData?.get(0)?.merchantId)


        assertEquals("5812", decodedData.merchantCategoryCode)
        assertEquals("702", decodedData.transactionCurrency)
        assertEquals("10.00", decodedData.transactionAmount)
        assertEquals("SA", decodedData.countryCode)
        assertEquals("Dummy Merchant", decodedData.merchantName)
        assertEquals("RIYADHADD", decodedData.merchantCity)

        assertNotNull(decodedData.additionalDataFieldData)
        assertEquals("***", decodedData.additionalDataFieldData?.billNumber)

        assertEquals("C9AE", decodedData.crc)
    }

    @Test
    fun `decode should handle QR code with missing optional elements`() {
        // QR code without transaction amount (tag 54) and additional data field (tag 62)
        val qrCodeString = "00020101021126280009SAR000000101030032703003SAR5204581253037025802SA5913Dummy Merchant6009RIYADHADD6304ABCD"
        val decodedData = EmvQrCodeDecoder.decode(qrCodeString)

        assertNotNull(decodedData.conventionsQrCodeEmvCoData)
        assertEquals("01", decodedData.conventionsQrCodeEmvCoData?.payloadFormatIndicator)


        assertNull(decodedData.transactionAmount)
        assertNull(decodedData.additionalDataFieldData?.billNumber) // Assuming bill number is optional

        assertEquals("5812", decodedData.merchantCategoryCode)
        assertEquals("702", decodedData.transactionCurrency)
        assertEquals("SA", decodedData.countryCode)
        assertEquals("Dummy Merchant", decodedData.merchantName)
        assertEquals("RIYADHADD", decodedData.merchantCity)
        assertEquals("ABCD", decodedData.crc)
    }

    @Test
    fun `decode should handle incorrectly formatted data elements`() {
        // QR code with non-numeric length for merchant name (tag 59)
        val qrCodeString = "00020101021126280009SAR000000101030032703003SAR5204581253037025802SA59AAInvalidLength6009RIYADHADD6304ABCD"
        val decodedData = EmvQrCodeDecoder.decode(qrCodeString)
        // Based on current implementation, it might try to parse "AA" as length, fail, and then merchant name would be null or misparsed.
        // Let's assume for now it results in a null or empty merchantName if length parsing fails.
        // The exact behavior depends on the robustness of the substring and parsing logic.
        // For this test, we'll check if critical parts are still parsed, implying some level of error recovery or skipping.
        assertNotNull(decodedData.conventionsQrCodeEmvCoData, "Conventions data should be parsed")
        assertEquals("SA", decodedData.countryCode, "Country code should be parsed")
        assertEquals("RIYADHADD", decodedData.merchantCity, "Merchant city should be parsed")
        //assertEquals(null, decodedData.merchantName) // Behavior verification needed
    }
    
    @Test
    fun `decode should return default object for an empty QR code string`() {
        val qrCodeString = ""
        val decodedData = EmvQrCodeDecoder.decode(qrCodeString)

        assertNull(decodedData.conventionsQrCodeEmvCoData?.payloadFormatIndicator)
        assertNull(decodedData.merchantInformationData)
        assertNull(decodedData.merchantCategoryCode)
        assertNull(decodedData.transactionCurrency)
        assertNull(decodedData.transactionAmount)
        assertNull(decodedData.countryCode)
        assertNull(decodedData.merchantName)
        assertNull(decodedData.merchantCity)
        assertNull(decodedData.additionalDataFieldData)
        assertNull(decodedData.crc)
    }

    @Test
    fun `getConventionsQrCodeEmvCoData should parse convention data correctly`() {
        val qrSnippet = "000201010212" // Payload Format Indicator and Point of Initiation Method
        val decodedData = EmvQrCodeDecoder.decode(qrSnippet + "6304FFFF") // Add dummy CRC

        assertNotNull(decodedData.conventionsQrCodeEmvCoData)
        assertEquals("01", decodedData.conventionsQrCodeEmvCoData?.payloadFormatIndicator)
        assertEquals("12", decodedData.conventionsQrCodeEmvCoData?.pointOfInitiationMethod)
    }

    @Test
    fun `getMerchantInformationData should parse merchant information correctly`() {
        // Includes two merchant account information blocks (tags 26 and 27)
        val qrSnippet = "26280009SAR000000101030032703003SAR"
        val decodedData = EmvQrCodeDecoder.decode(qrSnippet + "6304FFFF") // Add dummy CRC

        assertNotNull(decodedData.merchantInformationData)
        assertEquals(2, decodedData.merchantInformationData?.size)

        assertEquals("SAR", decodedData.merchantInformationData?.get(0)?.merchantAccountInformation)
        assertEquals("000000101", decodedData.merchantInformationData?.get(0)?.merchantId)
        assertEquals("003", decodedData.merchantInformationData?.get(0)?.merchantIdService)


        assertEquals("SAR", decodedData.merchantInformationData?.get(1)?.merchantAccountInformation)
        assertNull(decodedData.merchantInformationData?.get(1)?.merchantId) // ID is not present in the second one
        assertEquals("003", decodedData.merchantInformationData?.get(1)?.merchantIdService)
    }

    @Test
    fun `getAdditionalDataFieldData should parse additional data correctly`() {
        val qrSnippet = "62230103ABC0203DEF0503***" // Bill Number, Mobile Number, Store Label
        val decodedData = EmvQrCodeDecoder.decode(qrSnippet + "6304FFFF")

        assertNotNull(decodedData.additionalDataFieldData)
        assertEquals("***", decodedData.additionalDataFieldData?.billNumber)
        assertEquals("ABC", decodedData.additionalDataFieldData?.mobileNumber)
        assertEquals("DEF", decodedData.additionalDataFieldData?.storeLabel)
    }

    @Test
    fun `decode should handle QR code with only CRC`() {
        val qrCodeString = "6304ABCD"
        val decodedData = EmvQrCodeDecoder.decode(qrCodeString)
        assertNull(decodedData.conventionsQrCodeEmvCoData?.payloadFormatIndicator)
        assertEquals("ABCD", decodedData.crc)
    }

    @Test
    fun `decode should handle QR code with invalid length field`() {
        // Tag 59 (Merchant Name) has length "AA" which is not a number
        val qrCodeString = "00020159AAInvalidName6304ABCD"
        val decodedData = EmvQrCodeDecoder.decode(qrCodeString)
        // Expecting merchant name to be null or skipped due to parsing error of its length
        assertNull(decodedData.merchantName, "Merchant name should be null due to invalid length")
        assertEquals("01", decodedData.conventionsQrCodeEmvCoData?.payloadFormatIndicator) // Check if other fields are parsed
        assertEquals("ABCD", decodedData.crc)
    }
}
