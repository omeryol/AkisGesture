package io.github.omeryol.akisgesture.diagnostics

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.R
import io.github.omeryol.akisgesture.ui.component.AkisGlassCard

@Composable
fun DiagnosticsSettingsEntry() {
    val context = LocalContext.current
    var refreshToken by remember { mutableIntStateOf(0) }
    val exportReport = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { RuntimeDiagnostics.export(context, it) }
                ?: error("Output stream could not be opened")
        }.onSuccess {
            Toast.makeText(context, context.getString(R.string.diagnostic_exported), Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, context.getString(R.string.diagnostic_export_failed), Toast.LENGTH_LONG).show()
        }
    }

    val recording = RuntimeDiagnostics.isRecording
    val count = RuntimeDiagnostics.eventCount() + refreshToken * 0
    AkisGlassCard(accentTint = if (recording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary) {
        Text(
            text = stringResource(R.string.diagnostic_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.diagnostic_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (recording) {
                stringResource(R.string.diagnostic_recording, count)
            } else {
                stringResource(R.string.diagnostic_idle, count)
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (recording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
        )
        val disconnectSummary = remember(count, refreshToken) {
            RuntimeDiagnostics.getLastDisconnectSummary(context)
        }
        if (!disconnectSummary.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.diagnostic_last_disconnect, disconnectSummary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = {
                RuntimeDiagnostics.clear()
                refreshToken++
            }) {
                Text(stringResource(R.string.diagnostic_clear))
            }
            Spacer(Modifier.width(8.dp))
            if (recording) {
                OutlinedButton(onClick = {
                    RuntimeDiagnostics.stopSession()
                    refreshToken++
                }) {
                    Text(stringResource(R.string.diagnostic_stop))
                }
            } else {
                Button(onClick = {
                    RuntimeDiagnostics.startSession()
                    refreshToken++
                }) {
                    Text(stringResource(R.string.diagnostic_start))
                }
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = {
                    exportReport.launch("akis-gesture-diagnostic-report.json")
                    refreshToken++
                },
                enabled = count > 0,
            ) {
                Text(stringResource(R.string.diagnostic_export))
            }
        }
    }
}
