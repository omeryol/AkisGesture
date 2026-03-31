package com.openswipe.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openswipe.ui.viewmodel.HomeViewModel

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
