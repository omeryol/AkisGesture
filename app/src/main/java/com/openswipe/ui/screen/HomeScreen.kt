package com.omer.akisgesture.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.omer.akisgesture.AkisGestureApp
import com.omer.akisgesture.service.GestureAccessibilityService
import com.omer.akisgesture.ui.theme.StatusConnected
import com.omer.akisgesture.ui.theme.StatusDisconnected
import com.omer.akisgesture.ui.util.edgeLabel
import com.omer.akisgesture.ui.viewmodel.HomeViewModel
import com.omer.akisgesture.util.PermissionHelper

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val serviceState by GestureAccessibilityService.serviceState.collectAsState()
    val isConnected = serviceState == GestureAccessibilityService.ServiceState.CONNECTED
    val ruleSet by AkisGestureApp.getInstance().compiledRuleSet.collectAsState()
    val context = LocalContext.current
    val batteryOptimized = !PermissionHelper.isBatteryOptimizationIgnored(context)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 服务状态卡片
        ServiceStatusCard(
            isConnected = isConnected,
            onSetupClick = onNavigateToPermissions,
        )

        // 电池优化警告
        if (batteryOptimized) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pil kısıtlamasını kapatın",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        text = "Pil kısıtlaması hareketlerin arka planda durmasına neden olabilir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        PermissionHelper.requestIgnoreBatteryOptimization(context)
                    }) {
                        Text("Ayarlara git")
                    }
                }
            }
        }

        // Kurallar摘要卡片
        RuleSummaryCard(ruleSet = ruleSet)
    }
}

@Composable
private fun ServiceStatusCard(
    isConnected: Boolean,
    onSetupClick: () -> Unit,
) {
    Card(
        onClick = { if (!isConnected) onSetupClick() },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) {
                StatusConnected.copy(alpha = 0.12f)
            } else {
                StatusDisconnected.copy(alpha = 0.12f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = if (isConnected) StatusConnected else StatusDisconnected,
                modifier = Modifier.size(32.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isConnected) "Hareketler hazır" else "Hareketler kapalı",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (isConnected) "Sol, sağ ve alt kenar etkin" else "Kurulumu tamamlamak için dokunun",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RuleSummaryCard(ruleSet: com.omer.akisgesture.rule.CompiledRuleSet) {
    val edges = com.omer.akisgesture.overlay.Edge.entries
    val activeEdges = edges.filter { ruleSet.hasRulesFor(it) }
    val totalRules = ruleSet.totalRuleCount()
    val edgeDetails = activeEdges.joinToString(", ") { edge ->
        "${edgeLabel(edge)} (${ruleSet.ruleCountFor(edge)})"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = if (totalRules > 0) "${totalRules}  hareket kuralı etkin"
                       else "Etkin kural yok",
                style = MaterialTheme.typography.titleMedium,
            )
            if (activeEdges.isNotEmpty()) {
                Text(
                    text = "Etkin kenarlar: $edgeDetails",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "Hareketleri değiştirmek için Kurallar bölümünü açın.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
