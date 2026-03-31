package com.openswipe.ui.screen

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openswipe.OpenSwipeApp
import com.openswipe.service.GestureAccessibilityService
import com.openswipe.ui.theme.StatusConnected
import com.openswipe.ui.theme.StatusDisconnected
import com.openswipe.ui.util.edgeLabel
import com.openswipe.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val serviceState by GestureAccessibilityService.serviceState.collectAsState()
    val isConnected = serviceState == GestureAccessibilityService.ServiceState.CONNECTED
    val ruleSet by OpenSwipeApp.getInstance().compiledRuleSet.collectAsState()

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

        Spacer(modifier = Modifier.height(8.dp))

        // 规则摘要卡片
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
                    text = if (isConnected) "手势服务已连接" else "手势服务未连接",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (isConnected) "手势功能正常运行中" else "点击进行权限设置",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RuleSummaryCard(ruleSet: com.openswipe.rule.CompiledRuleSet) {
    val edges = com.openswipe.overlay.Edge.entries
    val activeEdges = edges.filter { ruleSet.hasRulesFor(it) }
    val totalRules = ruleSet.totalRuleCount()
    val edgeDetails = activeEdges.joinToString("、") { edge ->
        "${edgeLabel(edge)}(${ruleSet.ruleCountFor(edge)})"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = if (totalRules > 0) "${totalRules} 条手势规则生效中"
                       else "无生效规则",
                style = MaterialTheme.typography.titleMedium,
            )
            if (activeEdges.isNotEmpty()) {
                Text(
                    text = "活跃边缘：$edgeDetails",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "前往「规则配置」编辑手势规则",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
