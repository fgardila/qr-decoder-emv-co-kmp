package dev.code93.kmp.qrd

import dev.code93.kmp.qrd.data.AcquirerNetworkIdType
import dev.code93.kmp.qrd.data.AdditionalMerchantInformationData
import dev.code93.kmp.qrd.data.AggregatorMerchantCodeType
import dev.code93.kmp.qrd.data.ChannelType
import dev.code93.kmp.qrd.data.ConventionsQrCodeEmvCoData
import dev.code93.kmp.qrd.data.DiscountApplicationType
import dev.code93.kmp.qrd.data.ImmediatePaymentKeyType
import dev.code93.kmp.qrd.data.IncConditionType
import dev.code93.kmp.qrd.data.IncValueType
import dev.code93.kmp.qrd.data.MerchantAdditionalDataFieldType
import dev.code93.kmp.qrd.data.MerchantAdditionalFieldsData
import dev.code93.kmp.qrd.data.MerchantCodeType
import dev.code93.kmp.qrd.data.MerchantInformationData
import dev.code93.kmp.qrd.data.MerchantInformationLanguageData
import dev.code93.kmp.qrd.data.MerchantInformationLanguageDataType
import dev.code93.kmp.qrd.data.OtherTransactionsFieldsData
import dev.code93.kmp.qrd.data.SecurityFieldType
import dev.code93.kmp.qrd.data.TaxIvaBaseType
import dev.code93.kmp.qrd.data.TaxIvaConditionType
import dev.code93.kmp.qrd.data.TaxIvaValueType
import dev.code93.kmp.qrd.data.TransactionDetailData
import dev.code93.kmp.qrd.data.TransactionIdType

class EmvQrCodeDecoder(qrCode: String) {
    private val dataElements: MutableMap<String, String> = mutableMapOf()

    init {
        parseQrCode(qrCode)
    }


    private fun parseQrCode(qrCode: String) {
        var index = 0
        while (index < qrCode.length) {
            val id = qrCode.substringOrNull(index, index + 2) ?: break
            index += 2

            val length = qrCode.substringOrNull(index, index + 2)?.toIntOrNull() ?: break
            index += 2

            val value = qrCode.substringOrNull(index, index + length) ?: break
            index += length

            dataElements[id] = value
        }
    }

    private inline fun <reified T> extractSubFields(tag: String): Map<T, String?> where T : Enum<T>, T : SubFieldType {
        val fieldValue = dataElements[tag] ?: return emptyMap()
        return enumValues<T>().associateWith { extractSubElement(fieldValue, it.subTag) }
    }

    private fun extractSubElement(fieldValue: String, subElementId: String): String? {
        var index = 0
        while (index < fieldValue.length) {
            val id = fieldValue.substringOrNull(index, index + 2) ?: break
            index += 2

            val lengthStr = fieldValue.substringOrNull(index, index + 2) ?: break
            val length = lengthStr.toIntOrNull() ?: break
            index += 2

            val value = fieldValue.substringOrNull(index, index + length) ?: break
            index += length

            if (id == subElementId) {
                return value
            }
        }
        return null
    }

    fun decode(): QRCodeEmvCoColombiaData {
        val conventionsQrCodeEmvCoData = getConventionsQrCodeEmvCoData()
        val merchantInformationData = getMerchantInformationData()
        val additionalMerchantInformationData = getAdditionalMerchantInformationData()
        val otherTransactionsFieldsData = getOtherTransactionsFieldsData()
        val merchantInformationLanguageData = getMerchantInformationLanguageData()
        val merchantAdditionalFieldsData = getAdditionalCommerceFieldsData()
        val transactionDetailData = getTransactionDetailData()

        return QRCodeEmvCoColombiaData(
            conventionsQrCodeEmvCoData = conventionsQrCodeEmvCoData,
            merchantInformationData = merchantInformationData,
            additionalMerchantInformationData = additionalMerchantInformationData,
            otherTransactionsFieldsData = otherTransactionsFieldsData,
            merchantInformationLanguageData = merchantInformationLanguageData,
            merchantAdditionalFieldsData = merchantAdditionalFieldsData,
            transactionDetailData = transactionDetailData
        )
    }

    private fun getMerchantInformationLanguageData(): MerchantInformationLanguageData {
        val languageData = extractSubFields<MerchantInformationLanguageDataType>("64")
        return MerchantInformationLanguageData(
            languagePreference = languageData[MerchantInformationLanguageDataType.LANGUAGE_PREFERENCE],
            alternateMerchantName = languageData[MerchantInformationLanguageDataType.ALTERNATE_MERCHANT_NAME],
            alternateMerchantCity = languageData[MerchantInformationLanguageDataType.ALTERNATE_MERCHANT_CITY]
        )
    }

