package dev.code93.emvqr.presentation.scanner

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.code93.emvqr.domain.usecase.DecodeQrFromImageUseCase
import dev.code93.emvqr.domain.usecase.DecodeQrUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val decodeQr: DecodeQrUseCase,
    private val decodeQrFromImage: DecodeQrFromImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onQrScanned(rawText: String) {
        _uiState.value = ScannerUiState.Success(decodeQr(rawText))
    }

    fun onImageSelected(uri: Uri) {
        _uiState.value = ScannerUiState.Decoding
        viewModelScope.launch {
            _uiState.value = try {
                decodeQrFromImage(uri)
                    ?.let { ScannerUiState.Success(it) }
                    ?: ScannerUiState.Error(noQrFound = true)
            } catch (_: Exception) {
                ScannerUiState.Error(noQrFound = false)
            }
        }
    }

    fun onReset() {
        _uiState.value = ScannerUiState.Idle
    }
}
