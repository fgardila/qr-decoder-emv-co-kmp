package dev.code93.kmp.qrd

/**
 * Entry point of the library: decodes EMVCo Merchant-Presented QR codes
 * following the Colombian industry standard **EASPBV v1.4-2025**
 * (Redeban, Credibanco, Bre-B).
 *
 * Decoding is **lenient by design**: malformed input never throws. The parser
 * extracts every well-formed TLV element and stops silently at the first
 * invalid one; absent fields decode as `null` (or `""` for the few fields the
 * contract defines as non-null). Standard compliance is the authorizing
 * backend's responsibility, not this client library's — use
 * [decodeWithDiagnostics] when you want to observe how much of the payload
 * was actually parsed.
 *
 * Decoding does **not** verify integrity; check the CRC first with
 * [isCrcValid] if your flow requires it.
 *
 * ```kotlin
 * if (EmvQr.isCrcValid(rawText)) {
 *     val data = EmvQr.decode(rawText)
 * }
 * ```
 */
public object EmvQr {

    /** Decodes [rawText] into the typed sections of [QRCodeEmvCoColombiaData]. */
    public fun decode(rawText: String): QRCodeEmvCoColombiaData =
        EmvQrCodeDecoder(rawText).decode()

    /**
     * Decodes [rawText] and additionally reports [ParseDiagnostics] describing
     * how much of the payload the lenient parser consumed — useful for logging
     * and support when a QR in the field deviates from the standard.
     */
    public fun decodeWithDiagnostics(rawText: String): EmvQrDecodeResult {
        val decoder = EmvQrCodeDecoder(rawText)
        return EmvQrDecodeResult(
            data = decoder.decode(),
            diagnostics = decoder.diagnostics()
        )
    }

    /**
     * Returns `true` when the last 4 characters of [rawText] match the
     * CRC-16/CCITT-FALSE checksum (tag `63`) of everything before them,
     * computed over the UTF-8 bytes per ISO/IEC 13239.
     *
     * A mismatch means the scanned text is not what was encoded (corrupted
     * read or tampering), not merely a deviation from the standard.
     */
    public fun isCrcValid(rawText: String): Boolean = CRCValidator.validate(rawText)
}

/**
 * Result of [EmvQr.decodeWithDiagnostics]: the decoded [data] plus the
 * [diagnostics] of the lenient parse.
 */
@ConsistentCopyVisibility
public data class EmvQrDecodeResult internal constructor(
    public val data: QRCodeEmvCoColombiaData,
    public val diagnostics: ParseDiagnostics,
)

/**
 * Observability of the lenient top-level TLV parse — *information, not
 * enforcement*: a partially-consumed payload still decodes whatever was
 * well-formed.
 *
 * @property parsedTagCount Number of top-level TLV elements successfully read.
 * @property consumedChars Characters of the payload consumed by the parser.
 * @property totalChars Total length of the raw payload.
 */
@ConsistentCopyVisibility
public data class ParseDiagnostics internal constructor(
    public val parsedTagCount: Int,
    public val consumedChars: Int,
    public val totalChars: Int,
) {
    /** `true` when the whole payload was well-formed TLV and fully consumed. */
    public val isFullyParsed: Boolean
        get() = consumedChars == totalChars
}
