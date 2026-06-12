package dev.code93.emvqr.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.code93.emvqr.BuildConfig
import dev.code93.emvqr.R

private const val URL_WEBSITE = "https://www.code93.dev"
private const val URL_GITHUB = "https://github.com/fgardila/qr-decoder-emv-co-kmp"
private const val URL_DOCS = "https://fgardila.github.io/qr-decoder-emv-co-kmp/"
private const val URL_LIBRARY = "https://central.sonatype.com/artifact/dev.code93/emvdecoder"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenLicenses: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader(stringResource(R.string.settings_developer_section))
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_developer_name)) },
                supportingContent = { Text(stringResource(R.string.settings_developer_role)) },
                leadingContent = { Icon(Icons.Filled.Person, contentDescription = null) }
            )
            LinkItem(
                icon = Icons.Filled.Language,
                label = stringResource(R.string.settings_website),
                onClick = { uriHandler.openUri(URL_WEBSITE) }
            )

            HorizontalDivider()
            SectionHeader(stringResource(R.string.settings_links_section))
            LinkItem(
                icon = Icons.Filled.Code,
                label = stringResource(R.string.settings_github),
                onClick = { uriHandler.openUri(URL_GITHUB) }
            )
            LinkItem(
                icon = Icons.Filled.Description,
                label = stringResource(R.string.settings_docs),
                onClick = { uriHandler.openUri(URL_DOCS) }
            )
            LinkItem(
                icon = Icons.Filled.Widgets,
                label = stringResource(R.string.settings_library),
                onClick = { uriHandler.openUri(URL_LIBRARY) }
            )

            HorizontalDivider()
            SectionHeader(stringResource(R.string.settings_about_section))
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_licenses)) },
                leadingContent = { Icon(Icons.Filled.Policy, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onOpenLicenses)
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_version)) },
                supportingContent = { Text(BuildConfig.VERSION_NAME) },
                leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun LinkItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
