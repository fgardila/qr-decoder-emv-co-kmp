package dev.code93.kmp.qrd.data

import dev.code93.kmp.qrd.SubFieldType

/**
 * Nombre de Archivo: ConventionsQrCodeEmvCoData.kt
 * Proyecto: Biblioteca de Decodificación de Código QR EMV
 * Descripción: Convenciones QR Code EMVCo
 * Autor: Fabian Guillermo Ardila Castro (fabian.ardila@code93.dev)
 * Creado: 14 noviembre de 2024
 * Última Actualización: 14 noviembre de 2024
 * Versión: 1.0.0
 * Licencia: Licencia MIT
 * GitHub: https://github.com/fabianardila/emv_qrcode/
 */
data class ConventionsQrCodeEmvCoData(
    val indicatorEmv: String,
    val qrType: String,
    val cyclicRedundancyCheck: String,
    val securityField: String,
)

enum class SecurityFieldType(override val subTag: String) : SubFieldType {
    NETWORK_ID("00"),
    HASH_VALUE("01")
}
