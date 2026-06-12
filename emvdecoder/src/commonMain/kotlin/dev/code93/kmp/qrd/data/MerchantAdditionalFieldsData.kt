package dev.code93.kmp.qrd.data

import dev.code93.kmp.qrd.SubFieldType

/**
 * Section VI of the EASPBV standard — Additional Data Field Template (tag `62`).
 *
 * @property billingNumber Invoice number indicated by the merchant (sub-tag `01`).
 * @property mobileNumber Mobile number for financial operations (sub-tag `02`).
 * @property storeLabel Store label/distinctive (sub-tag `03`).
 * @property loyaltyNumber Loyalty program number (sub-tag `04`).
 * @property referenceLabel Transaction reference (sub-tag `05`). For voids,
 *   the first 12 characters are the RRN and the next 6 the approval number.
 * @property customerLabel Customer identification number (sub-tag `06`).
 * @property terminalLabel Alphanumeric terminal associated with the merchant (sub-tag `07`). `""` when absent.
 * @property transactionPurpose Purpose of the transaction (sub-tag `08`): `00` purchase,
 *   `02` void, `03` transfer, `04` withdrawal, `05` collection, `06` top-up, `07` deposit. `""` when absent.
 * @property additionalConsumerData Data the wallet must request from the consumer (sub-tag `09`):
 *   `A` address, `M` mobile number, `E` email.
 * @property merchantTaxId Merchant tax ID / NIT (sub-tag `10`).
 * @property originChannel Origin channel (sub-tag `11`): three digits — medium / location /
 *   merchant presentation, per annex tables 1.1–1.3 of the spec. Coexists with tag `80`.
 */
@ConsistentCopyVisibility
public data class MerchantAdditionalFieldsData internal constructor(
    public val billingNumber: String?,
    public val mobileNumber: String?,
    public val storeLabel: String?,
    public val loyaltyNumber: String?,
    public val referenceLabel: String?,
    public val customerLabel: String?,
    public val terminalLabel: String,
    public val transactionPurpose: String,
    public val additionalConsumerData: String?,
    public val merchantTaxId: String?,
    public val originChannel: String?
)

/**
 * Sub-fields of the Additional Data Field Template (tag `62`).
 */
public enum class MerchantAdditionalDataFieldType(override val subTag: String) : SubFieldType {
    /** Invoice number. */
    BILLING_NUMBER("01"),

    /** Mobile number. */
    MOBILE_NUMBER("02"),

    /** Store label. */
    STORE_LABEL("03"),

    /** Loyalty number. */
    LOYALTY_NUMBER("04"),

    /** Transaction reference (RRN + approval number for voids). */
    REFERENCE_LABEL("05"),

    /** Customer identification. */
    CUSTOMER_LABEL("06"),

    /** Terminal label. */
    TERMINAL_LABEL("07"),

    /**
     * Purpose: `00` purchase, `02` void, `03` transfer, `04` withdrawal,
     * `05` collection, `06` top-up, `07` deposit.
     */
    PURPOSE_OF_TRANSACTION("08"),

    /** Consumer data request: `A` address, `M` mobile, `E` email. */
    ADDITIONAL_CONSUMER_DATA("09"),

    /** Merchant tax ID (NIT). */
    MERCHANT_TAX_ID("10"),

    /** Origin channel (3 digits, annex tables 1.1–1.3). */
    ORIGIN_CHANNEL("11")
}
