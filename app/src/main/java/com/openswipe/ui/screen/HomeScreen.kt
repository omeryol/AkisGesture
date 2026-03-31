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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openswipe.service.GestureAccessibilityService
import com.openswipe.ui.theme.StatusConnected
import com.openswipe.ui.theme.StatusDisconnected

@Composable
fun HomeScreen(
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val serviceState by GestureAccessibilityService.serviceState.collectAsState()
    val isConnected = serviceState == GestureAccessibilityService.ServiceState.CONNECTED

    var gestureEnabled by remember { mutableStateOf(true) }
    var leftSensitivity by remember { mutableFloatStateOf(0.5f) }
    var rightSensitivity by remember { mutableFloatStateOf(0.5f) }
    var bottomGestureEnabled by remember { mutableStateOf(true) }

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

        // 手势总开关
        SettingSwitch(
            title = "启用手势",
            subtitle = "启用或禁用所有手势操作",
            checked = gestureEnabled,
            onCheckedChange = { gestureEnabled = it },
        )

        // 左边缘灵敏度
        SettingSlider(
            title = "左边缘灵敏度",
            value = leftSensitivity,
            onValueChange = { leftSensitivity = it },
        )

        // 右边缘灵敏度
        SettingSlider(
            title = "右边缘灵敏度",
            value = rightSensitivity,
            onValueChange = { rightSensitivity = it },
        )

        // 底部手势开关
        SettingSwitch(
            title = "底部手势",
            subtitle = "从底部上滑回到主页",
            checked = bottomGestureEnabled,
            onCheckedChange = { bottomGestureEnabled = it },
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

@Composable
private fun SettingSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${(value * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..1f,
            )
        }
    }
}
