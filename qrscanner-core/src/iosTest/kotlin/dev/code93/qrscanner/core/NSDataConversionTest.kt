package dev.code93.qrscanner.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create

class NSDataConversionTest {

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    @Test
    fun round_trip_nsdata_a_bytearray() {
        val original = byteArrayOf(0x00, 0x01, 0x7F, -0x80, 0x42)
        val nsData = original.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = original.size.toULong())
        }
        assertContentEquals(original, nsData.toByteArray())
    }

    @Test
    fun nsdata_vacio_da_bytearray_vacio() {
        assertEquals(0, NSData().toByteArray().size)
    }
}
