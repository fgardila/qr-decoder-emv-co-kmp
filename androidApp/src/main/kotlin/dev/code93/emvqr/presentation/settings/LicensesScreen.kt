package dev.code93.emvqr.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import dev.code93.emvqr.R

private data class LicenseEntry(
    val name: String,
    val license: String,
    val url: String
)

private val licenses = listOf(
    LicenseEntry(
        "emvdecoder (dev.code93)",
        "MIT License",
        "https://github.com/fgardila/qr-decoder-emv-co-kmp/blob/main/LICENSE"
    ),
    LicenseEntry(
        "Kotlin y librerías kotlinx",
        "Apache License 2.0",
        "https://github.com/JetBrains/kotlin"
    ),
    LicenseEntry(
        "AndroidX / Jetpack Compose",
        "Apache License 2.0",
        "https://developer.android.com/jetpack"
    ),
    LicenseEntry(
        "AndroidX CameraX",
        "Apache License 2.0",
        "https://developer.android.com/training/camerax"
    ),
    LicenseEntry(
        "Dagger / Hilt",
        "Apache License 2.0",
        "https://github.com/google/dagger"
    ),
    LicenseEntry(
        "ZXing (core)",
        "Apache License 2.0",
        "https://github.com/zxing/zxing"
    ),
    LicenseEntry(
        "ML Kit Barcode Scanning",
        "Términos de los servicios de Google Play",
        "https://developers.google.com/ml-kit/terms"
    ),
    LicenseEntry(
        "Material Design Components",
        "Apache License 2.0",
        "https://github.com/material-components"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_licenses)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(licenses) { entry ->
                ListItem(
                    headlineContent = { Text(entry.name) },
                    supportingContent = { Text(entry.license) },
                    modifier = Modifier.clickable { uriHandler.openUri(entry.url) }
                )
                HorizontalDivider()
            }
        }
    }
}
