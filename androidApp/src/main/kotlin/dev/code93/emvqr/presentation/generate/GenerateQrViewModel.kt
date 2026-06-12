package dev.code93.emvqr.presentation.generate

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.code93.emvqr.core.util.QrBitmapEncoder
import dev.code93.emvqr.domain.model.PaymentKeyType
import dev.code93.emvqr.domain.model.PaymentNetwork
import dev.code93.emvqr.domain.model.QrGenerationParams
import dev.code93.emvqr.domain.usecase.GenerateQrUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GenerateQrUiState(
    val network: PaymentNetwork = PaymentNetwork.REDEBAN,
    val keyType: PaymentKeyType? = PaymentKeyType.ALPHANUMERIC,
    val keyValue: String = "",
    val merchantCode: String = "",
    val merchantName: String = "",
    val merchantCity: String = "",
    val amount: String = "",
    val nameError: GenerateFieldError? = null,
    val cityError: GenerateFieldError? = null,
    val keyOrCodeError: Boolean = false,
    val amountError: Boolean = false,
    val generatedRawText: String? = null,
    val generatedBitmap: Bitmap? = null
)

enum class GenerateFieldError { REQUIRED, TOO_LONG }

@HiltViewModel
class GenerateQrViewModel @Inject constructor(
    private val generateQr: GenerateQrUseCase,
    private val bitmapEncoder: QrBitmapEncoder
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerateQrUiState())
    val uiState: StateFlow<GenerateQrUiState> = _uiState.asStateFlow()

    fun onNetworkChanged(network: PaymentNetwork) = _uiState.update { it.copy(network = network) }

    fun onKeyTypeChanged(keyType: PaymentKeyType?) =
        _uiState.update { it.copy(keyType = keyType, keyOrCodeError = false) }

    fun onKeyValueChanged(value: String) =
        _uiState.update { it.copy(keyValue = value, keyOrCodeError = false) }

    fun onMerchantCodeChanged(value: String) =
        _uiState.update { it.copy(merchantCode = value, keyOrCodeError = false) }

    fun onMerchantNameChanged(value: String) =
        _uiState.update { it.copy(merchantName = value, nameError = null) }

    fun onMerchantCityChanged(value: String) =
        _uiState.update { it.copy(merchantCity = value, cityError = null) }

    fun onAmountChanged(value: String) =
        _uiState.update { it.copy(amount = value, amountError = false) }

    fun onGenerate() {
        val state = _uiState.value
        if (!validate(state)) return

        val params = QrGenerationParams(
            network = state.network,
            keyType = state.keyType.takeIf { state.keyValue.isNotBlank() },
            keyValue = state.keyValue.takeIf { it.isNotBlank() },
            merchantCode = state.merchantCode.takeIf { it.isNotBlank() },
            merchantName = state.merchantName,
            merchantCity = state.merchantCity,
            amount = state.amount.takeIf { it.isNotBlank() }
        )
        val rawText = generateQr(params)
        _uiState.update {
            it.copy(
                generatedRawText = rawText,
                generatedBitmap = bitmapEncoder.encode(rawText)
            )
        }
    }

    private fun validate(state: GenerateQrUiState): Boolean {
        val nameError = when {
            state.merchantName.isBlank() -> GenerateFieldError.REQUIRED
            state.merchantName.trim().length > 25 -> GenerateFieldError.TOO_LONG
            else -> null
        }
        val cityError = when {
            state.merchantCity.isBlank() -> GenerateFieldError.REQUIRED
            state.merchantCity.trim().length > 15 -> GenerateFieldError.TOO_LONG
            else -> null
        }
        val hasKey = state.keyType != null && state.keyValue.isNotBlank()
        val keyOrCodeError = !hasKey && state.merchantCode.isBlank()
        val amountError = state.amount.isNotBlank() &&
            !state.amount.trim().matches(Regex("\\d{1,10}(\\.\\d{1,2})?"))

        _uiState.update {
            it.copy(
                nameError = nameError,
                cityError = cityError,
                keyOrCodeError = keyOrCodeError,
                amountError = amountError
            )
        }
        return nameError == null && cityError == null && !keyOrCodeError && !amountError
    }
}
