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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.omeryol.akisgesture.ui.theme.StatusConnected
import io.github.omeryol.akisgesture.ui.theme.StatusDisconnected
import io.github.omeryol.akisgesture.util.PermissionHelper

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

    // Ayarlardan geri dönüldüğünde izin durumunu yenile
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isAccessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(context)
        isBatteryOptimized = PermissionHelper.isBatteryOptimizationIgnored(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "İzinler",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Akış Gesture düzgün çalışmak için aşağıdaki izinlere ihtiyaç duyar.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Adım 1：Erişilebilirlik hizmeti（Gerekli）
        PermissionCard(
            step = 1,
            title = "Erişilebilirlik hizmeti",
            description = "Kenar hareketlerini algılar ve seçilen işlemleri uygular.\nAyarlar içinde Akış Gesture hizmetini etkinleştirin.",
            isGranted = isAccessibilityEnabled,
            required = true,
            onRequest = { PermissionHelper.openAccessibilitySettings(context) },
        )

        // Adım 2: Pil kısıtlamasını kaldır
        PermissionCard(
            step = 2,
            title = "Pil kısıtlamasını kaldır",
            description = "HyperOS sisteminin hareket hizmetini arka planda durdurmasını önler.",
            isGranted = isBatteryOptimized,
            required = false,
            onRequest = { PermissionHelper.requestIgnoreBatteryOptimization(context) },
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onAllGranted,
            enabled = isAccessibilityEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (isAccessibilityEnabled) "Kullanmaya başla" else "Önce erişilebilirlik hizmetini açın",
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                StatusConnected.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
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
                            text = "Adım $step: $title",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (required) {
                            Text(
                                text = "Gerekli",
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
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Ayarları aç")
                    }
                } else {
                    OutlinedButton(
                        onClick = onRequest,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Ayarları aç")
                    }
                }
            }
        }
    }
}
