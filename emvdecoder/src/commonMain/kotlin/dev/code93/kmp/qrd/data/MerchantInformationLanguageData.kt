package dev.code93.kmp.qrd.data

import dev.code93.kmp.qrd.SubFieldType

/**
 * Section V of the EASPBV standard — Merchant Information Language Template
 * (tag `64`): merchant data in an alternate language.
 *
 * @property languagePreference Preferred language (sub-tag `00`), per ISO 639.
 * @property alternateMerchantName Merchant name in the preferred language (sub-tag `01`).
 * @property alternateMerchantCity Merchant city in the preferred language (sub-tag `02`).
 */
@ConsistentCopyVisibility
public data class MerchantInformationLanguageData internal constructor(
    public val languagePreference: String?,
    public val alternateMerchantName: String?,
    public val alternateMerchantCity: String?
)

/**
 * Sub-fields of the Merchant Information Language Template (tag `64`).
 */
public enum class MerchantInformationLanguageDataType(override val subTag: String) : SubFieldType {
    /** Language preference (ISO 639). */
    LANGUAGE_PREFERENCE("00"),

    /** Merchant name in the alternate language. */
    ALTERNATE_MERCHANT_NAME("01"),

    /** Merchant city in the alternate language. */
    ALTERNATE_MERCHANT_CITY("02")
}
