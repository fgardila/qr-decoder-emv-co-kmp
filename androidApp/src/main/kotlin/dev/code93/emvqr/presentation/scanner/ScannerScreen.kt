package dev.code93.emvqr.presentation.scanner

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.code93.emvqr.R
import dev.code93.emvqr.core.designsystem.components.EmptyState
import dev.code93.emvqr.presentation.scanner.components.DecodedResult
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    scannedQrFlow: StateFlow<String?>,
    onConsumeScannedQr: () -> Unit,
    onOpenCamera: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scannedQr by scannedQrFlow.collectAsState()

    LaunchedEffect(scannedQr) {
        scannedQr?.let { raw ->
            viewModel.onQrScanned(raw)
            onConsumeScannedQr()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(viewModel::onImageSelected)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.scanner_title)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onOpenCamera,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.scanner_open_camera))
                }
                OutlinedButton(
                    onClick = {
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.scanner_pick_image))
                }
            }

            when (val state = uiState) {
                is ScannerUiState.Idle -> {
                    Text(
                        text = stringResource(R.string.scanner_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    EmptyState(
                        icon = Icons.Filled.QrCodeScanner,
                        message = stringResource(R.string.scanner_empty_hint),
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }

                is ScannerUiState.Decoding -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.scanner_decoding))
                    }
                }

                is ScannerUiState.Error -> {
                    EmptyState(
                        icon = Icons.Filled.QrCodeScanner,
                        message = stringResource(
                            if (state.noQrFound) R.string.scanner_error_no_qr
                            else R.string.scanner_error_generic
                        ),
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }

                is ScannerUiState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            DecodedResult(
                                decoded = state.decoded,
                                onScanAgain = viewModel::onReset
                            )
                        }
                        items(state.decoded.sections) { section ->
                            dev.code93.emvqr.presentation.scanner.components.SectionContent(section)
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}
