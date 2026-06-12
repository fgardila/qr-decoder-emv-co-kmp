import Foundation
import emvdecoder

/// Adapta la librería KMP (EmvQr) al dominio de la app, agrupando los campos
/// por secciones del estándar EASPBV v1.4 y omitiendo valores vacíos.
struct EmvDecoderRepository: EmvDecoding {

    func decode(rawText: String) -> DecodedQr {
        let result = EmvQr.shared.decodeWithDiagnostics(rawText: rawText)
        let data = result.data

        var sections: [EmvSection] = []

        if let conventions = data.conventionsQrCodeEmvCoData {
            sections.appendSection("Convenciones QR EMVCo", fields: [
                ("Indicador EMV", conventions.indicatorEmv),
                ("Tipo de QR", qrTypeLabel(conventions.qrType)),
                ("CRC", conventions.cyclicRedundancyCheck),
                ("Campo de seguridad", conventions.securityField)
            ])
        }

        if let merchant = data.merchantInformationData {
            var fields: [(String, String?)] = []
            if let paymentKey = merchant.immediatePaymentKey {
                for (key, value) in paymentKey {
                    fields.append((paymentKeyLabel(key), value as? String))
                }
            }
            fields.append(("Red adquirente", merchant.acquirerNetworkId))
            fields.append(("Código de comercio", merchant.merchantCode))
            fields.append(("Código agregador", merchant.aggregatorMerchantCode))
            sections.appendSection("Información del comercio", fields: fields)
        }

        if let additional = data.additionalMerchantInformationData {
            sections.appendSection("Información adicional del comercio", fields: [
                ("MCC", additional.merchantCategoryCode),
                ("País", additional.countryCode),
                ("Nombre", additional.merchantName),
                ("Ciudad", additional.merchantCity),
                ("Código postal", additional.postalCode),
                ("Canal", additional.channel),
                ("Condición IVA", taxConditionLabel(additional.taxIvaCondition)),
                ("IVA", additional.taxIvaValue),
                ("Base IVA", additional.taxIvaBase),
                ("Condición INC", taxConditionLabel(additional.taxIncCondition)),
                ("INC", additional.taxIncValue),
                ("ID de transacción", additional.transactionId)
            ])
        }

        if let transaction = data.transactionDetailData {
            sections.appendSection("Detalle de la transacción", fields: [
                ("Moneda", currencyLabel(transaction.currencyCode)),
                ("Valor", transaction.transactionValue),
                ("Indicador de propina", transaction.tipIndicator),
                ("Valor propina", transaction.tipValue),
                ("Porcentaje propina", transaction.tipPercentage)
            ])
        }

        if let fields = data.merchantAdditionalFieldsData {
            sections.appendSection("Campos adicionales del comercio", fields: [
                ("Número de factura", fields.billingNumber),
                ("Número celular", fields.mobileNumber),
                ("Etiqueta de tienda", fields.storeLabel),
                ("Número de lealtad", fields.loyaltyNumber),
                ("Referencia", fields.referenceLabel),
                ("Etiqueta de cliente", fields.customerLabel),
                ("Terminal", fields.terminalLabel),
                ("Propósito", purposeLabel(fields.transactionPurpose)),
                ("Datos del consumidor", fields.additionalConsumerData),
                ("NIT del comercio", fields.merchantTaxId),
                ("Canal de origen", fields.originChannel)
            ])
        }

        if let other = data.otherTransactionsFieldsData {
            var fields: [(String, String?)] = [
                ("Código de servicio", other.serviceCode),
                ("Referencia de pago", other.paymentReference),
                ("Tipo de producto", other.productType),
                ("Cuenta origen", other.sourceAccount),
                ("Cuenta destino", other.destinationAccount),
                ("Referencia cuenta destino", other.destinationAccountReference),
                ("Tipo producto transferencia", other.transferProductType)
            ]
            if let discount = other.discountApplication {
                for (key, value) in discount {
                    fields.append((discountLabel(key), value as? String))
                }
            }
            sections.appendSection("Otras transacciones", fields: fields)
        }

        if let language = data.merchantInformationLanguageData {
            sections.appendSection("Idioma del comercio", fields: [
                ("Idioma preferido", language.languagePreference),
                ("Nombre alternativo", language.alternateMerchantName),
                ("Ciudad alternativa", language.alternateMerchantCity)
            ])
        }

        return DecodedQr(
            rawText: rawText,
            crcValid: EmvQr.shared.isCrcValid(rawText: rawText),
            sections: sections,
            diagnostics: DecodeDiagnostics(
                parsedTagCount: Int(result.diagnostics.parsedTagCount),
                consumedChars: Int(result.diagnostics.consumedChars),
                totalChars: Int(result.diagnostics.totalChars),
                isFullyParsed: result.diagnostics.isFullyParsed
            )
        )
    }

    // MARK: - Etiquetas semánticas (spec EASPBV v1.4)

    private func qrTypeLabel(_ type: String) -> String? {
        switch type {
        case "11": return "11 — Estático"
        case "12": return "12 — Dinámico"
        default: return type.isEmpty ? nil : type
        }
    }

    private func taxConditionLabel(_ condition: String?) -> String? {
        switch condition {
        case "01": return "01 — Generado por la billetera"
        case "02": return "02 — Calculado por el comercio"
        case "03": return "03 — Porcentaje calculado por la billetera"
        default: return condition
        }
    }

    private func purposeLabel(_ purpose: String?) -> String? {
        switch purpose {
        case "00": return "00 — Compra"
        case "02": return "02 — Anulación"
        case "03": return "03 — Transferencia"
        case "04": return "04 — Retiro"
        case "05": return "05 — Recaudo"
        case "06": return "06 — Recarga"
        case "07": return "07 — Depósito"
        default: return purpose
        }
    }

    private func currencyLabel(_ code: String) -> String? {
        code == "170" ? "170 — Peso colombiano (COP)" : (code.isEmpty ? nil : code)
    }

    private func paymentKeyLabel(_ key: ImmediatePaymentKeyType) -> String {
        switch key {
        case .networkId: return "GUID de la llave"
        case .identificationType: return "Llave — identificación"
        case .phoneNumber: return "Llave — celular"
        case .emailAddress: return "Llave — correo"
        case .alphanumericData: return "Llave — alfanumérica"
        case .merchantId: return "Llave — Merchant ID"
        default: return key.name
        }
    }

    private func discountLabel(_ key: DiscountApplicationType) -> String {
        switch key {
        case .guid: return "GUID de descuento"
        case .discountIndicator: return "Indicador de descuento"
        case .discountAmount: return "Monto descuento"
        case .ivaDiscountAmount: return "IVA del descuento"
        case .discountPercentage: return "Porcentaje descuento"
        case .discountValue: return "Valor descuento"
        case .discountQuery: return "Consulta descuento"
        default: return key.name
        }
    }
}

private extension Array where Element == EmvSection {
    mutating func appendSection(_ title: String, fields: [(String, String?)]) {
        let nonEmpty = fields.compactMap { label, value -> EmvField? in
            guard let value, !value.isEmpty else { return nil }
            return EmvField(label: label, value: value)
        }
        if !nonEmpty.isEmpty {
            append(EmvSection(title: title, fields: nonEmpty))
        }
    }
}
