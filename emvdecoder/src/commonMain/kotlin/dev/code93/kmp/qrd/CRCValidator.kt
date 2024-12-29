package dev.code93.kmp.qrd

/**
 * Nombre de Archivo: CRCValidator.kt
 * Proyecto: Biblioteca de Decodificación de Código QR EMV
 * Descripción: Esta clase proporciona funcionalidad para validar el CRC (Verificación de Redundancia Cíclica)
 * de una cadena de código QR EMV. Garantiza la integridad y autenticidad de los datos del código QR
 * comparando el valor de CRC calculado con el presente en el código QR.
 * Autor: Mohamed Ayed (engmohammedayed@gmail.com)
 * Creado: 8 de abril de 2024
 * Última Actualización: 11 de abril de 2024
 * Versión: 1.0.0
 * Licencia: Licencia MIT
 * GitHub: https://github.com/mohamedayed/emv_qrcode/
 *
 * Notas:
 * La validación de CRC es una parte esencial del procesamiento de códigos QR EMV, asegurando que los datos
 * no han sido alterados o corrompidos. Esta clase implementa el algoritmo estándar CRC-16 CCITT
 * para calcular y validar el valor de CRC del contenido del código QR. Está diseñada para ser utilizada
 * junto con el EmvQrCodeDecoder para verificar la integridad de los datos antes de la decodificación.
 */
class CRCValidator {
    companion object {
        private const val CRC_LENGTH = 4

        fun validate(qrCode: String): Boolean {
            val crcValueFromCode = qrCode.takeLast(CRC_LENGTH)
            val dataToValidate = qrCode.dropLast(CRC_LENGTH)

            return calculateCrc(dataToValidate).equals(crcValueFromCode, ignoreCase = true)
        }

        private fun calculateCrc(data: String): String {
            var crc = 0xFFFF
            val polynomial = 0x1021

            val byteArray = data.encodeToByteArray()

            byteArray.forEach { byte ->
                var b = byte.toInt()
                for (i in 0 until 8) {
                    val bit = ((b shr (7 - i) and 1) == 1)
                    val c15 = ((crc shr 15 and 1) == 1)
                    crc = crc shl 1
                    if (c15 xor bit) crc = crc xor polynomial
                }
            }
            crc = crc and 0xffff

            return crc.toString(16).uppercase().padStart(CRC_LENGTH, '0')
        }
    }
}