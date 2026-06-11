package dev.code93.kmp.qrd

import dev.code93.kmp.qrd.data.ImmediatePaymentKeyType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Casos de prueba con rawText reales de QR Redeban (red CO.COM.RBM):
 * QR estáticos (tipo 11) de Llaves/pagos inmediatos con CRC válido.
 *
 * Nota sobre el contrato actual: estos QR transportan el ID de transacción
 * en el tag 90 (CO.COM.RBM.TRXID), que el decoder NO mapea (lee el tag 86).
 * Por eso transactionId se espera null; si el decoder llegara a soportar el
 * tag 90, estos tests deben actualizarse.
 */
class RedebanQrSamplesTest {

    private val qrLlaveOcfrf =
        "00020101021126310014CO.COM.RBM.LLA0409@ocfrf11549250014CO.COM.RBM.RED0103RBM" +
            "50290013CO.COM.RBM.CU01080000000051220013CO.COM.RBM.CA0101052040000530317" +
            "0540815000.005802CO59010600106101062290712CC113062211508020011036318027001" +
            "6CO.COM.RBM.CANAL0103APP81250015CO.COM.RBM.CIVA01020282260014CO.COM.RBM.IV" +
            "A01040.0083270015CO.COM.RBM.BASE01040.0084250015CO.COM.RBM.CINC01020285260" +
            "014CO.COM.RBM.INC01040.0090430016CO.COM.RBM.TRXID0119177742sRKijVmYXHacX91" +
            "460014CO.COM.RBM.SEC0124zsyEie5vv8XGpY7pIwifsWtR63048215"

    private val qrMiLlave =
        "00020101021126400014CO.COM.RBM.LLA0418@millave109874199249250014CO.COM.RBM.RED" +
            "0103RBM50290013CO.COM.RBM.CU01080000000051220013CO.COM.RBM.CA0101052040000" +
            "530317054101000000.005802CO59010600106101062290712CC109874199208020011036" +
            "3180270016CO.COM.RBM.CANAL0103APP81250015CO.COM.RBM.CIVA01020282260014CO." +
            "COM.RBM.IVA01040.0083270015CO.COM.RBM.BASE01040.0084250015CO.COM.RBM.CINC" +
            "01020285260014CO.COM.RBM.INC01040.0090430016CO.COM.RBM.TRXID0119177756H6o" +
            "FRyaUweotz91460014CO.COM.RBM.SEC0124H3ZJqDHKp0n7nFn/EDi45YGS6304F906"

    // ---------- CRC ----------

    @Test
    fun redebanQrsPassCrcValidation() {
        assertTrue(CRCValidator.validate(qrLlaveOcfrf))
        assertTrue(CRCValidator.validate(qrMiLlave))
    }

    @Test
    fun redebanQrRejectsTamperedAmount() {
        val tampered = qrLlaveOcfrf.replaceFirst("15000.00", "99000.00")
        assertTrue(!CRCValidator.validate(tampered))
    }

    // ---------- QR 1: llave @ocfrf115 ----------

    @Test
    fun qrLlaveOcfrfConventions() {
        val data = EmvQrCodeDecoder(qrLlaveOcfrf).decode()

        val conventions = assertNotNull(data.conventionsQrCodeEmvCoData)
        assertEquals("01", conventions.indicatorEmv)
        assertEquals("11", conventions.qrType) // QR estático
        assertEquals("8215", conventions.cyclicRedundancyCheck)
        assertEquals("zsyEie5vv8XGpY7pIwifsWtR", conventions.securityField)
    }

    @Test
    fun qrLlaveOcfrfMerchantInformation() {
        val data = EmvQrCodeDecoder(qrLlaveOcfrf).decode()

        val merchant = assertNotNull(data.merchantInformationData)
        val paymentKey = assertNotNull(merchant.immediatePaymentKey)
        assertEquals("CO.COM.RBM.LLA", paymentKey[ImmediatePaymentKeyType.NETWORK_ID])
        assertEquals("@ocfrf115", paymentKey[ImmediatePaymentKeyType.ALPHANUMERIC_DATA])
        assertNull(paymentKey[ImmediatePaymentKeyType.IDENTIFICATION_TYPE])
        assertNull(paymentKey[ImmediatePaymentKeyType.PHONE_NUMBER])
        assertNull(paymentKey[ImmediatePaymentKeyType.EMAIL_ADDRESS])
        assertNull(paymentKey[ImmediatePaymentKeyType.MERCHANT_ID])

        assertEquals("RBM", merchant.acquirerNetworkId)
        assertEquals("00000000", merchant.merchantCode)
        assertEquals("0", merchant.aggregatorMerchantCode)
    }

