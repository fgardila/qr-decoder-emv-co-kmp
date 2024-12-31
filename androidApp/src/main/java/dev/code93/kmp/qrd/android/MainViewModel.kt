package dev.code93.android.emvreaderqr

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.code93.kmp.qrd.CRCValidator
import dev.code93.kmp.qrd.EmvQrCodeDecoder
import dev.code93.kmp.qrd.QRCodeEmvCoColombiaData
import dev.code93.kmp.qrd.android.ExpandableItem
import dev.code93.kmp.qrd.android.MainActivity
import dev.code93.kmp.qrd.data.*

class MainViewModel : ViewModel() {

    val expandableList = mutableStateListOf<ExpandableItem>()
    val alertDialogState = mutableStateOf(Triple(false, "", ""))

    fun processQRCode(data: String) {
        val decoder = EmvQrCodeDecoder(data)
        val decodedData = decoder.decode()

        if (CRCValidator.validate(data)) {
            updateExpandableList(decodedData)
        } else {
            showError("Error al validar CRC.")
        }
    }

    private fun updateExpandableList(data: QRCodeEmvCoColombiaData) {
        expandableList.clear()
        data.conventionsQrCodeEmvCoData?.let {
            updateConventions(it)
        }
        data.merchantInformationData?.let {
            updateMerchantInformation(it)
        }
        data.additionalMerchantInformationData?.let {
            updateAdditionalMerchantInformation(it)
        }
        data.otherTransactionsFieldsData?.let {
            updateOtherTransactionsFields(it)
        }
        data.merchantInformationLanguageData?.let {
            updateMerchantInformationLanguage(it)
        }
        data.merchantAdditionalFieldsData?.let {
            updateMerchantAdditionalFields(it)
        }
        data.transactionDetailData?.let {
            updateTransactionDetail(it)
        }
    }

    private fun updateConventions(it: ConventionsQrCodeEmvCoData) {
        expandableList.add(
            ExpandableItem(
                title = "Conventions QR Code EMV CO",
                items = listOf(
                    "Indicator EMV" to it.indicatorEmv,
                    "QR Type" to it.qrType,
                    "Cyclic Redundancy Check" to it.cyclicRedundancyCheck,
                    "Security Field" to it.securityField
                )
            )
        )
    }

    private fun updateMerchantInformation(it: MerchantInformationData) {
        val items = mutableListOf<Pair<String, String?>>()

        it.immediatePaymentKey?.forEach { (key, value) ->
            items.add(key.name to value)
        }
        items.add("Acquirer Network ID" to it.acquirerNetworkId)
        items.add("Merchant Code" to it.merchantCode)
        items.add("Aggregator Merchant Code" to it.aggregatorMerchantCode)

        expandableList.add(
            ExpandableItem(
                title = "Merchant Information",
                items = items
            )
        )
    }

    private fun updateAdditionalMerchantInformation(it: AdditionalMerchantInformationData) {
        val items = mutableListOf<Pair<String, String?>>()

        items.add("Merchant Category Code" to it.merchantCategoryCode)
        items.add("Country Code" to it.countryCode)
        items.add("Merchant Name" to it.merchantName)
        items.add("Merchant City" to it.merchantCity)
        items.add("Postal Code" to it.postalCode)
        items.add("Channel" to it.channel)
        items.add("Tax IVA Condition" to it.taxIvaCondition)
        items.add("Tax IVA Value" to it.taxIvaValue)
        items.add("Tax IVA Base" to it.taxIvaBase)
        items.add("Tax INC Condition" to it.taxIncCondition)
        items.add("Tax INC Value" to it.taxIncValue)
        items.add("Taxes" to it.taxes)
        items.add("Transaction ID" to it.transactionId)

        expandableList.add(
            ExpandableItem(
                title = "Additional Merchant Information",
                items = items
            )
        )
    }

    private fun updateOtherTransactionsFields(it: OtherTransactionsFieldsData) {
        val items = mutableListOf<Pair<String, String?>>()

        items.add("Service Code" to it.serviceCode)
        items.add("Payment Reference" to it.paymentReference)
        items.add("Product Type" to it.productType)
        items.add("Source Account" to it.sourceAccount)
        items.add("Destination Account" to it.destinationAccount)
        items.add("Destination Account Reference" to it.destinationAccountReference)
        items.add("Transfer Product Type" to it.transferProductType)

        it.discountApplication?.forEach { (key, value) ->
            items.add(key.name to value)
        }

        expandableList.add(
            ExpandableItem(
                title = "Other Transactions Fields",
                items = items
            )
        )
    }

    private fun updateMerchantInformationLanguage(it: MerchantInformationLanguageData) {
        expandableList.add(
            ExpandableItem(
                title = "Merchant Information Language",
                items = listOf(
                    "Languaje Preference" to it.languagePreference,
                    "Alternate Merchant Name " to it.alternateMerchantName,
                    "Merchant City Name" to it.alternateMerchantCity
                )
            )
        )
    }

    private fun updateMerchantAdditionalFields(it: MerchantAdditionalFieldsData) {
        val items = mutableListOf<Pair<String, String?>>()

        items.add("Billing Number" to it.billingNumber)
        items.add("Mobile Number" to it.mobileNumber)
        items.add("Store Label" to it.storeLabel)
        items.add("Loyalty Number" to it.loyaltyNumber)
        items.add("Reference Label" to it.referenceLabel)
        items.add("Customer Label" to it.customerLabel)
        items.add("Terminal Label" to it.terminalLabel)
        items.add("Transaction Purpose" to it.transactionPurpose)
        items.add("Additional Consumer Data" to it.additionalConsumerData)
        items.add("Merchant Tax ID" to it.merchantTaxId)
        items.add("Origin Channel" to it.originChannel)

        expandableList.add(
            ExpandableItem(
                title = "Merchant Additional Fields",
                items = items
            )
        )
    }

    private fun updateTransactionDetail(it: TransactionDetailData) {
        val items = mutableListOf<Pair<String, String?>>()

        items.add("Currency Code" to it.currencyCode)
        items.add("Transaction Value" to it.transactionValue)
        items.add("Tip Indicator" to it.tipIndicator)
        items.add("Tip Value" to it.tipValue)
        items.add("Tip Percentage" to it.tipPercentage)

        expandableList.add(
            ExpandableItem(
                title = "Transaction Detail",
                items = items
            )
        )
    }


    fun showDialog(title: String, message: String) {
        MainActivity.alertDialogState.value = Triple(true, title, message)
    }

    private fun showError(message: String) {
        alertDialogState.value = Triple(true, "Error", message)
    }
}