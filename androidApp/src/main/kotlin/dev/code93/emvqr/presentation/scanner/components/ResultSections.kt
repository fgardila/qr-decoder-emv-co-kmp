package dev.code93.emvqr.presentation.scanner.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.code93.emvqr.R
import dev.code93.emvqr.core.designsystem.components.KeyValueRow
import dev.code93.emvqr.core.designsystem.components.SectionCard
import dev.code93.emvqr.domain.model.DecodedQr
import dev.code93.emvqr.domain.model.EmvSection

/** Cabecera del resultado: integridad CRC + diagnósticos del parsing. */
@Composable
fun DecodedResult(
    decoded: DecodedQr,
    onScanAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        stringResource(
                            if (decoded.crcValid) R.string.scanner_crc_valid
                            else R.string.scanner_crc_invalid
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (decoded.crcValid) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (decoded.crcValid) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            )
            AssistChip(
                onClick = {},
                label = {
                    val diagnostics = decoded.diagnostics
                    val parseLabel = if (diagnostics.isFullyParsed) {
                        stringResource(R.string.scanner_parse_complete)
                    } else {
                        stringResource(
                            R.string.scanner_parse_partial,
                            diagnostics.consumedChars,
                            diagnostics.totalChars
                        )
                    }
                    Text(
                        stringResource(
                            R.string.scanner_diagnostics,
                            diagnostics.parsedTagCount,
                            parseLabel
                        )
                    )
                }
            )
        }
        TextButton(onClick = onScanAgain) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Text(
                text = stringResource(R.string.scanner_scan_again),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun SectionContent(section: EmvSection, modifier: Modifier = Modifier) {
    SectionCard(title = section.title, modifier = modifier) {
        section.fields.forEachIndexed { index, field ->
            KeyValueRow(
                label = field.label,
                value = field.value,
                monospace = field.label == "CRC" || field.label.startsWith("GUID")
            )
            if (index < section.fields.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}
