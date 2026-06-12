package dev.code93.emvqr.domain.generator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TlvEncoderTest {

    @Test
    fun `codifica tag longitud y valor`() {
        assertEquals("000201", TlvEncoder.tlv("00", "01"))
        assertEquals("5913Tienda Prueba", TlvEncoder.tlv("59", "Tienda Prueba"))
    }

    @Test
    fun `longitud con padding de dos digitos`() {
        assertEquals("5304ABCD", TlvEncoder.tlv("53", "ABCD"))
    }

    @Test
    fun `rechaza valores de mas de 99 caracteres`() {
        assertFailsWith<IllegalArgumentException> {
            TlvEncoder.tlv("26", "x".repeat(100))
        }
    }

    @Test
    fun `rechaza valores vacios`() {
        assertFailsWith<IllegalArgumentException> {
            TlvEncoder.tlv("59", "")
        }
    }
}
