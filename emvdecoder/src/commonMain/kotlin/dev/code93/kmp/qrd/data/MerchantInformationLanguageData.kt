package dev.code93.kmp.qrd.data

import dev.code93.kmp.qrd.SubFieldType

data class MerchantInformationLanguageData(
    val languagePreference: String?,
    val alternateMerchantName: String?,
    val alternateMerchantCity: String?
)

enum class MerchantInformationLanguageDataType(override val subTag: String) : SubFieldType {
    LANGUAGE_PREFERENCE("00"),
    ALTERNATE_MERCHANT_NAME("01"),
    ALTERNATE_MERCHANT_CITY("02")
}
