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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openswipe.service.GestureAccessibilityService
import com.openswipe.ui.theme.StatusConnected
import com.openswipe.ui.theme.StatusDisconnected
import com.openswipe.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val serviceState by GestureAccessibilityService.serviceState.collectAsState()
    val isConnected = serviceState == GestureAccessibilityService.ServiceState.CONNECTED
    val config by viewModel.configState.collectAsState()

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

        // 左边缘手势开关
        SettingSwitch(
            title = "左边缘手势",
            subtitle = "从左边缘向右滑动触发返回",
            checked = config.leftEnabled,
            onCheckedChange = { viewModel.setLeftEnabled(it) },
        )

        // 右边缘手势开关
        SettingSwitch(
            title = "右边缘手势",
            subtitle = "从右边缘向左滑动触发返回",
            checked = config.rightEnabled,
            onCheckedChange = { viewModel.setRightEnabled(it) },
        )

        // 底部手势开关
        SettingSwitch(
            title = "底部手势",
            subtitle = "从底部上滑回到主页/最近任务",
            checked = config.bottomEnabled,
            onCheckedChange = { viewModel.setBottomEnabled(it) },
        )
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
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
