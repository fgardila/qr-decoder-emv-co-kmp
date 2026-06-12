package dev.code93.kmp.qrd

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Sanity test del source set androidHostTest (unit tests JVM del target Android
 * bajo el plugin com.android.kotlin.multiplatform.library).
 */
class EmvQrCodeDecoderAndroidHostTest {

    @Test
    fun decodeBasicTagsOnAndroidHost() {
        val data = EmvQr.decode("0002015303170540510.005802CO")

        assertEquals("01", data.conventionsQrCodeEmvCoData?.indicatorEmv)
        assertEquals("170", data.transactionDetailData?.currencyCode)
        assertEquals("10.00", data.transactionDetailData?.transactionValue)
        assertEquals("CO", data.additionalMerchantInformationData?.countryCode)
    }

    @Test
    fun crcValidatorKnownVectorOnAndroidHost() {
        assertTrue(EmvQr.isCrcValid("123456789" + "29B1"))
    }
}
