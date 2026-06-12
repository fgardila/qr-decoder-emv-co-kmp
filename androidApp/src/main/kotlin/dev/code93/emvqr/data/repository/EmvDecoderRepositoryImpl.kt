package dev.code93.emvqr.data.repository

import dev.code93.emvqr.data.mapper.EmvDataMapper
import dev.code93.emvqr.domain.model.DecodeDiagnostics
import dev.code93.emvqr.domain.model.DecodedQr
import dev.code93.emvqr.domain.repository.EmvDecoderRepository
import dev.code93.kmp.qrd.EmvQr
import javax.inject.Inject

class EmvDecoderRepositoryImpl @Inject constructor(
    private val mapper: EmvDataMapper
) : EmvDecoderRepository {

    override fun decode(rawText: String): DecodedQr {
        val result = EmvQr.decodeWithDiagnostics(rawText)
        return DecodedQr(
            rawText = rawText,
            crcValid = EmvQr.isCrcValid(rawText),
            sections = mapper.toSections(result.data),
            diagnostics = DecodeDiagnostics(
                parsedTagCount = result.diagnostics.parsedTagCount,
                consumedChars = result.diagnostics.consumedChars,
                totalChars = result.diagnostics.totalChars,
                isFullyParsed = result.diagnostics.isFullyParsed
            )
        )
    }
}
