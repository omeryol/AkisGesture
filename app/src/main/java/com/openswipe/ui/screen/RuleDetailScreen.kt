package com.openswipe.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openswipe.model.GestureType
import com.openswipe.model.SectionRange
import com.openswipe.overlay.Edge
import com.openswipe.ui.component.ActionPickerDialog
import com.openswipe.ui.viewmodel.RuleConfigViewModel
import com.openswipe.ui.viewmodel.actionIcon
import com.openswipe.ui.viewmodel.edgeIcon
import com.openswipe.ui.viewmodel.edgeLabel
import com.openswipe.ui.viewmodel.gestureLabel
import com.openswipe.ui.viewmodel.sectionLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleDetailScreen(
    ruleId: String,
    viewModel: RuleConfigViewModel,
    onNavigateBack: () -> Unit,
) {
    val rules by viewModel.rules.collectAsState()
    val rule = rules.find { it.id == ruleId }

    var showActionPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEdgeMenu by remember { mutableStateOf(false) }
    var showSectionMenu by remember { mutableStateOf(false) }
    var showGestureMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("规则详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (rule == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("规则不存在", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Enable/Disable ──
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("启用规则") },
                    supportingContent = {
                        Text(if (rule.enabled) "已启用" else "已禁用")
                    },
                    trailingContent = {
                        Switch(
                            checked = rule.enabled,
                            onCheckedChange = { viewModel.toggleRuleEnabled(ruleId) },
                        )
                    },
                )
            }

            // ── Trigger Condition ──
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "触发条件",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(12.dp))

                    // Edge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("边缘", modifier = Modifier.weight(1f))
                        androidx.compose.foundation.layout.Box {
                            OutlinedButton(onClick = { showEdgeMenu = true }) {
                                Text("${edgeIcon(rule.trigger.edge)} ${edgeLabel(rule.trigger.edge)}")
                            }
                            DropdownMenu(
                                expanded = showEdgeMenu,
                                onDismissRequest = { showEdgeMenu = false },
                            ) {
                                Edge.entries.forEach { edge ->
                                    DropdownMenuItem(
                                        text = { Text("${edgeIcon(edge)} ${edgeLabel(edge)}") },
                                        onClick = {
                                            viewModel.updateRuleTrigger(
                                                ruleId,
                                                rule.trigger.copy(edge = edge),
                                            )
                                            showEdgeMenu = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("区段", modifier = Modifier.weight(1f))
                        val sectionOptions = listOf(
                            "全段" to SectionRange.ALL,
                            "左1/3" to SectionRange.thirds(0),
                            "中1/3" to SectionRange.thirds(1),
                            "右1/3" to SectionRange.thirds(2),
                            "前半" to SectionRange.halves(0),
                            "后半" to SectionRange.halves(1),
                        )
                        androidx.compose.foundation.layout.Box {
                            OutlinedButton(onClick = { showSectionMenu = true }) {
                                Text(sectionLabel(rule.trigger.section))
                            }
                            DropdownMenu(
                                expanded = showSectionMenu,
                                onDismissRequest = { showSectionMenu = false },
                            ) {
                                sectionOptions.forEach { (label, section) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            viewModel.updateRuleTrigger(
                                                ruleId,
                                                rule.trigger.copy(section = section),
                                            )
                                            showSectionMenu = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Gesture type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("手势类型", modifier = Modifier.weight(1f))
                        androidx.compose.foundation.layout.Box {
                            OutlinedButton(onClick = { showGestureMenu = true }) {
                                Text(gestureLabel(rule.trigger.gestureType))
                            }
                            DropdownMenu(
                                expanded = showGestureMenu,
                                onDismissRequest = { showGestureMenu = false },
                            ) {
                                GestureType.entries.forEach { gestureType ->
                                    DropdownMenuItem(
                                        text = { Text(gestureLabel(gestureType)) },
                                        onClick = {
                                            viewModel.updateRuleTrigger(
                                                ruleId,
                                                rule.trigger.copy(gestureType = gestureType),
                                            )
                                            showGestureMenu = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Action ──
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("执行动作") },
                    supportingContent = {
                        Text("${actionIcon(rule.action)} ${rule.action.label}")
                    },
                    trailingContent = {
                        OutlinedButton(onClick = { showActionPicker = true }) {
                            Text("更换")
                        }
                    },
                )
            }

            // ── Delete ──
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("删除规则")
            }
        }
    }

    // ── Dialogs ──

    if (showActionPicker) {
        ActionPickerDialog(
            onDismiss = { showActionPicker = false },
            onSelect = { action ->
                viewModel.updateRuleAction(ruleId, action)
                showActionPicker = false
            },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除此规则吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeRule(ruleId)
                    showDeleteConfirm = false
                    onNavigateBack()
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            },
        )
    }
}
