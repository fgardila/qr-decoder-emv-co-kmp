package dev.code93.emvqr.domain.generator

import dev.code93.emvqr.domain.generator.TlvEncoder.tlv
import dev.code93.emvqr.domain.model.QrGenerationParams

/**
 * Construye el rawText de un QR estático según el estándar EASPBV v1.4-2025.
 *
 * Orden de tags: 00, 01, 26 (llave), 49 (red), 50 (código comercio), 52, 53,
 * 54 (opcional), 58, 59, 60, 62 (terminal + propósito) y 63 (CRC).
 */
object EaspbvQrBuilder {

    private const val TERMINAL_LABEL = "TERMQR01"
    private const val PURPOSE_PURCHASE = "00"

    fun build(params: QrGenerationParams): String {
        validate(params)

        val payload = buildString {
            append(tlv("00", "01"))                       // Payload Format Indicator
            append(tlv("01", "11"))                       // QR estático (con monto = híbrido)

            if (params.keyType != null && !params.keyValue.isNullOrBlank()) {
                append(
                    tlv(
                        "26",
                        tlv("00", "${params.network.guidPrefix}.LLA") +
                            tlv(params.keyType.subTag, params.keyValue.trim())
                    )
                )
            }

            append(
                tlv(
                    "49",
                    tlv("00", "${params.network.guidPrefix}.RED") +
                        tlv("01", params.network.networkId)
                )
            )

            if (!params.merchantCode.isNullOrBlank()) {
                append(
                    tlv(
                        "50",
                        tlv("00", "${params.network.guidPrefix}.CU") +
                            tlv("01", params.merchantCode.trim())
                    )
                )
            }

            append(tlv("52", params.mcc))
            append(tlv("53", "170"))                      // COP, ISO 4217
            params.amount?.takeIf { it.isNotBlank() }?.let { append(tlv("54", it.trim())) }
            append(tlv("58", "CO"))
            append(tlv("59", params.merchantName.trim()))
            append(tlv("60", params.merchantCity.trim()))
            append(tlv("62", tlv("07", TERMINAL_LABEL) + tlv("08", PURPOSE_PURCHASE)))
        }

        val toChecksum = payload + "6304"
        return toChecksum + Crc16CcittFalse.checksum(toChecksum)
    }

    private fun validate(params: QrGenerationParams) {
        val hasKey = params.keyType != null && !params.keyValue.isNullOrBlank()
        val hasMerchantCode = !params.merchantCode.isNullOrBlank()
        require(hasKey || hasMerchantCode) { "Se requiere una llave o un código de comercio" }
        require(params.merchantName.isNotBlank()) { "El nombre del comercio es obligatorio" }
        require(params.merchantName.trim().length <= 25) { "Nombre máximo 25 caracteres" }
        require(params.merchantCity.isNotBlank()) { "La ciudad es obligatoria" }
        require(params.merchantCity.trim().length <= 15) { "Ciudad máximo 15 caracteres" }
        require(params.mcc.matches(Regex("\\d{4}"))) { "El MCC debe tener 4 dígitos" }
        params.amount?.takeIf { it.isNotBlank() }?.let {
            require(it.trim().matches(Regex("\\d{1,10}(\\.\\d{1,2})?"))) { "Monto inválido" }
        }
    }
}
