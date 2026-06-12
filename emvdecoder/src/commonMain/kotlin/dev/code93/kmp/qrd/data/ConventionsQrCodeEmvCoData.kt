package dev.code93.kmp.qrd.data

import dev.code93.kmp.qrd.SubFieldType

/**
 * Section I of the EASPBV standard — QR Code EMVCo conventions.
 *
 * @property indicatorEmv Payload Format Indicator (tag `00`). Always `"01"` per the EMV standard.
 * @property qrType Point of Initiation Method (tag `01`): `"11"` = static QR, `"12"` = dynamic QR.
 *   A static QR (`11`) with a transaction amount > 0 is a "static with value" (hybrid) QR.
 * @property cyclicRedundancyCheck CRC checksum (tag `63`), computed per ISO/IEC 13239.
 *   Verify it with [dev.code93.kmp.qrd.CRCValidator.validate].
 * @property securityField SHA-256 security hash (tag `91`, sub-tag `01`). Empty when absent.
 */
data class ConventionsQrCodeEmvCoData(
    val indicatorEmv: String,
    val qrType: String,
    val cyclicRedundancyCheck: String,
    val securityField: String,
)

/**
 * Sub-fields of the security template (tag `91`).
 */
enum class SecurityFieldType(override val subTag: String) : SubFieldType {
    /** Globally Unique Identifier (`CO.COM.RBM.SEC` / `CO.COM.CRB.SEC`). */
    NETWORK_ID("00"),

    /** SHA-256 security hash. */
    HASH_VALUE("01")
}
