package dev.code93.kmp.qrd.data

import dev.code93.kmp.qrd.SubFieldType

data class AdditionalMerchantInformationData(
    val merchantCategoryCode: String,
    val countryCode: String,
    val merchantName: String,
    val merchantCity: String,
    val postalCode: String?,
    val channel: String?,
    val taxIvaCondition: String?,
    val taxIvaValue: String?,
    val taxIvaBase: String?,
    val taxIncCondition: String?,
    val taxIncValue: String?,
    val taxes: String?,
    val transactionId: String?
)

enum class ChannelType(override val subTag: String) : SubFieldType {
    GUID("00"),
    CHANNEL_VALUE("01")
}

enum class TaxIvaConditionType(override val subTag: String) : SubFieldType {
    GUID("00"),
    VALUE("01")
}

enum class TaxIvaValueType(override val subTag: String) : SubFieldType {
    GUID("00"),
    VALUE("01") // Valor o porcentaje del IVA
}

enum class TaxIvaBaseType(override val subTag: String) : SubFieldType {
    GUID("00"),
    BASE_VALUE("01") // Valor base del IVA
}

enum class IncConditionType(override val subTag: String) : SubFieldType {
    GUID("00"),
    VALUE("01")
}

enum class IncValueType(override val subTag: String) : SubFieldType {
    GUID("00"),
    VALUE("01") // Valor o porcentaje del INC
}

enum class TaxesType(override val subTag: String) : SubFieldType {
    GUID("00"), // Reservado para próximos impuestos
    VALUE("01")
}

enum class TransactionIdType(override val subTag: String) : SubFieldType {
    GUID("00"),
    TRANSACTION_ID("01") // ID de la transacción
}

