package dev.code93.kmp.qrd.data

/**
 * Section VII of the EASPBV standard — transaction detail.
 *
 * @property currencyCode Currency code (tag `53`), ISO 4217 numeric; `"170"` for Colombian Peso (COP).
 * @property transactionValue Gross transaction amount (tag `54`), excluding taxes and tip.
 * @property tipIndicator Tip indicator (tag `55`): `"01"` the wallet must prompt
 *   for the tip (tags 56/57 empty), `"02"` fixed tip in tag 56, `"03"` percentage
 *   tip in tag 57. `null` means no tip.
 * @property tipValue Fixed tip amount (tag `56`); only when [tipIndicator] is `"02"`.
 * @property tipPercentage Tip percentage (tag `57`); only when [tipIndicator] is `"03"`.
 */
data class TransactionDetailData(
    val currencyCode: String,
    val transactionValue: String,
    val tipIndicator: String?,
    val tipValue: String?,
    val tipPercentage: String?
)
