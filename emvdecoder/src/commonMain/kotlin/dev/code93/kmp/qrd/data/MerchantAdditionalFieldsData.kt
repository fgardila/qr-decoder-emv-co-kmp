package dev.code93.kmp.qrd.data

import dev.code93.kmp.qrd.SubFieldType

data class MerchantAdditionalFieldsData(
    val billingNumber: String?,
    val mobileNumber: String?,
    val storeLabel: String?,
    val loyaltyNumber: String?,
    val referenceLabel: String?,
    val customerLabel: String?,
    val terminalLabel: String,
    val transactionPurpose: String,
    val additionalConsumerData: String?,
    val merchantTaxId: String?,
    val originChannel: String?
)

enum class MerchantAdditionalDataFieldType(override val subTag: String) : SubFieldType {
    BILLING_NUMBER("01"),
    MOBILE_NUMBER("02"),
    STORE_LABEL("03"),
    LOYALTY_NUMBER("04"),
    REFERENCE_LABEL("05"),
    CUSTOMER_LABEL("06"),
    TERMINAL_LABEL("07"),
    PURPOSE_OF_TRANSACTION("08"),
    ADDITIONAL_CONSUMER_DATA("09"),
    MERCHANT_TAX_ID("10"),
    ORIGIN_CHANNEL("11")
}