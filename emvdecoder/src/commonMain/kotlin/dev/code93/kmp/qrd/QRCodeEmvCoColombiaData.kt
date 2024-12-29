package dev.code93.kmp.qrd

import dev.code93.kmp.qrd.data.AdditionalMerchantInformationData
import dev.code93.kmp.qrd.data.ConventionsQrCodeEmvCoData
import dev.code93.kmp.qrd.data.MerchantAdditionalFieldsData
import dev.code93.kmp.qrd.data.MerchantInformationData
import dev.code93.kmp.qrd.data.MerchantInformationLanguageData
import dev.code93.kmp.qrd.data.OtherTransactionsFieldsData
import dev.code93.kmp.qrd.data.TransactionDetailData

data class QRCodeEmvCoColombiaData(
    val conventionsQrCodeEmvCoData: ConventionsQrCodeEmvCoData?,
    val merchantInformationData: MerchantInformationData?,
    val additionalMerchantInformationData: AdditionalMerchantInformationData?,
    val otherTransactionsFieldsData: OtherTransactionsFieldsData?,
    val merchantInformationLanguageData: MerchantInformationLanguageData?,
    val merchantAdditionalFieldsData: MerchantAdditionalFieldsData?,
    val transactionDetailData: TransactionDetailData?
)