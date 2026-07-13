package dev.code93.qrscanner.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.ncgroup.kscan.Barcode
import org.ncgroup.kscan.BarcodeResult

class BarcodeResultMappingTest {

    @Test
    fun success_devuelve_el_raw_text() {
        val result = BarcodeResult.OnSuccess(
            Barcode(data = "00020101021126310014CO.COM.RBM.LLA", format = "QR_CODE", rawBytes = byteArrayOf())
        )
        assertEquals("00020101021126310014CO.COM.RBM.LLA", result.rawTextOrNull())
    }

    @Test
    fun success_con_data_vacia_devuelve_null() {
        val result = BarcodeResult.OnSuccess(
            Barcode(data = "", format = "QR_CODE", rawBytes = byteArrayOf())
        )
        assertNull(result.rawTextOrNull())
    }

    @Test
    fun failed_devuelve_null() {
        assertNull(BarcodeResult.OnFailed(Exception("sin QR")).rawTextOrNull())
    }

    @Test
    fun canceled_devuelve_null() {
        assertNull(BarcodeResult.OnCanceled.rawTextOrNull())
    }
}
