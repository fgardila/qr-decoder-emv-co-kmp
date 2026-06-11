package dev.code93.kmp.qrd

import dev.code93.kmp.qrd.data.DiscountApplicationType
import dev.code93.kmp.qrd.data.ImmediatePaymentKeyType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EmvQrCodeDecoderTest {

    /** QR colombiano completo: todos los tags y subcampos que el decoder soporta. */
    private fun buildFullQr(): String {
        val payload = buildString {
            append(tlv("00", "01"))                                  // Indicator EMV
            append(tlv("01", "12"))                                  // QR dinámico
            append(
                tlv(
                    "26",                                            // Llave de pagos inmediatos
                    tlv("00", "CO.COM.RBM") +
                        tlv("01", "01") +
                        tlv("02", "573001234567") +
                        tlv("03", "pagos@tienda.co") +
                        tlv("04", "ABC123") +
                        tlv("05", "M000123")
                )
            )
            append(tlv("49", tlv("00", "CO.COM.RBM") + tlv("01", "RBM")))        // Red adquirente
            append(tlv("50", tlv("00", "CO.COM.RBM") + tlv("01", "0010203040"))) // Código de comercio
            append(tlv("51", tlv("00", "CO.COM.RBM") + tlv("01", "AGG001")))     // Código agregador
            append(tlv("52", "5812"))                                // MCC
            append(tlv("53", "170"))                                 // Moneda COP
            append(tlv("54", "15000"))                               // Valor
            append(tlv("55", "01"))                                  // Indicador propina
            append(tlv("56", "1000"))                                // Valor propina
            append(tlv("57", "10"))                                  // Porcentaje propina
            append(tlv("58", "CO"))                                  // País
            append(tlv("59", "Tienda Prueba"))                       // Nombre comercio
            append(tlv("60", "Bogota"))                              // Ciudad
            append(tlv("61", "110111"))                              // Código postal
            append(
                tlv(
                    "62",                                            // Campos adicionales
                    tlv("01", "F-001") + tlv("02", "573009") +
                        tlv("03", "T1") + tlv("04", "LOY9") +
                        tlv("05", "REF7") + tlv("06", "CLI") +
                        tlv("07", "TERM01") + tlv("08", "Pago") +
                        tlv("09", "DATA") + tlv("10", "9001234") +
                        tlv("11", "APP")
                )
            )
            append(tlv("64", tlv("00", "es") + tlv("01", "Tienda Alt") + tlv("02", "Bogota Alt")))
            append(tlv("80", tlv("00", "CO.COM.RBM") + tlv("01", "APP")))      // Canal
            append(tlv("81", tlv("00", "CO.COM.RBM") + tlv("01", "01")))       // Condición IVA
            append(tlv("82", tlv("00", "CO.COM.RBM") + tlv("01", "1900")))     // Valor IVA
            append(tlv("83", tlv("00", "CO.COM.RBM") + tlv("01", "10000")))    // Base IVA
            append(tlv("84", tlv("00", "CO.COM.RBM") + tlv("01", "02")))       // Condición INC
            append(tlv("85", tlv("00", "CO.COM.RBM") + tlv("01", "800")))      // Valor INC
            append(tlv("86", tlv("00", "CO.COM.RBM") + tlv("01", "TX123456"))) // ID transacción
            append(tlv("91", tlv("00", "CO.COM.RBM") + tlv("01", "A1B2C3D4"))) // Campo de seguridad
            append(tlv("92", "01"))                                  // Código de servicio
            append(tlv("93", "REF-PAGO"))                            // Referencia de pago
            append(tlv("94", "02"))                                  // Tipo de producto
            append(tlv("95", "1234567890"))                          // Cuenta origen
            append(tlv("96", "0987654321"))                          // Cuenta destino
            append(tlv("97", "REF-DEST"))                            // Referencia cuenta destino
            append(tlv("98", "03"))                                  // Tipo producto transferencia
            append(
                tlv(
                    "99",                                            // Aplicación de descuento
                    tlv("01", "S") + tlv("02", "500") + tlv("03", "95") +
                        tlv("04", "5") + tlv("05", "500") + tlv("06", "N")
                )
            )
        }
        return withCrc(payload)
    }

    @Test
    fun decodeFullQrConventions() {
        val qr = buildFullQr()
        val data = EmvQrCodeDecoder(qr).decode()

        val conventions = assertNotNull(data.conventionsQrCodeEmvCoData)
        assertEquals("01", conventions.indicatorEmv)
        assertEquals("12", conventions.qrType)
        assertEquals(qr.takeLast(4), conventions.cyclicRedundancyCheck)
        assertEquals("A1B2C3D4", conventions.securityField)
    }

    @Test
    fun decodeFullQrMerchantInformation() {
        val data = EmvQrCodeDecoder(buildFullQr()).decode()

        val merchant = assertNotNull(data.merchantInformationData)
        val paymentKey = assertNotNull(merchant.immediatePaymentKey)
        assertEquals("CO.COM.RBM", paymentKey[ImmediatePaymentKeyType.NETWORK_ID])
        assertEquals("01", paymentKey[ImmediatePaymentKeyType.IDENTIFICATION_TYPE])
        assertEquals("573001234567", paymentKey[ImmediatePaymentKeyType.PHONE_NUMBER])
        assertEquals("pagos@tienda.co", paymentKey[ImmediatePaymentKeyType.EMAIL_ADDRESS])
        assertEquals("ABC123", paymentKey[ImmediatePaymentKeyType.ALPHANUMERIC_DATA])
        assertEquals("M000123", paymentKey[ImmediatePaymentKeyType.MERCHANT_ID])

        assertEquals("RBM", merchant.acquirerNetworkId)
        assertEquals("0010203040", merchant.merchantCode)
        assertEquals("AGG001", merchant.aggregatorMerchantCode)
    }

    @Test
    fun decodeFullQrAdditionalMerchantInformation() {
        val data = EmvQrCodeDecoder(buildFullQr()).decode()

        val additional = assertNotNull(data.additionalMerchantInformationData)
        assertEquals("5812", additional.merchantCategoryCode)
        assertEquals("CO", additional.countryCode)
        assertEquals("Tienda Prueba", additional.merchantName)
        assertEquals("Bogota", additional.merchantCity)
        assertEquals("110111", additional.postalCode)
        assertEquals("APP", additional.channel)
        assertEquals("01", additional.taxIvaCondition)
        assertEquals("1900", additional.taxIvaValue)
        assertEquals("10000", additional.taxIvaBase)
        assertEquals("02", additional.taxIncCondition)
        assertEquals("800", additional.taxIncValue)
        assertEquals("TX123456", additional.transactionId)
    }

    @Test
    fun decodeFullQrTransactionDetail() {
        val data = EmvQrCodeDecoder(buildFullQr()).decode()

        val transaction = assertNotNull(data.transactionDetailData)
        assertEquals("170", transaction.currencyCode)
        assertEquals("15000", transaction.transactionValue)
        assertEquals("01", transaction.tipIndicator)
        assertEquals("1000", transaction.tipValue)
        assertEquals("10", transaction.tipPercentage)
    }

    @Test
    fun decodeFullQrMerchantAdditionalFields() {
        val data = EmvQrCodeDecoder(buildFullQr()).decode()

        val fields = assertNotNull(data.merchantAdditionalFieldsData)
        assertEquals("F-001", fields.billingNumber)
        assertEquals("573009", fields.mobileNumber)
        assertEquals("T1", fields.storeLabel)
        assertEquals("LOY9", fields.loyaltyNumber)
        assertEquals("REF7", fields.referenceLabel)
        assertEquals("CLI", fields.customerLabel)
        assertEquals("TERM01", fields.terminalLabel)
        assertEquals("Pago", fields.transactionPurpose)
        assertEquals("DATA", fields.additionalConsumerData)
        assertEquals("9001234", fields.merchantTaxId)
        assertEquals("APP", fields.originChannel)
    }

    @Test
    fun decodeFullQrMerchantInformationLanguage() {
        val data = EmvQrCodeDecoder(buildFullQr()).decode()

        val language = assertNotNull(data.merchantInformationLanguageData)
        assertEquals("es", language.languagePreference)
        assertEquals("Tienda Alt", language.alternateMerchantName)
        assertEquals("Bogota Alt", language.alternateMerchantCity)
    }

    @Test
    fun decodeFullQrOtherTransactionsFields() {
        val data = EmvQrCodeDecoder(buildFullQr()).decode()

        val other = assertNotNull(data.otherTransactionsFieldsData)
        assertEquals("01", other.serviceCode)
        assertEquals("REF-PAGO", other.paymentReference)
        assertEquals("02", other.productType)
        assertEquals("1234567890", other.sourceAccount)
        assertEquals("0987654321", other.destinationAccount)
        assertEquals("REF-DEST", other.destinationAccountReference)
        assertEquals("03", other.transferProductType)

        val discount = assertNotNull(other.discountApplication)
        assertEquals("S", discount[DiscountApplicationType.DISCOUNT_INDICATOR])
        assertEquals("500", discount[DiscountApplicationType.DISCOUNT_AMOUNT])
        assertEquals("95", discount[DiscountApplicationType.IVA_DISCOUNT_AMOUNT])
        assertEquals("5", discount[DiscountApplicationType.DISCOUNT_PERCENTAGE])
        assertEquals("500", discount[DiscountApplicationType.DISCOUNT_VALUE])
        assertEquals("N", discount[DiscountApplicationType.DISCOUNT_QUERY])
    }

    @Test
    fun decodeFullQrPassesCrcValidation() {
        assertTrue(CRCValidator.validate(buildFullQr()))
    }

    @Test
    fun decodeMinimalQrUsesDefaultsForMissingTags() {
        val qr = withCrc(tlv("00", "01") + tlv("53", "170") + tlv("54", "5000"))
        val data = EmvQrCodeDecoder(qr).decode()

        assertEquals("01", data.conventionsQrCodeEmvCoData?.indicatorEmv)
        assertEquals("", data.conventionsQrCodeEmvCoData?.qrType)
        assertEquals("", data.conventionsQrCodeEmvCoData?.securityField)

        assertNull(data.merchantInformationData?.acquirerNetworkId)
        assertEquals(emptyMap(), data.merchantInformationData?.immediatePaymentKey)

        assertEquals("", data.additionalMerchantInformationData?.merchantCategoryCode)
        assertEquals("", data.additionalMerchantInformationData?.merchantName)
        assertNull(data.additionalMerchantInformationData?.postalCode)
        assertNull(data.additionalMerchantInformationData?.channel)

        // Contrato actual: estos dos campos caen a cadena vacía, el resto a null
        assertEquals("", data.merchantAdditionalFieldsData?.terminalLabel)
        assertEquals("", data.merchantAdditionalFieldsData?.transactionPurpose)
        assertNull(data.merchantAdditionalFieldsData?.billingNumber)

        assertNull(data.otherTransactionsFieldsData?.serviceCode)
        assertNull(data.merchantInformationLanguageData?.languagePreference)

        assertEquals("170", data.transactionDetailData?.currencyCode)
        assertEquals("5000", data.transactionDetailData?.transactionValue)
        assertNull(data.transactionDetailData?.tipIndicator)
    }

    @Test
    fun decodeEmptyStringReturnsDefaults() {
        val data = EmvQrCodeDecoder("").decode()

        assertEquals("", data.conventionsQrCodeEmvCoData?.indicatorEmv)
        assertEquals("", data.transactionDetailData?.currencyCode)
        assertEquals(emptyMap(), data.merchantInformationData?.immediatePaymentKey)
        assertNull(data.otherTransactionsFieldsData?.discountApplication?.values?.firstOrNull { it != null })
    }

    @Test
    fun decodeStopsAtTruncatedTag() {
        // El tag "5" quedó cortado: lo anterior debe conservarse
        val data = EmvQrCodeDecoder(tlv("00", "01") + "5").decode()
        assertEquals("01", data.conventionsQrCodeEmvCoData?.indicatorEmv)
        assertEquals("", data.transactionDetailData?.currencyCode)
    }

    @Test
    fun decodeStopsAtTruncatedLength() {
        val data = EmvQrCodeDecoder(tlv("00", "01") + "530").decode()
        assertEquals("01", data.conventionsQrCodeEmvCoData?.indicatorEmv)
        assertEquals("", data.transactionDetailData?.currencyCode)
    }

    @Test
    fun decodeStopsAtNonNumericLength() {
        val data = EmvQrCodeDecoder(tlv("00", "01") + "53XX170").decode()
        assertEquals("01", data.conventionsQrCodeEmvCoData?.indicatorEmv)
        assertEquals("", data.transactionDetailData?.currencyCode)
    }

    @Test
    fun decodeStopsWhenDeclaredLengthExceedsRemainder() {
        // El tag 59 declara 13 caracteres pero solo quedan 3
        val data = EmvQrCodeDecoder(tlv("00", "01") + "5913ABC").decode()
        assertEquals("01", data.conventionsQrCodeEmvCoData?.indicatorEmv)
        assertEquals("", data.additionalMerchantInformationData?.merchantName)
    }

    @Test
    fun decodeValueWithExactRemainingLengthIsAccepted() {
        val data = EmvQrCodeDecoder(tlv("00", "01") + "5803COL").decode()
        assertEquals("COL", data.additionalMerchantInformationData?.countryCode)
    }

    @Test
    fun decodeMalformedSubFieldsReturnsNullValues() {
        // Subcampo con longitud no numérica dentro del tag 26
        val qr = withCrc(tlv("00", "01") + tlv("26", "00XXCO.COM.RBM"))
        val data = EmvQrCodeDecoder(qr).decode()

        val paymentKey = assertNotNull(data.merchantInformationData?.immediatePaymentKey)
        assertTrue(paymentKey.values.all { it == null })
    }

    @Test
    fun decodeSubFieldNotInFirstPositionIsFound() {
        // El subcampo 05 (merchant id) va después de otros subcampos
        val qr = withCrc(tlv("00", "01") + tlv("26", tlv("00", "CO.COM.RBM") + tlv("05", "M999")))
        val data = EmvQrCodeDecoder(qr).decode()

        val paymentKey = assertNotNull(data.merchantInformationData?.immediatePaymentKey)
        assertEquals("M999", paymentKey[ImmediatePaymentKeyType.MERCHANT_ID])
        assertNull(paymentKey[ImmediatePaymentKeyType.PHONE_NUMBER])
    }

    @Test
    fun decodeRepeatedTagKeepsLastValue() {
        val qr = withCrc(tlv("00", "01") + tlv("59", "Primero") + tlv("59", "Segundo"))
        val data = EmvQrCodeDecoder(qr).decode()
        assertEquals("Segundo", data.additionalMerchantInformationData?.merchantName)
    }

    @Test
    fun decodeValueWithMultibyteCharacters() {
        val qr = withCrc(tlv("00", "01") + tlv("59", "Café Bogotá"))
        val data = EmvQrCodeDecoder(qr).decode()
        // La longitud TLV cuenta caracteres, no bytes
        assertEquals("Café Bogotá", data.additionalMerchantInformationData?.merchantName)
    }
}
