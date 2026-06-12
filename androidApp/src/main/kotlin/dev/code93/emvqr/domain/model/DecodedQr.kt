package dev.code93.emvqr.domain.model

/** Resultado de decodificar un QR: secciones legibles + integridad + diagnósticos. */
data class DecodedQr(
    val rawText: String,
    val crcValid: Boolean,
    val sections: List<EmvSection>,
    val diagnostics: DecodeDiagnostics
)

/** Sección del estándar EASPBV con sus campos no vacíos. */
data class EmvSection(
    val title: String,
    val fields: List<EmvField>
)

data class EmvField(
    val label: String,
    val value: String
)

/** Observabilidad del parsing laxo de la librería. */
data class DecodeDiagnostics(
    val parsedTagCount: Int,
    val consumedChars: Int,
    val totalChars: Int,
    val isFullyParsed: Boolean
)