    @Test
    fun qrLlaveOcfrfAdditionalMerchantInformation() {
        val data = EmvQrCodeDecoder(qrLlaveOcfrf).decode()

        val additional = assertNotNull(data.additionalMerchantInformationData)
        assertEquals("0000", additional.merchantCategoryCode)
        assertEquals("CO", additional.countryCode)
        assertEquals("0", additional.merchantName)
        assertEquals("0", additional.merchantCity)
        assertEquals("0", additional.postalCode)
        assertEquals("APP", additional.channel)
        assertEquals("02", additional.taxIvaCondition)
        assertEquals("0.00", additional.taxIvaValue)
        assertEquals("0.00", additional.taxIvaBase)
        assertEquals("02", additional.taxIncCondition)
        assertEquals("0.00", additional.taxIncValue)
        // El ID viaja en el tag 90 (no mapeado), no en el 86
        assertNull(additional.transactionId)
    }

    @Test
    fun qrLlaveOcfrfTransactionDetail() {
        val data = EmvQrCodeDecoder(qrLlaveOcfrf).decode()

        val transaction = assertNotNull(data.transactionDetailData)
        assertEquals("170", transaction.currencyCode) // COP
        assertEquals("15000.00", transaction.transactionValue)
        assertNull(transaction.tipIndicator)
        assertNull(transaction.tipValue)
        assertNull(transaction.tipPercentage)
    }

    @Test
    fun qrLlaveOcfrfMerchantAdditionalFields() {
        val data = EmvQrCodeDecoder(qrLlaveOcfrf).decode()

        val fields = assertNotNull(data.merchantAdditionalFieldsData)
        assertEquals("CC1130622115", fields.terminalLabel)
        assertEquals("00", fields.transactionPurpose)
        assertEquals("631", fields.originChannel)
        assertNull(fields.billingNumber)
        assertNull(fields.mobileNumber)
        assertNull(fields.storeLabel)
        assertNull(fields.loyaltyNumber)
        assertNull(fields.referenceLabel)
        assertNull(fields.customerLabel)
        assertNull(fields.additionalConsumerData)
        assertNull(fields.merchantTaxId)
    }

    @Test
    fun qrLlaveOcfrfHasNoTransferOrLanguageData() {
        val data = EmvQrCodeDecoder(qrLlaveOcfrf).decode()

        val other = assertNotNull(data.otherTransactionsFieldsData)
        assertNull(other.serviceCode)
        assertNull(other.paymentReference)
        assertNull(other.sourceAccount)
        assertNull(other.destinationAccount)

        val language = assertNotNull(data.merchantInformationLanguageData)
        assertNull(language.languagePreference)
        assertNull(language.alternateMerchantName)
        assertNull(language.alternateMerchantCity)
    }

    // ---------- QR 2: llave @millave1098741992 ----------

    @Test
    fun qrMiLlaveConventions() {
        val data = EmvQrCodeDecoder(qrMiLlave).decode()

        val conventions = assertNotNull(data.conventionsQrCodeEmvCoData)
        assertEquals("01", conventions.indicatorEmv)
        assertEquals("11", conventions.qrType)
        assertEquals("F906", conventions.cyclicRedundancyCheck)
        assertEquals("H3ZJqDHKp0n7nFn/EDi45YGS", conventions.securityField)
    }

    @Test
    fun qrMiLlaveMerchantInformation() {
        val data = EmvQrCodeDecoder(qrMiLlave).decode()

        val merchant = assertNotNull(data.merchantInformationData)
        val paymentKey = assertNotNull(merchant.immediatePaymentKey)
        assertEquals("CO.COM.RBM.LLA", paymentKey[ImmediatePaymentKeyType.NETWORK_ID])
        assertEquals("@millave1098741992", paymentKey[ImmediatePaymentKeyType.ALPHANUMERIC_DATA])

        assertEquals("RBM", merchant.acquirerNetworkId)
        assertEquals("00000000", merchant.merchantCode)
        assertEquals("0", merchant.aggregatorMerchantCode)
    }

    @Test
    fun qrMiLlaveTransactionDetailWithLargeAmount() {
        val data = EmvQrCodeDecoder(qrMiLlave).decode()

        val transaction = assertNotNull(data.transactionDetailData)
        assertEquals("170", transaction.currencyCode)
        assertEquals("1000000.00", transaction.transactionValue)
    }

    @Test
    fun qrMiLlaveTaxesAndChannel() {
        val data = EmvQrCodeDecoder(qrMiLlave).decode()

        val additional = assertNotNull(data.additionalMerchantInformationData)
        assertEquals("APP", additional.channel)
        assertEquals("02", additional.taxIvaCondition)
        assertEquals("0.00", additional.taxIvaValue)
        assertEquals("0.00", additional.taxIvaBase)
        assertEquals("02", additional.taxIncCondition)
        assertEquals("0.00", additional.taxIncValue)
        assertNull(additional.transactionId) // tag 90 no mapeado
    }

    @Test
    fun qrMiLlaveMerchantAdditionalFields() {
        val data = EmvQrCodeDecoder(qrMiLlave).decode()

        val fields = assertNotNull(data.merchantAdditionalFieldsData)
        assertEquals("CC1098741992", fields.terminalLabel)
        assertEquals("00", fields.transactionPurpose)
        assertEquals("631", fields.originChannel)
    }
}
