package dev.code93.kmp.qrd

import dev.code93.kmp.qrd.data.AdditionalMerchantInformationData
import dev.code93.kmp.qrd.data.ConventionsQrCodeEmvCoData
import dev.code93.kmp.qrd.data.MerchantAdditionalFieldsData
import dev.code93.kmp.qrd.data.MerchantInformationData
import dev.code93.kmp.qrd.data.MerchantInformationLanguageData
import dev.code93.kmp.qrd.data.OtherTransactionsFieldsData
import dev.code93.kmp.qrd.data.TransactionDetailData

/**
 * Result of decoding a Colombian EMVCo QR payload, grouped by the seven
 * sections of the EASPBV v1.4-2025 standard.
 *
 * Produced by [EmvQrCodeDecoder.decode]. All sections are present on a decoded
 * instance; fields inside each section are `null` (or `""` where the contract
 * defines non-null defaults) when the corresponding tag was absent.
 *
 * @property conventionsQrCodeEmvCoData Section I — EMV indicator, QR type, CRC and security hash.
 * @property merchantInformationData Section II — immediate-payment keys (Llaves), acquirer network, merchant codes.
 * @property additionalMerchantInformationData Section III — MCC, location, IVA/INC taxes, channel, transaction ID.
 * @property otherTransactionsFieldsData Section IV — transfers, collections, top-ups and discounts (tags 92–99).
 * @property merchantInformationLanguageData Section V — merchant info in an alternate language (tag 64).
 * @property merchantAdditionalFieldsData Section VI — additional data field template (tag 62).
 * @property transactionDetailData Section VII — currency, amount and tip fields (tags 53–57).
 */
data class QRCodeEmvCoColombiaData(
    val conventionsQrCodeEmvCoData: ConventionsQrCodeEmvCoData?,
    val merchantInformationData: MerchantInformationData?,
    val additionalMerchantInformationData: AdditionalMerchantInformationData?,
    val otherTransactionsFieldsData: OtherTransactionsFieldsData?,
    val merchantInformationLanguageData: MerchantInformationLanguageData?,
    val merchantAdditionalFieldsData: MerchantAdditionalFieldsData?,
    val transactionDetailData: TransactionDetailData?
)
