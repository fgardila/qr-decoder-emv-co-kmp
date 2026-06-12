package dev.code93.emvqr.presentation.generate

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import dev.code93.emvqr.R
import dev.code93.emvqr.domain.model.PaymentKeyType
import dev.code93.emvqr.domain.model.PaymentNetwork
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateQrScreen(
    viewModel: GenerateQrViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.generate_title)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.generate_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Red adquirente
            Text(
                text = stringResource(R.string.generate_network),
                style = MaterialTheme.typography.labelLarge
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentNetwork.entries.forEach { network ->
                    FilterChip(
                        selected = state.network == network,
                        onClick = { viewModel.onNetworkChanged(network) },
                        label = { Text(network.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Tipo de llave
            Text(
                text = stringResource(R.string.generate_key_type),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                keyTypeOptions.forEach { (keyType, labelRes) ->
                    FilterChip(
                        selected = state.keyType == keyType,
                        onClick = { viewModel.onKeyTypeChanged(keyType) },
                        label = { Text(stringResource(labelRes)) }
                    )
                }
            }

            if (state.keyType != null) {
                OutlinedTextField(
                    value = state.keyValue,
                    onValueChange = viewModel::onKeyValueChanged,
                    label = { Text(stringResource(R.string.generate_key_value)) },
                    isError = state.keyOrCodeError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = state.merchantCode,
                    onValueChange = viewModel::onMerchantCodeChanged,
                    label = { Text(stringResource(R.string.generate_merchant_code)) },
                    isError = state.keyOrCodeError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (state.keyOrCodeError) {
                ErrorText(stringResource(R.string.generate_error_key_or_code))
            }

            OutlinedTextField(
                value = state.merchantName,
                onValueChange = viewModel::onMerchantNameChanged,
                label = { Text(stringResource(R.string.generate_merchant_name)) },
                isError = state.nameError != null,
                supportingText = state.nameError?.let {
                    {
                        Text(
                            stringResource(
                                if (it == GenerateFieldError.REQUIRED) R.string.generate_error_name_required
                                else R.string.generate_error_name_long
                            )
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.merchantCity,
                onValueChange = viewModel::onMerchantCityChanged,
                label = { Text(stringResource(R.string.generate_city)) },
                isError = state.cityError != null,
                supportingText = state.cityError?.let {
                    {
                        Text(
                            stringResource(
                                if (it == GenerateFieldError.REQUIRED) R.string.generate_error_city_required
                                else R.string.generate_error_city_long
                            )
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.amount,
                onValueChange = viewModel::onAmountChanged,
                label = { Text(stringResource(R.string.generate_amount)) },
                isError = state.amountError,
                supportingText = if (state.amountError) {
                    { Text(stringResource(R.string.generate_error_amount_invalid)) }
                } else {
                    null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::onGenerate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.QrCode, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.generate_button))
            }

            state.generatedBitmap?.let { bitmap ->
                GeneratedQrResult(
                    bitmap = bitmap,
                    onShare = { shareQr(context, bitmap) },
                    onCopy = {
                        state.generatedRawText?.let { clipboard.setText(AnnotatedString(it)) }
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private val keyTypeOptions = listOf(
    PaymentKeyType.ALPHANUMERIC to R.string.generate_key_alphanumeric,
    PaymentKeyType.PHONE to R.string.generate_key_phone,
    null to R.string.generate_key_none
)

@Composable
private fun GeneratedQrResult(
    bitmap: Bitmap,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp)
        )
        Text(
            text = stringResource(R.string.generate_result_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onShare, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.generate_share))
            }
            OutlinedButton(onClick = onCopy, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.generate_copy_raw))
            }
        }
    }
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )
}

private fun shareQr(context: android.content.Context, bitmap: Bitmap) {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(imagesDir, "qr-emv-colombia.png")
    file.outputStream().use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    }
    val uri = FileProvider.getUriForFile(context, "dev.code93.emvqr.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_qr_subject))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
