package dev.code93.kmp.qrd.data

data class TransactionDetailData(
    val currencyCode: String,
    val transactionValue: String,
    val tipIndicator: String?,
    val tipValue: String?,
    val tipPercentage: String?
)
