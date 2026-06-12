package dev.code93.kmp.qrd.data

import dev.code93.kmp.qrd.SubFieldType

/**
 * Section II of the EASPBV standard — merchant identification.
 *
 * @property immediatePaymentKey Multi-key template for instant payments /
 *   Bre-B "Llaves" (tag `26`), keyed by [ImmediatePaymentKeyType]. The map
 *   contains every key type; absent sub-tags map to `null`. Per the standard,
 *   when this template carries a key the wallet should use it; otherwise it
 *   falls back to the merchant code (tag `50`).
 * @property acquirerNetworkId Acquirer network identifier (tag `49`, sub-tag `01`),
 *   e.g. `"RBM"` (Redeban), `"CRB"` (Credibanco), `"ACH"`, `"VISA"` — see spec annex table 1.4.
 * @property merchantCode Merchant code (tag `50`, sub-tag `01`).
 * @property aggregatorMerchantCode Aggregator merchant identifier (tag `51`, sub-tag `01`).
 */
data class MerchantInformationData(
    val immediatePaymentKey: Map<ImmediatePaymentKeyType, String?>?,
    val acquirerNetworkId: String?,
    val merchantCode: String?,
    val aggregatorMerchantCode: String?
)

/**
 * Sub-fields of the immediate-payment key template (tag `26`) — the "Llave"
 * (alias) used for instant transfers/payments such as Bre-B.
 */
enum class ImmediatePaymentKeyType(override val subTag: String) : SubFieldType {
    /** Globally Unique Identifier (`CO.COM.RBM.LLA` / `CO.COM.CRB.LLA`). */
    NETWORK_ID("00"),

    /** Identification document (c.c., c.e., passport, NUIP). */
    IDENTIFICATION_TYPE("01"),

    /** Mobile phone number key. */
    PHONE_NUMBER("02"),

    /** Email address key. */
    EMAIL_ADDRESS("03"),

    /** Alphanumeric key (e.g. `@ocfrf115`). */
    ALPHANUMERIC_DATA("04"),

    /** Merchant ID key — coexists with the merchant code in tag `50`. */
    MERCHANT_ID("05")
}

/**
 * Sub-fields of the acquirer network template (tag `49`).
 */
enum class AcquirerNetworkIdType(override val subTag: String) : SubFieldType {
    /** Globally Unique Identifier (`CO.COM.<RED>.RED`). */
    GUID("00"),

    /** Network identifier (up to four characters), e.g. `RBM`, `CRB`, `ACH`, `VCSS`. */
    NETWORK_IDENTIFIER("01")
}

/**
 * Sub-fields of the merchant code template (tag `50`).
 */
enum class MerchantCodeType(override val subTag: String) : SubFieldType {
    /** Globally Unique Identifier (`CO.COM.RBM.CU` / `CO.COM.CRB.CU`). */
    GUID("00"),

    /** Merchant code assigned by the acquirer. */
    CODE("01")
}

/**
 * Sub-fields of the aggregator merchant template (tag `51`).
 */
enum class AggregatorMerchantCodeType(override val subTag: String) : SubFieldType {
    /** Globally Unique Identifier (`CO.COM.RBM.CA` / `CO.COM.CRB.CA`). */
    GUID("00"),

    /** Aggregator identifier derived from the merchant's legal name. */
    IDENTIFIER("01")
}
