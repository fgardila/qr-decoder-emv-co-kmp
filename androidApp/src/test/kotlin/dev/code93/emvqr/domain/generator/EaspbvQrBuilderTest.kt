package dev.code93.emvqr.domain.generator

import dev.code93.emvqr.domain.model.PaymentKeyType
import dev.code93.emvqr.domain.model.PaymentNetwork
import dev.code93.emvqr.domain.model.QrGenerationParams
import dev.code93.kmp.qrd.EmvQr
import dev.code93.kmp.qrd.data.ImmediatePaymentKeyType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Round-trip: lo que genera el builder debe ser decodificable por la librería
 * real (dev.code93:emvdecoder) con CRC válido y parse completo.
 */
class EaspbvQrBuilderTest {

    private val baseParams = QrGenerationParams(
        network = PaymentNetwork.REDEBAN,
        keyType = PaymentKeyType.ALPHANUMERIC,
        keyValue = "@miLlave123",
        merchantCode = null,
        merchantName = "Tienda Prueba",
        merchantCity = "Bogota",
        amount = "15000.00"
    )

    @Test
    fun `QR generado pasa validacion CRC de la libreria`() {
        assertTrue(EmvQr.isCrcValid(EaspbvQrBuilder.build(baseParams)))
    }

    @Test
    fun `QR generado se parsea completo`() {
        val result = EmvQr.decodeWithDiagnostics(EaspbvQrBuilder.build(baseParams))
        assertTrue(result.diagnostics.isFullyParsed)
    }

    @Test
    fun `round-trip de campos principales`() {
        val data = EmvQr.decode(EaspbvQrBuilder.build(baseParams))

        assertEquals("01", data.conventionsQrCodeEmvCoData?.indicatorEmv)
        assertEquals("11", data.conventionsQrCodeEmvCoData?.qrType)
        assertEquals(
            "@miLlave123",
            data.merchantInformationData?.immediatePaymentKey
                ?.get(ImmediatePaymentKeyType.ALPHANUMERIC_DATA)
        )
        assertEquals("RBM", data.merchantInformationData?.acquirerNetworkId)
        assertEquals("170", data.transactionDetailData?.currencyCode)
        assertEquals("15000.00", data.transactionDetailData?.transactionValue)
        assertEquals("CO", data.additionalMerchantInformationData?.countryCode)
        assertEquals("Tienda Prueba", data.additionalMerchantInformationData?.merchantName)
        assertEquals("Bogota", data.additionalMerchantInformationData?.merchantCity)
        assertEquals("TERMQR01", data.merchantAdditionalFieldsData?.terminalLabel)
        assertEquals("00", data.merchantAdditionalFieldsData?.transactionPurpose)
    }

    @Test
    fun `con codigo de comercio en vez de llave`() {
        val params = baseParams.copy(
            keyType = null,
            keyValue = null,
            merchantCode = "0012345678",
            amount = null
        )
        val data = EmvQr.decode(EaspbvQrBuilder.build(params))

        assertEquals("0012345678", data.merchantInformationData?.merchantCode)
        assertNull(data.transactionDetailData?.transactionValue?.takeIf { it.isNotEmpty() })
        assertTrue(EmvQr.isCrcValid(EaspbvQrBuilder.build(params)))
    }

    @Test
    fun `red Credibanco usa GUIDs CRB`() {
        val data = EmvQr.decode(
            EaspbvQrBuilder.build(baseParams.copy(network = PaymentNetwork.CREDIBANCO))
        )
        assertEquals("CRB", data.merchantInformationData?.acquirerNetworkId)
        assertEquals(
            "CO.COM.CRB.LLA",
            data.merchantInformationData?.immediatePaymentKey
                ?.get(ImmediatePaymentKeyType.NETWORK_ID)
        )
    }

    @Test
    fun `rechaza nombre de mas de 25 caracteres`() {
        assertFailsWith<IllegalArgumentException> {
            EaspbvQrBuilder.build(baseParams.copy(merchantName = "x".repeat(26)))
        }
    }

    @Test
    fun `rechaza sin llave ni codigo de comercio`() {
        assertFailsWith<IllegalArgumentException> {
            EaspbvQrBuilder.build(
                baseParams.copy(keyType = null, keyValue = null, merchantCode = null)
            )
        }
    }

    @Test
    fun `rechaza monto invalido`() {
        assertFailsWith<IllegalArgumentException> {
            EaspbvQrBuilder.build(baseParams.copy(amount = "12,50"))
        }
    }
}
