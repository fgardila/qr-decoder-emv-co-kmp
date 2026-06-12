package dev.code93.emvqr.domain.usecase

import android.net.Uri
import dev.code93.emvqr.domain.generator.EaspbvQrBuilder
import dev.code93.emvqr.domain.model.DecodedQr
import dev.code93.emvqr.domain.model.QrGenerationParams
import dev.code93.emvqr.domain.repository.EmvDecoderRepository
import dev.code93.emvqr.domain.repository.QrImageRepository
import javax.inject.Inject

class DecodeQrUseCase @Inject constructor(
    private val repository: EmvDecoderRepository
) {
    operator fun invoke(rawText: String): DecodedQr = repository.decode(rawText)
}

class DecodeQrFromImageUseCase @Inject constructor(
    private val imageRepository: QrImageRepository,
    private val decodeQr: DecodeQrUseCase
) {
    /** @return null si la imagen no contiene un QR. */
    suspend operator fun invoke(uri: Uri): DecodedQr? =
        imageRepository.decodeFromImage(uri)?.let(decodeQr::invoke)
}

class GenerateQrUseCase @Inject constructor() {
    operator fun invoke(params: QrGenerationParams): String = EaspbvQrBuilder.build(params)
}
