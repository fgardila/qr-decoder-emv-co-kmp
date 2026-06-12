package dev.code93.emvqr.domain.model

/** Redes adquirentes del anexo 1.4 del estándar EASPBV. */
enum class PaymentNetwork(val networkId: String, val guidPrefix: String) {
    REDEBAN("RBM", "CO.COM.RBM"),
    CREDIBANCO("CRB", "CO.COM.CRB");
}

/** Tipos de llave de pagos inmediatos (sub-tags del template 26). */
enum class PaymentKeyType(val subTag: String) {
    PHONE("02"),
    EMAIL("03"),
    ALPHANUMERIC("04"),
    MERCHANT_ID("05");
}

/**
 * Parámetros mínimos para generar un QR estático de prueba EASPBV v1.4.
 * Debe venir una llave ([keyType]+[keyValue]) o un [merchantCode].
 */
data class QrGenerationParams(
    val network: PaymentNetwork,
    val keyType: PaymentKeyType?,
    val keyValue: String?,
    val merchantCode: String?,
    val merchantName: String,
    val merchantCity: String,
    val mcc: String = "0000",
    val amount: String? = null
)
