package dev.code93.emvqr.data.mapper

import dev.code93.emvqr.domain.model.EmvField
import dev.code93.emvqr.domain.model.EmvSection
import dev.code93.kmp.qrd.QRCodeEmvCoColombiaData
import dev.code93.kmp.qrd.data.DiscountApplicationType
import dev.code93.kmp.qrd.data.ImmediatePaymentKeyType
import javax.inject.Inject

/**
 * Convierte el resultado de la librería en secciones presentables, omitiendo
 * campos vacíos y secciones sin contenido. Títulos según la spec EASPBV v1.4.
 */
class EmvDataMapper @Inject constructor() {

    fun toSections(data: QRCodeEmvCoColombiaData): List<EmvSection> = buildList {
        data.conventionsQrCodeEmvCoData?.let { conventions ->
            section("Convenciones QR EMVCo") {
                field("Indicador EMV", conventions.indicatorEmv)
                field("Tipo de QR", qrTypeLabel(conventions.qrType))
                field("CRC", conventions.cyclicRedundancyCheck)
                field("Campo de seguridad", conventions.securityField)
            }
        }

        data.merchantInformationData?.let { merchant ->
            section("Información del comercio") {
                merchant.immediatePaymentKey?.forEach { (key, value) ->
                    field(paymentKeyLabel(key), value)
                }
                field("Red adquirente", merchant.acquirerNetworkId)
                field("Código de comercio", merchant.merchantCode)
                field("Código agregador", merchant.aggregatorMerchantCode)
            }
        }

        data.additionalMerchantInformationData?.let { additional ->
            section("Información adicional del comercio") {
                field("MCC", additional.merchantCategoryCode)
                field("País", additional.countryCode)
                field("Nombre", additional.merchantName)
                field("Ciudad", additional.merchantCity)
                field("Código postal", additional.postalCode)
                field("Canal", additional.channel)
                field("Condición IVA", taxConditionLabel(additional.taxIvaCondition))
                field("IVA", additional.taxIvaValue)
                field("Base IVA", additional.taxIvaBase)
                field("Condición INC", taxConditionLabel(additional.taxIncCondition))
                field("INC", additional.taxIncValue)
                field("ID de transacción", additional.transactionId)
            }
        }

        data.transactionDetailData?.let { transaction ->
            section("Detalle de la transacción") {
                field("Moneda", currencyLabel(transaction.currencyCode))
                field("Valor", transaction.transactionValue)
                field("Indicador de propina", transaction.tipIndicator)
                field("Valor propina", transaction.tipValue)
                field("Porcentaje propina", transaction.tipPercentage)
            }
        }

        data.merchantAdditionalFieldsData?.let { fields ->
            section("Campos adicionales del comercio") {
                field("Número de factura", fields.billingNumber)
                field("Número celular", fields.mobileNumber)
                field("Etiqueta de tienda", fields.storeLabel)
                field("Número de lealtad", fields.loyaltyNumber)
                field("Referencia", fields.referenceLabel)
                field("Etiqueta de cliente", fields.customerLabel)
                field("Terminal", fields.terminalLabel)
                field("Propósito", purposeLabel(fields.transactionPurpose))
                field("Datos del consumidor", fields.additionalConsumerData)
                field("NIT del comercio", fields.merchantTaxId)
                field("Canal de origen", fields.originChannel)
            }
        }

        data.otherTransactionsFieldsData?.let { other ->
            section("Otras transacciones") {
                field("Código de servicio", other.serviceCode)
                field("Referencia de pago", other.paymentReference)
                field("Tipo de producto", other.productType)
                field("Cuenta origen", other.sourceAccount)
                field("Cuenta destino", other.destinationAccount)
                field("Referencia cuenta destino", other.destinationAccountReference)
                field("Tipo producto transferencia", other.transferProductType)
                other.discountApplication?.forEach { (key, value) ->
                    field(discountLabel(key), value)
                }
            }
        }

        data.merchantInformationLanguageData?.let { language ->
            section("Idioma del comercio") {
                field("Idioma preferido", language.languagePreference)
                field("Nombre alternativo", language.alternateMerchantName)
                field("Ciudad alternativa", language.alternateMerchantCity)
            }
        }
    }

    private fun MutableList<EmvSection>.section(
        title: String,
        builder: MutableList<EmvField>.() -> Unit
    ) {
        val fields = buildList(builder)
        if (fields.isNotEmpty()) add(EmvSection(title, fields))
    }

    private fun MutableList<EmvField>.field(label: String, value: String?) {
        if (!value.isNullOrBlank()) add(EmvField(label, value))
    }

    private fun qrTypeLabel(type: String): String? = when (type) {
        "11" -> "11 — Estático"
        "12" -> "12 — Dinámico"
        else -> type.ifBlank { null }
    }

    private fun taxConditionLabel(condition: String?): String? = when (condition) {
        "01" -> "01 — Generado por la billetera"
        "02" -> "02 — Calculado por el comercio"
        "03" -> "03 — Porcentaje calculado por la billetera"
        else -> condition
    }

    private fun purposeLabel(purpose: String?): String? = when (purpose) {
        "00" -> "00 — Compra"
        "02" -> "02 — Anulación"
        "03" -> "03 — Transferencia"
        "04" -> "04 — Retiro"
        "05" -> "05 — Recaudo"
        "06" -> "06 — Recarga"
        "07" -> "07 — Depósito"
        else -> purpose
    }

    private fun currencyLabel(code: String): String? = when (code) {
        "170" -> "170 — Peso colombiano (COP)"
        else -> code.ifBlank { null }
    }

    private fun paymentKeyLabel(key: ImmediatePaymentKeyType): String = when (key) {
        ImmediatePaymentKeyType.NETWORK_ID -> "GUID de la llave"
        ImmediatePaymentKeyType.IDENTIFICATION_TYPE -> "Llave — identificación"
        ImmediatePaymentKeyType.PHONE_NUMBER -> "Llave — celular"
        ImmediatePaymentKeyType.EMAIL_ADDRESS -> "Llave — correo"
        ImmediatePaymentKeyType.ALPHANUMERIC_DATA -> "Llave — alfanumérica"
        ImmediatePaymentKeyType.MERCHANT_ID -> "Llave — Merchant ID"
    }

    private fun discountLabel(key: DiscountApplicationType): String = when (key) {
        DiscountApplicationType.GUID -> "GUID de descuento"
        DiscountApplicationType.DISCOUNT_INDICATOR -> "Indicador de descuento"
        DiscountApplicationType.DISCOUNT_AMOUNT -> "Monto descuento"
        DiscountApplicationType.IVA_DISCOUNT_AMOUNT -> "IVA del descuento"
        DiscountApplicationType.DISCOUNT_PERCENTAGE -> "Porcentaje descuento"
        DiscountApplicationType.DISCOUNT_VALUE -> "Valor descuento"
        DiscountApplicationType.DISCOUNT_QUERY -> "Consulta descuento"
    }
}
