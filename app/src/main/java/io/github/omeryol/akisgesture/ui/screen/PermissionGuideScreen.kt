package io.github.omeryol.akisgesture.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import io.github.omeryol.akisgesture.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.omeryol.akisgesture.ui.theme.StatusConnected
import io.github.omeryol.akisgesture.ui.theme.StatusDisconnected
import io.github.omeryol.akisgesture.util.PermissionHelper
import io.github.omeryol.akisgesture.ui.component.AkisGlassCard

import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun PermissionGuideScreen(
    onAllGranted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    var isAccessibilityEnabled by remember {
        mutableStateOf(PermissionHelper.isAccessibilityServiceEnabled(context))
    }
    var isBatteryOptimized by remember {
        mutableStateOf(PermissionHelper.isBatteryOptimizationIgnored(context))
    }
    var canWriteSettings by remember { mutableStateOf(PermissionHelper.canWriteSystemSettings(context)) }

    // Ayarlardan geri dönüldüğünde izin durumunu yenile
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isAccessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(context)
        isBatteryOptimized = PermissionHelper.isBatteryOptimizationIgnored(context)
        canWriteSettings = PermissionHelper.canWriteSystemSettings(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(R.string.permissions_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.permissions_intro),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Adım 1：Erişilebilirlik hizmeti（Gerekli）
        PermissionCard(
            step = 1,
            title = stringResource(R.string.accessibility_service),
            description = stringResource(R.string.accessibility_description),
            isGranted = isAccessibilityEnabled,
            required = true,
            onRequest = { PermissionHelper.openAccessibilitySettings(context) },
        )

        // Adım 2: Pil kısıtlamasını kaldır
        PermissionCard(
            step = 2,
            title = stringResource(R.string.battery_permission_title),
            description = stringResource(R.string.battery_permission_description),
            isGranted = isBatteryOptimized,
            required = false,
            onRequest = { PermissionHelper.requestIgnoreBatteryOptimization(context) },
        )

        PermissionCard(
            step = 3,
            title = stringResource(R.string.write_settings_permission_title),
            description = stringResource(R.string.write_settings_permission_description),
            isGranted = canWriteSettings,
            required = false,
            onRequest = { PermissionHelper.openWriteSettings(context) },
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onAllGranted,
            enabled = isAccessibilityEnabled,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (isAccessibilityEnabled) stringResource(R.string.start_using) else stringResource(R.string.enable_accessibility_first),
            )
        }
    }
}

@Composable
private fun PermissionCard(
    step: Int,
    title: String,
    description: String,
    isGranted: Boolean,
    required: Boolean,
    onRequest: () -> Unit,
) {
    AkisGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentTint = if (isGranted) StatusConnected else MaterialTheme.colorScheme.primary,
        containerColor = if (isGranted) StatusConnected.copy(alpha = 0.08f) else null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Filled.Check else Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = if (isGranted) StatusConnected else StatusDisconnected,
                    modifier = Modifier.size(24.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.step_title, step, title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (required) {
                            Text(
                                text = stringResource(R.string.required),
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusDisconnected,
                            )
                        }
                    }
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!isGranted) {
                if (required) {
                    Button(
                        onClick = onRequest,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.open_settings))
                    }
                } else {
                    OutlinedButton(
                        onClick = onRequest,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.open_settings))
                    }
                }
            }
        }
    }
}
