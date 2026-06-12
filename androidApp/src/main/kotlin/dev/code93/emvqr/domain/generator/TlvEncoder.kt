package dev.code93.emvqr.domain.generator

/**
 * Codificador TLV del estándar EMVCo: tag de 2 caracteres + longitud de
 * 2 dígitos + valor. Los templates anidados usan el mismo formato.
 */
object TlvEncoder {
    fun tlv(tag: String, value: String): String {
        require(tag.length == 2) { "El tag debe tener 2 caracteres: $tag" }
        require(value.length in 1..99) { "El valor del tag $tag debe tener entre 1 y 99 caracteres" }
        return tag + value.length.toString().padStart(2, '0') + value
    }
}
