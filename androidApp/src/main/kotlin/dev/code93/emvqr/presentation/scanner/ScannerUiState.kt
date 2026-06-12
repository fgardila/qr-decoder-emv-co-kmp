package dev.code93.emvqr.presentation.scanner

import dev.code93.emvqr.domain.model.DecodedQr

sealed interface ScannerUiState {
    data object Idle : ScannerUiState
    data object Decoding : ScannerUiState
    data class Success(val decoded: DecodedQr) : ScannerUiState
    data class Error(val noQrFound: Boolean) : ScannerUiState
}
