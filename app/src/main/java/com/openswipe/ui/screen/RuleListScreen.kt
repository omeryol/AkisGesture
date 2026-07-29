package com.omer.akisgesture.ui.screen

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
import androidx.compose.material3.AlertDialog
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
import com.omer.akisgesture.model.GestureRule
import com.omer.akisgesture.model.GestureType
import com.omer.akisgesture.ui.component.ActionPickerDialog
import com.omer.akisgesture.ui.component.AddRuleDialog
import com.omer.akisgesture.ui.component.GestureMapCard
import com.omer.akisgesture.ui.theme.AkisGesturePrimary
import com.omer.akisgesture.ui.viewmodel.RuleConfigViewModel
import com.omer.akisgesture.ui.util.actionIcon
import com.omer.akisgesture.ui.util.edgeIcon
import com.omer.akisgesture.ui.util.edgeLabel
import com.omer.akisgesture.ui.util.gestureLabel
import com.omer.akisgesture.ui.util.sectionLabel

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
    val gestureConfig by viewModel.gestureConfig.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showPresetMenu by remember { mutableStateOf(false) }
    // Rule whose action is being edited
    var editingActionRuleId by remember { mutableStateOf<String?>(null) }
    var selectedGroupKey by remember { mutableStateOf<String?>(null) }
    var addingGestureType by remember { mutableStateOf<GestureType?>(null) }

    val context = LocalContext.current
    val ruleGroups = rules
        .groupBy { Triple(it.trigger.edge, it.trigger.section, it.triggerMode) }
        .map { (_, groupedRules) ->
            RuleGroup(
                quick = groupedRules.firstOrNull {
                    it.trigger.gestureType == GestureType.QUICK_SWIPE
                },
                hold = groupedRules.firstOrNull {
                    it.trigger.gestureType == GestureType.SWIPE_HOLD
                },
            )
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hareket kuralları") },
                actions = {
                    val applyEnabled = hasUnapplied && conflicts.isEmpty()
                    TextButton(
                        onClick = {
                            viewModel.applyRules()
                            Toast.makeText(context, "Kurallar uygulandı", Toast.LENGTH_SHORT).show()
                        },
                        enabled = applyEnabled,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = if (applyEnabled)
                                AkisGesturePrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Uygula",
                            color = if (applyEnabled)
                                AkisGesturePrimary
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
                text = { Text("Kural ekle") },
                containerColor = AkisGesturePrimary,
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
                    text = "Hazır düzen",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
                Box {
                    OutlinedButton(onClick = { showPresetMenu = true }) {
                        Text(activePreset ?: "Özel")
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
                            text = "${conflicts.size}  çakışma: ${conflicts.firstOrNull()?.message ?: ""}",
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
                        text = "Henüz kural yok. Hazır bir düzen seçin veya kural ekleyin.",
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
                    item(key = "gesture-map") {
                        GestureMapCard(
                            rules = rules,
                            config = gestureConfig,
                            onZoneClick = { tappedRule ->
                                selectedGroupKey = ruleGroups
                                    .firstOrNull { tappedRule.id in it.ids }
                                    ?.key
                            },
                            onZoneRangeChange = viewModel::updateRulesSection,
                        )
                    }
                    items(ruleGroups, key = { it.key }) { group ->
                        RuleGroupCard(
                            group = group,
                            onClick = {
                                (group.quick ?: group.hold)?.let { onRuleClick(it.id) }
                            },
                            onDelete = { viewModel.removeRules(group.ids) },
                            onChangeAction = { editingActionRuleId = it.id },
                            onToggleEnabled = {
                                viewModel.setRulesEnabled(group.ids, it)
                            },
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
            onConfirm = { edge, section, quickAction, holdAction, triggerMode ->
                viewModel.addGesturePair(
                    edge = edge,
                    section = section,
                    quickAction = quickAction,
                    holdAction = holdAction,
                    triggerMode = triggerMode,
                )
                showAddDialog = false
            },
        )
    }

    val selectedGroup = ruleGroups.firstOrNull { it.key == selectedGroupKey }
    if (selectedGroup != null) {
        AlertDialog(
            onDismissRequest = { selectedGroupKey = null },
            title = {
                Text(
                    "${edgeLabel(selectedGroup.representative.trigger.edge)} · " +
                        sectionLabel(
                            selectedGroup.representative.trigger.section,
                            selectedGroup.representative.trigger.edge,
                        ),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "İki hareketi aynı alandan düzenle",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    GestureSlotButton(
                        title = "Hızlı çekme",
                        rule = selectedGroup.quick,
                        onClick = {
                            selectedGroup.quick?.let { editingActionRuleId = it.id }
                                ?: run { addingGestureType = GestureType.QUICK_SWIPE }
                        },
                    )
                    GestureSlotButton(
                        title = "Çekip bekletme",
                        rule = selectedGroup.hold,
                        onClick = {
                            selectedGroup.hold?.let { editingActionRuleId = it.id }
                                ?: run { addingGestureType = GestureType.SWIPE_HOLD }
                        },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedGroupKey = null
                        onRuleClick(selectedGroup.representative.id)
                    },
                ) {
                    Text("İnce ayarlar")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGroupKey = null }) {
                    Text("Bitti")
                }
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

    addingGestureType?.let { gestureType ->
        val group = selectedGroup
        if (group != null) {
            ActionPickerDialog(
                onDismiss = { addingGestureType = null },
                onSelect = { action ->
                    val trigger = group.representative.trigger.copy(gestureType = gestureType)
                    viewModel.addRule(
                        trigger = trigger,
                        action = action,
                        triggerMode = group.representative.triggerMode,
                    )
                    addingGestureType = null
                },
            )
        }
    }
}

private data class RuleGroup(
    val quick: GestureRule?,
    val hold: GestureRule?,
) {
    val representative: GestureRule get() = quick ?: requireNotNull(hold)
    val ids: Set<String> get() = listOfNotNull(quick?.id, hold?.id).toSet()
    val key: String
        get() = "${representative.trigger.edge}:" +
            "${representative.trigger.section.start}:${representative.trigger.section.end}:" +
            representative.triggerMode
}

@Composable
private fun GestureSlotButton(
    title: String,
    rule: GestureRule?,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(
                text = rule?.let { "${actionIcon(it.action)} ${it.action.label}" }
                    ?: "Eylem ata",
                style = MaterialTheme.typography.bodyLarge,
                color = if (rule == null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
        Text(if (rule == null) "+" else "Değiştir")
    }
}

@Composable
private fun RuleGroupCard(
    group: RuleGroup,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onChangeAction: (GestureRule) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
) {
    val rule = group.representative
    val enabled = listOfNotNull(group.quick, group.hold).any { it.enabled }
    val contentAlpha = if (enabled) 1f else 0.45f
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
                checked = enabled,
                onCheckedChange = onToggleEnabled,
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
                        text = " \u00B7 ${sectionLabel(rule.trigger.section, rule.trigger.edge)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                group.quick?.let { quick ->
                    OutlinedButton(
                        onClick = { onChangeAction(quick) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            "Hızlı · ${actionIcon(quick.action)} ${quick.action.label}",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                group.hold?.let { hold ->
                    OutlinedButton(
                        onClick = { onChangeAction(hold) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            "Beklet · ${actionIcon(hold.action)} ${hold.action.label}",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Delete
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Kuralı sil",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
