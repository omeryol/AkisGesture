package com.omer.akisgesture.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omer.akisgesture.AkisGestureApp
import com.omer.akisgesture.service.GestureAccessibilityService
import com.omer.akisgesture.ui.theme.StatusConnected
import com.omer.akisgesture.ui.theme.StatusDisconnected
import com.omer.akisgesture.util.PermissionHelper

@Composable
fun HomeScreen(
    viewModel: com.omer.akisgesture.ui.viewmodel.HomeViewModel,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val serviceState by GestureAccessibilityService.serviceState.collectAsState()
    val ruleSet by AkisGestureApp.getInstance().compiledRuleSet.collectAsState()
    val context = LocalContext.current
    val isConnected = serviceState == GestureAccessibilityService.ServiceState.CONNECTED
    val batteryOptimized = !PermissionHelper.isBatteryOptimizationIgnored(context)
    val activeEdges = com.omer.akisgesture.overlay.Edge.entries.count(ruleSet::hasRulesFor)
    val totalRules = ruleSet.totalRuleCount()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        StatusHero(
            isConnected = isConnected,
            onClick = { if (!isConnected) onNavigateToPermissions() },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                value = totalRules.toString(),
                label = "Etkin hareket",
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                value = "$activeEdges/3",
                label = "Etkin kenar",
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                value = if (isConnected) "Açık" else "Kapalı",
                label = "Koruma",
                modifier = Modifier.weight(1f),
            )
        }

        if (batteryOptimized) {
            BatteryNotice {
                PermissionHelper.requestIgnoreBatteryOptimization(context)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text("Hareketlerini kişiselleştir", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Alt menüdeki Hareketler bölümünden kısa ve bekletmeli işlemleri birlikte düzenleyebilirsin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusHero(
    isConnected: Boolean,
    onClick: () -> Unit,
) {
    val accent = if (isConnected) StatusConnected else StatusDisconnected
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ),
                )
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(accent.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isConnected) Icons.Filled.Check else Icons.Filled.Security,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isConnected) "Akış hazır" else "Kurulum gerekiyor",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    if (isConnected) {
                        "Kenar hareketleri etkin ve arka planda korunuyor"
                    } else {
                        "Hareketleri açmak için dokun"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BatteryNotice(onOpenSettings: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Filled.BatteryAlert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Pil korumasını kapat", fontWeight = FontWeight.SemiBold)
                Text(
                    "HyperOS hareket hizmetini durdurabilir.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OutlinedButton(onClick = onOpenSettings) {
                Text("Aç")
            }
        }
    }
}
