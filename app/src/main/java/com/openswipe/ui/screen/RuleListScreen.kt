package com.openswipe.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.openswipe.model.GestureRule
import com.openswipe.ui.component.ActionPickerDialog
import com.openswipe.ui.component.AddRuleDialog
import com.openswipe.ui.theme.OpenSwipePrimary
import com.openswipe.ui.viewmodel.RuleConfigViewModel
import com.openswipe.ui.util.actionIcon
import com.openswipe.ui.util.edgeIcon
import com.openswipe.ui.util.edgeLabel
import com.openswipe.ui.util.gestureLabel
import com.openswipe.ui.util.sectionLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleListScreen(
    viewModel: RuleConfigViewModel,
    onRuleClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val rules by viewModel.rules.collectAsState()
    val conflicts by viewModel.conflicts.collectAsState()
    val hasUnapplied by viewModel.hasUnappliedChanges.collectAsState()
    val activePreset by viewModel.activePresetName.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showPresetMenu by remember { mutableStateOf(false) }
    // Rule whose action is being edited
    var editingActionRuleId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("手势规则") },
                actions = {
                    val applyEnabled = hasUnapplied && conflicts.isEmpty()
                    TextButton(
                        onClick = {
                            viewModel.applyRules()
                            Toast.makeText(context, "规则已应用", Toast.LENGTH_SHORT).show()
                        },
                        enabled = applyEnabled,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = if (applyEnabled)
                                OpenSwipePrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "应用",
                            color = if (applyEnabled)
                                OpenSwipePrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("添加规则") },
                containerColor = OpenSwipePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // ── Preset selector ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "预设方案",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
                Box {
                    OutlinedButton(onClick = { showPresetMenu = true }) {
                        Text(activePreset ?: "自定义")
                    }
                    DropdownMenu(
                        expanded = showPresetMenu,
                        onDismissRequest = { showPresetMenu = false },
                    ) {
                        RuleConfigViewModel.presets.forEach { (name, graph) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    viewModel.loadPreset(name, graph)
                                    showPresetMenu = false
                                },
                            )
                        }
                    }
                }
            }

            // ── Conflict banner ──
            AnimatedVisibility(visible = conflicts.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${conflicts.size} 条冲突：${conflicts.firstOrNull()?.message ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            // ── Rule list ──
            if (rules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "暂无规则，请选择预设或添加规则",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(rules, key = { it.id }) { rule ->
                        RuleCard(
                            rule = rule,
                            onClick = { onRuleClick(rule.id) },
                            onDelete = { viewModel.removeRule(rule.id) },
                            onChangeAction = { editingActionRuleId = rule.id },
                            onToggleEnabled = { viewModel.toggleRuleEnabled(rule.id) },
                        )
                    }
                }
            }
        }
    }

    // ── Dialogs ──

    if (showAddDialog) {
        AddRuleDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { trigger, action, triggerMode ->
                viewModel.addRule(trigger, action, triggerMode)
                showAddDialog = false
            },
        )
    }

    editingActionRuleId?.let { ruleId ->
        ActionPickerDialog(
            onDismiss = { editingActionRuleId = null },
            onSelect = { action ->
                viewModel.updateRuleAction(ruleId, action)
                editingActionRuleId = null
            },
        )
    }
}

@Composable
private fun RuleCard(
    rule: GestureRule,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onChangeAction: () -> Unit,
    onToggleEnabled: () -> Unit,
) {
    val contentAlpha = if (rule.enabled) 1f else 0.45f
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Enable/disable switch
            Switch(
                checked = rule.enabled,
                onCheckedChange = { onToggleEnabled() },
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.width(8.dp))

            // Trigger side
            Column(modifier = Modifier.weight(1f).graphicsLayer { alpha = contentAlpha }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = edgeIcon(rule.trigger.edge),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = edgeLabel(rule.trigger.edge),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = " \u00B7 ${sectionLabel(rule.trigger.section, rule.trigger.edge)} \u00B7 ${gestureLabel(rule.trigger.gestureType)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Arrow
            Text(
                text = " \u2192 ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Action side — clickable to change
            OutlinedButton(
                onClick = onChangeAction,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${actionIcon(rule.action)} ${rule.action.label}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Delete
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "删除规则",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
