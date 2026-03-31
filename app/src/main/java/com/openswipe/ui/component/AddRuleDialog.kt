package com.openswipe.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openswipe.model.ActionNode
import com.openswipe.model.GestureType
import com.openswipe.model.SectionRange
import com.openswipe.model.TriggerNode
import com.openswipe.overlay.Edge
import com.openswipe.ui.theme.OpenSwipePrimary
import com.openswipe.ui.util.actionCategories
import com.openswipe.ui.util.actionIcon
import com.openswipe.ui.viewmodel.RuleConfigViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (TriggerNode, ActionNode) -> Unit,
) {
    // Wizard state: 0=edge, 1=section, 2=gesture, 3=action
    var step by remember { mutableIntStateOf(0) }
    var selectedEdge by remember { mutableStateOf<Edge?>(null) }
    var selectedSection by remember { mutableStateOf<SectionRange?>(null) }
    var selectedGesture by remember { mutableStateOf<GestureType?>(null) }
    var selectedAction by remember { mutableStateOf<ActionNode?>(null) }

    val stepTitles = listOf("选择边缘", "选择区段", "选择手势", "选择动作")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("添加规则 - ${stepTitles[step]}")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Step indicator
                Text(
                    text = "步骤 ${step + 1} / 4",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))

                when (step) {
                    0 -> EdgeSelector(
                        selected = selectedEdge,
                        onSelect = { selectedEdge = it },
                    )
                    1 -> SectionSelector(
                        edge = selectedEdge!!,
                        selected = selectedSection,
                        onSelect = { selectedSection = it },
                    )
                    2 -> GestureSelector(
                        selected = selectedGesture,
                        onSelect = { selectedGesture = it },
                    )
                    3 -> ActionSelector(
                        selected = selectedAction,
                        onSelect = { selectedAction = it },
                    )
                }
            }
        },
        confirmButton = {
            if (step < 3) {
                Button(
                    onClick = {
                        // Auto-fill defaults on skip
                        when (step) {
                            0 -> if (selectedEdge != null) {
                                // For left/right, default section to ALL
                                if (selectedEdge != Edge.BOTTOM && selectedSection == null) {
                                    selectedSection = SectionRange.ALL
                                }
                                step++
                            }
                            1 -> if (selectedSection != null) step++
                            2 -> if (selectedGesture != null) step++
                        }
                    },
                    enabled = when (step) {
                        0 -> selectedEdge != null
                        1 -> selectedSection != null
                        2 -> selectedGesture != null
                        else -> false
                    },
                ) {
                    Text("下一步")
                }
            } else {
                Button(
                    onClick = {
                        if (selectedEdge != null && selectedSection != null &&
                            selectedGesture != null && selectedAction != null
                        ) {
                            onConfirm(
                                TriggerNode(selectedEdge!!, selectedSection!!, selectedGesture!!),
                                selectedAction!!,
                            )
                        }
                    },
                    enabled = selectedAction != null,
                ) {
                    Text("确认")
                }
            }
        },
        dismissButton = {
            if (step > 0) {
                OutlinedButton(onClick = { step-- }) {
                    Text("上一步")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EdgeSelector(
    selected: Edge?,
    onSelect: (Edge) -> Unit,
) {
    val edges = listOf(
        Triple(Edge.LEFT, "\u2190 左侧", "从左边缘向右滑"),
        Triple(Edge.RIGHT, "\u2192 右侧", "从右边缘向左滑"),
        Triple(Edge.BOTTOM, "\u2193 底部", "从底部向上滑"),
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        edges.forEach { (edge, label, desc) ->
            FilterChip(
                selected = selected == edge,
                onClick = { onSelect(edge) },
                label = {
                    Column {
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                        Text(desc, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OpenSwipePrimary.copy(alpha = 0.15f),
                ),
                border = if (selected == edge) BorderStroke(1.dp, OpenSwipePrimary) else null,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SectionSelector(
    edge: Edge,
    selected: SectionRange?,
    onSelect: (SectionRange) -> Unit,
) {
    val options = if (edge == Edge.BOTTOM) {
        SectionRange.PRESETS
    } else {
        // Left / Right edges: only full section makes sense for Phase 1
        listOf("全段" to SectionRange.ALL)
    }

    if (options.size == 1) {
        // Auto-select for left/right
        Text("左右边缘默认使用全段", style = MaterialTheme.typography.bodyMedium)
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (label, section) ->
            FilterChip(
                selected = selected == section,
                onClick = { onSelect(section) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OpenSwipePrimary.copy(alpha = 0.15f),
                ),
                border = if (selected == section) BorderStroke(1.dp, OpenSwipePrimary) else null,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GestureSelector(
    selected: GestureType?,
    onSelect: (GestureType) -> Unit,
) {
    val gestures = listOf(
        "短滑" to GestureType.SHORT_SWIPE,
        "长滑" to GestureType.LONG_SWIPE,
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        gestures.forEach { (label, type) ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OpenSwipePrimary.copy(alpha = 0.15f),
                ),
                border = if (selected == type) BorderStroke(1.dp, OpenSwipePrimary) else null,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionSelector(
    selected: ActionNode?,
    onSelect: (ActionNode) -> Unit,
) {
    val categories = actionCategories()

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
                    selected = selected == action,
                    onClick = { if (available) onSelect(action) },
                    label = { Text("${actionIcon(action)} ${action.label}", maxLines = 1) },
                    enabled = available,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OpenSwipePrimary.copy(alpha = 0.15f),
                    ),
                    border = if (selected == action) BorderStroke(1.dp, OpenSwipePrimary) else null,
                )
            }
        }
    }
}
