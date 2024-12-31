import Foundation
import SwiftUI
import emvdecoder

class MainViewModel: ObservableObject {
    @Published var successMessage: AlertMessage? = nil
    @Published var errorMessage: AlertMessage? = nil
    @Published var expandableList: [ExpandableItem] = []
    
    func processQRCode(data: String) {
        let validator = CRCValidator.Companion().validate(qrCode: data)
        
        if validator {
            let decoder = EmvQrCodeDecoder(qrCode: data)
            let decodedData = decoder.decode()
            updateExpandableList(data: decodedData)
        } else {
            showError("Error en la validación del código QR.")
        }
    }
    
    private func updateExpandableList(data: QRCodeEmvCoColombiaData) {
        expandableList.removeAll()
        
        if let conventions = data.conventionsQrCodeEmvCoData {
            updateConventions(data: conventions)
        }
        if let merchantInfo = data.merchantInformationData {
            updateMerchantInformation(data: merchantInfo)
        }
        if let additionalInfo = data.additionalMerchantInformationData {
            updateAdditionalMerchantInformation(data: additionalInfo)
        }
        if let otherTransactionsFields = data.otherTransactionsFieldsData {
            updateOtherTransactionsFields(data: otherTransactionsFields)
        }
        
        let sizeList = expandableList
        print(sizeList)
    }
    
    private func updateConventions(data: ConventionsQrCodeEmvCoData) {
        let item = ExpandableItem(
            title: "Conventions QR Code EMV CO",
            items: [
                ("Indicator EMV", data.indicatorEmv),
                ("QR Type", data.qrType),
                ("Cyclic Redundancy Check", data.cyclicRedundancyCheck),
                ("Security Field", data.securityField)
            ]
        )
        expandableList.append(item)
    }
    
    private func updateMerchantInformation(data: MerchantInformationData) {
        var items: [(String, String?)] = []
        
        // Add immediate payment key items
        if let immediatePayment = data.immediatePaymentKey {
            for (key, value) in immediatePayment {
                let tKey = key
                let tValue = value
                items.append((key.name, immediatePayment.description))
            }
        }
        
        // Add other merchant information
        items.append(("Acquirer Network ID", data.acquirerNetworkId))
        items.append(("Merchant Code", data.merchantCode))
        items.append(("Aggregator Merchant Code", data.aggregatorMerchantCode))
        
        let item = ExpandableItem(
            title: "Merchant Information",
            items: items
        )
        expandableList.append(item)
    }
    
    private func updateAdditionalMerchantInformation(data: AdditionalMerchantInformationData) {
        let items: [(String, String?)] = [
            ("Merchant Category Code", data.merchantCategoryCode),
            ("Country Code", data.countryCode),
            ("Merchant Name", data.merchantName),
            ("Merchant City", data.merchantCity),
            ("Postal Code", data.postalCode),
            ("Channel", data.channel),
            ("Tax IVA Condition", data.taxIvaCondition),
            ("Tax IVA Value", data.taxIvaValue),
            ("Tax IVA Base", data.taxIvaBase),
            ("Tax INC Condition", data.taxIncCondition),
            ("Tax INC Value", data.taxIncValue),
            ("Taxes", data.taxes),
            ("Transaction ID", data.transactionId)
        ]
        
        let item = ExpandableItem(
            title: "Additional Merchant Information",
            items: items
        )
        expandableList.append(item)
    }
    
    private func updateOtherTransactionsFields(data: OtherTransactionsFieldsData) {
        var items: [(String, String?)] = []
        
        items.append(("Service Code", data.serviceCode))
        items.append(("Payment Reference", data.paymentReference))
        items.append(("Product Type", data.productType))
        items.append(("Source Account", data.sourceAccount))
        items.append(("Destination Account", data.destinationAccount))
        items.append(("Destination Account Reference", data.destinationAccountReference))
        items.append(("Transfer Product Type", data.transferProductType))
        
        let discountApplication = data.discountApplication
        
        let item = ExpandableItem(
            title: "Other Transactions Fields", items: items
        )
        expandableList.append(item)
    }
    
    private func showSuccess(_ message: String) {
        DispatchQueue.main.async {
            self.errorMessage = nil  // Clear any existing error message
            self.successMessage = AlertMessage(text: message)
        }
    }
    
    private func showError(_ message: String) {
        DispatchQueue.main.async {
            self.successMessage = nil  // Clear any existing success message
            self.errorMessage = AlertMessage(text: message)
        }
    }
}
