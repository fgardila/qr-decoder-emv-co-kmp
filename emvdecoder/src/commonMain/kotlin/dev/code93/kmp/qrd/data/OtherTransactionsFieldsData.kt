package dev.code93.kmp.qrd.data

import dev.code93.kmp.qrd.SubFieldType

data class OtherTransactionsFieldsData(
    val serviceCode: String?,
    val paymentReference: String?,
    val productType: String?,
    val sourceAccount: String?,
    val destinationAccount: String?,
    val destinationAccountReference: String?,
    val transferProductType: String?,
    val discountApplication: Map<DiscountApplicationType, String?>?
)

enum class DiscountApplicationType(override val subTag: String) : SubFieldType {
    DISCOUNT_INDICATOR("01"),    // Indicador de descuento
    DISCOUNT_AMOUNT("02"),       // Monto del descuento
    IVA_DISCOUNT_AMOUNT("03"),   // IVA sobre el monto del descuento
    DISCOUNT_PERCENTAGE("04"),   // Porcentaje del descuento
    DISCOUNT_VALUE("05"),        // Valor del descuento
    DISCOUNT_QUERY("06")         // Consulta sobre el descuento
}


