package dev.code93.kmp.qrd.data

import dev.code93.kmp.qrd.SubFieldType

data class MerchantInformationData(
    val immediatePaymentKey: Map<ImmediatePaymentKeyType, String?>?,
    val acquirerNetworkId: String?,
    val merchantCode: String?,
    val aggregatorMerchantCode: String?
)

enum class ImmediatePaymentKeyType(override val subTag: String) : SubFieldType {
    NETWORK_ID("00"),
    IDENTIFICATION_TYPE("01"),
    PHONE_NUMBER("02"),
    EMAIL_ADDRESS("03"),
    ALPHANUMERIC_DATA("04"),
    MERCHANT_ID("05")
}

enum class AcquirerNetworkIdType(override val subTag: String) : SubFieldType {
    GUID("00"),
    NETWORK_IDENTIFIER("01")
}

enum class MerchantCodeType(override val subTag: String) : SubFieldType {
    GUID("00"),
    CODE("01")
}

enum class AggregatorMerchantCodeType(override val subTag: String) : SubFieldType {
    GUID("00"),
    IDENTIFIER("01")
}

