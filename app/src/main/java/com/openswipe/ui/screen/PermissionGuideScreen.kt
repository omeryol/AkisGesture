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
import com.openswipe.ui.theme.StatusConnected
import com.openswipe.ui.theme.StatusDisconnected
import com.openswipe.util.PermissionHelper

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

    // 从系统设置返回时刷新权限状态
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
            text = "权限设置",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "OpenSwipe 需要以下权限才能正常工作",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 步骤 1：无障碍服务（必需）
        PermissionCard(
            step = 1,
            title = "无障碍服务",
            description = "用于检测触摸手势和执行导航操作。\n请在设置中找到 OpenSwipe 并启用。",
            isGranted = isAccessibilityEnabled,
            required = true,
            onRequest = { PermissionHelper.openAccessibilitySettings(context) },
        )

        // 步骤 2：电池优化白名单（推荐）
        PermissionCard(
            step = 2,
            title = "忽略电池优化",
            description = "防止系统在后台杀死手势服务，保持手势持续可用。",
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
                text = if (isAccessibilityEnabled) "开始使用" else "请先启用无障碍服务",
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
                            text = "步骤 $step: $title",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (required) {
                            Text(
                                text = "必需",
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
                        Text("前往设置")
                    }
                } else {
                    OutlinedButton(
                        onClick = onRequest,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("前往设置")
                    }
                }
            }
        }
    }
}
