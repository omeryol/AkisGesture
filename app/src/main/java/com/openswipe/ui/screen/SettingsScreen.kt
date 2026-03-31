package com.openswipe.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.openswipe.gesture.BottomTriggerMode
import com.openswipe.ui.viewmodel.HomeViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val config by viewModel.configState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "手势区域",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        SettingsSwitch(
            title = "左边缘手势",
            subtitle = "从左边缘向右滑动触发返回",
            checked = config.leftEnabled,
            onCheckedChange = { viewModel.setLeftEnabled(it) },
        )

        SettingsSwitch(
            title = "右边缘手势",
            subtitle = "从右边缘向左滑动触发返回",
            checked = config.rightEnabled,
            onCheckedChange = { viewModel.setRightEnabled(it) },
        )

        SettingsSwitch(
            title = "底部手势",
            subtitle = "从底部上滑回到主页/最近任务",
            checked = config.bottomEnabled,
            onCheckedChange = { viewModel.setBottomEnabled(it) },
        )

        AnimatedVisibility(visible = config.bottomEnabled) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Height slider
                    Text(text = "触发区域高度", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "20", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = config.bottomTriggerHeightDp,
                            onValueChange = { viewModel.setBottomTriggerHeight(it) },
                            valueRange = 20f..80f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                        )
                        Text(text = "80", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = "${config.bottomTriggerHeightDp.roundToInt()}dp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )

                    // Trigger mode
                    Text(text = "触发模式", style = MaterialTheme.typography.titleMedium)
                    Column(Modifier.selectableGroup()) {
                        TriggerModeOption(
                            label = "轻触",
                            description = "触碰即检测",
                            selected = config.bottomTriggerMode == BottomTriggerMode.TOUCH,
                            onClick = { viewModel.setBottomTriggerMode(BottomTriggerMode.TOUCH) },
                        )
                        TriggerModeOption(
                            label = "滑动",
                            description = "仅上滑触发，点击穿透",
                            selected = config.bottomTriggerMode == BottomTriggerMode.SWIPE,
                            onClick = { viewModel.setBottomTriggerMode(BottomTriggerMode.SWIPE) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitch(
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
private fun TriggerModeOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
