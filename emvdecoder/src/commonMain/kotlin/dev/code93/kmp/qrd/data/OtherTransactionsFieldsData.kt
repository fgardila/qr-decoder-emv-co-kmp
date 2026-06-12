package dev.code93.kmp.qrd.data

import dev.code93.kmp.qrd.SubFieldType

/**
 * Section IV of the EASPBV standard — fields for other transaction types:
 * collections, top-ups, transfers, deposits and withdrawals (tags `92`–`99`).
 *
 * @property serviceCode Service code for collection/top-up operations (tag `92`).
 * @property paymentReference Payment reference (collection) or mobile number (top-up) (tag `93`).
 * @property productType Product type for collections (tag `94`).
 * @property sourceAccount Source account for transfers (tag `95`): first 3 chars
 *   are the entity FIID, the next 19 the account number.
 * @property destinationAccount Destination account for transfers (tag `96`); same layout as [sourceAccount].
 * @property destinationAccountReference Additional destination reference (tag `97`): document type + number.
 * @property transferProductType Product type for transfers (tag `98`):
 *   `01` savings account, `02` checking account, `03` wallets.
 * @property discountApplication Discount application template (tag `99`), keyed by
 *   [DiscountApplicationType]; absent sub-tags map to `null`.
 */
@ConsistentCopyVisibility
public data class OtherTransactionsFieldsData internal constructor(
    public val serviceCode: String?,
    public val paymentReference: String?,
    public val productType: String?,
    public val sourceAccount: String?,
    public val destinationAccount: String?,
    public val destinationAccountReference: String?,
    public val transferProductType: String?,
    public val discountApplication: Map<DiscountApplicationType, String?>?
)

/**
 * Sub-fields of the discount application template (tag `99`).
 */
public enum class DiscountApplicationType(override val subTag: String) : SubFieldType {
    /** Globally Unique Identifier (`CO.COM.RBM.DESC` / `CO.COM.CRB.DESC`). */
    GUID("00"),

    /** Discount indicator (mandatory within the template). */
    DISCOUNT_INDICATOR("01"),

    /** Discount amount (optional). */
    DISCOUNT_AMOUNT("02"),

    /** IVA over the discount amount (optional). */
    IVA_DISCOUNT_AMOUNT("03"),

    /** Discount percentage (optional). */
    DISCOUNT_PERCENTAGE("04"),

    /** Discount value (optional). */
    DISCOUNT_VALUE("05"),

    /** Discount query (mandatory): `01` read sub-tags 02–05, `02` query the merchant. */
    DISCOUNT_QUERY("06")
}