    private fun getAdditionalCommerceFieldsData(): MerchantAdditionalFieldsData {
        val merchantAdditionalFieldsData = extractSubFields<MerchantAdditionalDataFieldType>("62")

        return MerchantAdditionalFieldsData(
            billingNumber = merchantAdditionalFieldsData[MerchantAdditionalDataFieldType.BILLING_NUMBER],
            mobileNumber = merchantAdditionalFieldsData[MerchantAdditionalDataFieldType.MOBILE_NUMBER],
            storeLabel = merchantAdditionalFieldsData[MerchantAdditionalDataFieldType.STORE_LABEL],
            loyaltyNumber = merchantAdditionalFieldsData[MerchantAdditionalDataFieldType.LOYALTY_NUMBER],
            referenceLabel = merchantAdditionalFieldsData[MerchantAdditionalDataFieldType.REFERENCE_LABEL],
            customerLabel = merchantAdditionalFieldsData[MerchantAdditionalDataFieldType.CUSTOMER_LABEL],
            terminalLabel = merchantAdditionalFieldsData[MerchantAdditionalDataFieldType.TERMINAL_LABEL]
                ?: "",
            transactionPurpose = merchantAdditionalFieldsData[MerchantAdditionalDataFieldType.PURPOSE_OF_TRANSACTION]
                ?: "",
            additionalConsumerData = merchantAdditionalFieldsData[MerchantAdditionalDataFieldType.ADDITIONAL_CONSUMER_DATA],
            merchantTaxId = merchantAdditionalFieldsData[MerchantAdditionalDataFieldType.MERCHANT_TAX_ID],
            originChannel = merchantAdditionalFieldsData[MerchantAdditionalDataFieldType.ORIGIN_CHANNEL]
        )

    }

    private fun getConventionsQrCodeEmvCoData(): ConventionsQrCodeEmvCoData {
        val securityField =
            extractSubFields<SecurityFieldType>("91")[SecurityFieldType.HASH_VALUE] ?: ""
        return ConventionsQrCodeEmvCoData(
            indicatorEmv = dataElements["00"] ?: "",
            qrType = dataElements["01"] ?: "",
            cyclicRedundancyCheck = dataElements["63"] ?: "",
            securityField = securityField
        )
    }

    private fun getMerchantInformationData(): MerchantInformationData {
        val immediatePaymentKey = extractSubFields<ImmediatePaymentKeyType>("26")
        val acquirerNetworkId =
            extractSubFields<AcquirerNetworkIdType>("49")[AcquirerNetworkIdType.NETWORK_IDENTIFIER]
        val merchantCode = extractSubFields<MerchantCodeType>("50")[MerchantCodeType.CODE]
        val aggregatorMerchantCode =
            extractSubFields<AggregatorMerchantCodeType>("51")[AggregatorMerchantCodeType.IDENTIFIER]

        return MerchantInformationData(
            immediatePaymentKey = immediatePaymentKey,
            acquirerNetworkId = acquirerNetworkId,
            merchantCode = merchantCode,
            aggregatorMerchantCode = aggregatorMerchantCode
        )
    }

    private fun getAdditionalMerchantInformationData(): AdditionalMerchantInformationData {
        val channel = extractSubFields<ChannelType>("80")[ChannelType.CHANNEL_VALUE]
        val taxIvaCondition = extractSubFields<TaxIvaConditionType>("81")[TaxIvaConditionType.VALUE]
        val taxIvaValue = extractSubFields<TaxIvaValueType>("82")[TaxIvaValueType.VALUE]
        val taxIvaBase = extractSubFields<TaxIvaBaseType>("83")[TaxIvaBaseType.BASE_VALUE]
        val taxIncCondition = extractSubFields<IncConditionType>("84")[IncConditionType.VALUE]
        val taxIncValue = extractSubFields<IncValueType>("85")[IncValueType.VALUE]
        val transactionId =
            extractSubFields<TransactionIdType>("86")[TransactionIdType.TRANSACTION_ID]

        return AdditionalMerchantInformationData(
            merchantCategoryCode = dataElements["52"] ?: "",
            countryCode = dataElements["58"] ?: "",
            merchantName = dataElements["59"] ?: "",
            merchantCity = dataElements["60"] ?: "",
            postalCode = dataElements["61"],
            channel = channel,
            taxIvaCondition = taxIvaCondition,
            taxIvaValue = taxIvaValue,
            taxIvaBase = taxIvaBase,
            taxIncCondition = taxIncCondition,
            taxIncValue = taxIncValue,
            taxes = null,
            transactionId = transactionId
        )
    }

    private fun getOtherTransactionsFieldsData(): OtherTransactionsFieldsData {
        val discountApplication = extractSubFields<DiscountApplicationType>("99")
        return OtherTransactionsFieldsData(
            serviceCode = dataElements["92"],
            paymentReference = dataElements["93"],
            productType = dataElements["94"],
            sourceAccount = dataElements["95"],
            destinationAccount = dataElements["96"],
            destinationAccountReference = dataElements["97"],
            transferProductType = dataElements["98"],
            discountApplication = discountApplication
        )
    }

    private fun getTransactionDetailData(): TransactionDetailData {
        return TransactionDetailData(
            currencyCode = dataElements["53"] ?: "", // Código de moneda
            transactionValue = dataElements["54"] ?: "", // Valor de la transacción
            tipIndicator = dataElements["55"], // Indicador de propina
            tipValue = dataElements["56"], // Valor de la propina
            tipPercentage = dataElements["57"] // Porcentaje de la propina
        )
    }

    private fun String.substringOrNull(startIndex: Int, endIndex: Int): String? =
        if (startIndex < length && endIndex <= length) this.substring(
            startIndex,
            endIndex
        ) else null
}

interface SubFieldType {
    val subTag: String
}