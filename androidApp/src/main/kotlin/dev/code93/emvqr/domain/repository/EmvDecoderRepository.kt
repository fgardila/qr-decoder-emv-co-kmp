package dev.code93.emvqr.domain.repository

import dev.code93.emvqr.domain.model.DecodedQr

interface EmvDecoderRepository {
    fun decode(rawText: String): DecodedQr
}
