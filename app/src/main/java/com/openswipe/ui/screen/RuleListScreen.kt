package com.omer.akisgesture.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.omer.akisgesture.overlay.Edge
import com.omer.akisgesture.ui.component.ActionPickerDialog
import com.omer.akisgesture.ui.component.AddRuleDialog
import com.omer.akisgesture.ui.component.GestureMapCard
import com.omer.akisgesture.ui.theme.AkisGesturePrimary
import com.omer.akisgesture.ui.viewmodel.RuleConfigViewModel
import com.omer.akisgesture.ui.util.actionImageVector
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
    var selectedEdge by remember { mutableStateOf(Edge.BOTTOM) }
    var showMap by remember { mutableStateOf(false) }

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
    val visibleGroups = ruleGroups.filter { it.representative.trigger.edge == selectedEdge }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hareketler") },
                actions = {
                    Box {
                        TextButton(onClick = { showPresetMenu = true }) {
                            Text(activePreset ?: "Düzen")
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
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AkisGesturePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Kural ekle")
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val edges = listOf(Edge.BOTTOM, Edge.LEFT, Edge.RIGHT)
            TabRow(selectedTabIndex = edges.indexOf(selectedEdge)) {
                edges.forEach { edge ->
                    val count = ruleGroups.count { it.representative.trigger.edge == edge }
                    Tab(
                        selected = selectedEdge == edge,
                        onClick = { selectedEdge = edge },
                        text = {
                            Text(
                                when (edge) {
                                    Edge.BOTTOM -> "Alt · $count"
                                    Edge.LEFT -> "Sol · $count"
                                    Edge.RIGHT -> "Sağ · $count"
                                },
                            )
                        },
                    )
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
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    item(key = "edge-header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Alanlar",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "${visibleGroups.size}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = { showMap = true }) {
                                Icon(Icons.Filled.Map, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Harita")
                            }
                        }
                    }
                    item(key = "column-header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Bölge",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(0.9f).padding(start = 8.dp),
                            )
                            Text(
                                "Hızlı",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1.05f).padding(start = 8.dp),
                            )
                            Text(
                                "Beklet",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1.05f).padding(start = 8.dp),
                            )
                            Spacer(Modifier.width(48.dp))
                        }
                    }
                    items(visibleGroups, key = { it.key }) { group ->
                        RuleTableRow(
                            group = group,
                            onClick = { selectedGroupKey = group.key },
                            onDelete = { viewModel.removeRules(group.ids) },
                            onSelectAction = { gestureType, rule ->
                                selectedGroupKey = group.key
                                if (rule != null) {
                                    editingActionRuleId = rule.id
                                } else {
                                    addingGestureType = gestureType
                                }
                            },
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

    if (showMap) {
        AlertDialog(
            onDismissRequest = { showMap = false },
            title = { Text("Hareket alanları") },
            text = {
                GestureMapCard(
                    rules = rules,
                    config = gestureConfig,
                    onZoneClick = { tappedRule ->
                        selectedGroupKey = ruleGroups
                            .firstOrNull { tappedRule.id in it.ids }
                            ?.key
                        showMap = false
                    },
                    onZoneRangeChange = viewModel::updateRulesSection,
                )
            },
            confirmButton = {
                TextButton(onClick = { showMap = false }) { Text("Bitti") }
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
        rule?.let {
            Icon(
                actionImageVector(it.action),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(
                text = rule?.action?.label
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
private fun RuleTableRow(
    group: RuleGroup,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSelectAction: (GestureType, GestureRule?) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
) {
    val rule = group.representative
    val enabled = listOfNotNull(group.quick, group.hold).any { it.enabled }
    val contentAlpha = if (enabled) 1f else 0.45f
    var menuOpen by remember { mutableStateOf(false) }
    Column(modifier = Modifier.graphicsLayer { alpha = contentAlpha }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            ) {
                Text(
                    sectionLabel(rule.trigger.section, rule.trigger.edge),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (enabled) "Etkin" else "Kapalı",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ActionCell(
                label = "Hızlı",
                rule = group.quick,
                modifier = Modifier.weight(1.05f),
                onClick = {
                    onSelectAction(GestureType.QUICK_SWIPE, group.quick)
                },
            )
            ActionCell(
                label = "Beklet",
                rule = group.hold,
                modifier = Modifier.weight(1.05f),
                onClick = {
                    onSelectAction(GestureType.SWIPE_HOLD, group.hold)
                },
            )
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Alan seçenekleri")
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(if (enabled) "Devre dışı bırak" else "Etkinleştir") },
                        onClick = {
                            onToggleEnabled(!enabled)
                            menuOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("İnce ayarlar") },
                        onClick = {
                            menuOpen = false
                            onClick()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Sil") },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ActionCell(
    label: String,
    rule: GestureRule?,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        if (rule != null) {
            Icon(
                actionImageVector(rule.action),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                rule.action.label,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
