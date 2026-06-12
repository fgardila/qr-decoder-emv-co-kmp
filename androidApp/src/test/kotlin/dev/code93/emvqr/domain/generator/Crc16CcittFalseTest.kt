package dev.code93.emvqr.domain.generator

import kotlin.test.Test
import kotlin.test.assertEquals

class Crc16CcittFalseTest {

    @Test
    fun `vector estandar CRC-16 CCITT-FALSE`() {
        assertEquals("29B1", Crc16CcittFalse.checksum("123456789"))
    }

    @Test
    fun `cadena vacia devuelve valor inicial`() {
        assertEquals("FFFF", Crc16CcittFalse.checksum(""))
    }

    @Test
    fun `resultado siempre tiene 4 caracteres hex mayusculas`() {
        val crc = Crc16CcittFalse.checksum("00020101021163")
        assertEquals(4, crc.length)
        assertEquals(crc.uppercase(), crc)
    }
}
