package com.openswipe.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openswipe.model.ActionNode
import com.openswipe.ui.theme.OpenSwipePrimary
import com.openswipe.ui.util.actionCategories
import com.openswipe.ui.util.actionIcon
import com.openswipe.ui.viewmodel.RuleConfigViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActionPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (ActionNode) -> Unit,
) {
    val categories = actionCategories()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择动作") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                categories.forEach { (categoryName, items) ->
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items.forEach { action ->
                            val available = RuleConfigViewModel.isActionAvailable(action)
                            FilterChip(
                                selected = false,
                                onClick = {
                                    if (available) {
                                        onSelect(action)
                                    }
                                },
                                label = {
                                    Text(
                                        "${actionIcon(action)} ${action.label}",
                                        maxLines = 1,
                                    )
                                },
                                enabled = available,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OpenSwipePrimary.copy(alpha = 0.15f),
                                ),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
