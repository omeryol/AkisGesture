package com.omer.akisgesture.ui.screen

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
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.omer.akisgesture.model.GestureType
import com.omer.akisgesture.model.SectionRange
import com.omer.akisgesture.model.TriggerMode
import com.omer.akisgesture.overlay.Edge
import com.omer.akisgesture.ui.component.ActionPickerDialog
import com.omer.akisgesture.ui.viewmodel.RuleConfigViewModel
import com.omer.akisgesture.ui.util.actionIcon
import com.omer.akisgesture.ui.util.edgeIcon
import com.omer.akisgesture.ui.util.edgeLabel
import com.omer.akisgesture.ui.util.gestureLabel
import com.omer.akisgesture.ui.util.sectionLabel
import kotlinx.coroutines.launch

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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kural ayrıntısı") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.applyRules()
                        scope.launch {
                            snackbarHostState.showSnackbar("Kaydedildi")
                        }
                        onNavigateBack()
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "Kaydet")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (rule == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Kural bulunamadı", style = MaterialTheme.typography.bodyLarge)
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
                    headlineContent = { Text("Kuralı etkinleştir") },
                    supportingContent = {
                        Text(if (rule.enabled) "Etkin" else "Kapalı")
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
                        text = "Hareket koşulu",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(12.dp))

                    // Edge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Kenar", modifier = Modifier.weight(1f))
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

                    Text(
                        if (rule.trigger.edge == Edge.BOTTOM)
                            "Sol ve sağ sınır"
                        else
                            "Üst ve alt sınır",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    RangeSlider(
                        value = rule.trigger.section.start..rule.trigger.section.end,
                        onValueChange = { newRange ->
                            val start = newRange.start.coerceIn(0f, 0.9f)
                            val end = newRange.endInclusive.coerceIn(start + 0.1f, 1f)
                            viewModel.updateRuleTrigger(
                                ruleId,
                                rule.trigger.copy(section = SectionRange(start, end)),
                            )
                        },
                        valueRange = 0f..1f,
                        steps = 9,
                    )
                    Text(
                        "${(rule.trigger.section.start * 100).toInt()}% – " +
                            "${(rule.trigger.section.end * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(8.dp))

                    // Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Alan", modifier = Modifier.weight(1f))
                        val sectionOptions = SectionRange.presets(rule.trigger.edge)
                        androidx.compose.foundation.layout.Box {
                            OutlinedButton(onClick = { showSectionMenu = true }) {
                                Text(sectionLabel(rule.trigger.section, rule.trigger.edge))
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Hareket türü", modifier = Modifier.weight(1f))
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

            // ── Trigger Mode ──
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tetikleme biçimi",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(Modifier.selectableGroup()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = rule.triggerMode == TriggerMode.SWIPE,
                                    onClick = { viewModel.updateRuleTriggerMode(ruleId, TriggerMode.SWIPE) },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = rule.triggerMode == TriggerMode.SWIPE, onClick = null)
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text("Kaydırma", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Yalnızca kaydırınca çalışır; normal dokunuş uygulamaya geçer.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = rule.triggerMode == TriggerMode.TOUCH,
                                    onClick = { viewModel.updateRuleTriggerMode(ruleId, TriggerMode.TOUCH) },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = rule.triggerMode == TriggerMode.TOUCH, onClick = null)
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text("Dokunma", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Kenara dokunulduğu anda algılar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            // ── Action ──
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Yapılacak işlem") },
                    supportingContent = {
                        Text("${actionIcon(rule.action)} ${rule.action.label}")
                    },
                    trailingContent = {
                        OutlinedButton(onClick = { showActionPicker = true }) {
                            Text("Değiştir")
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
                Text("Kuralı sil")
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
            title = { Text("OnaylaSil") },
            text = { Text("Bu kural silinsin mi? Bu işlem geri alınamaz.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeRule(ruleId)
                    showDeleteConfirm = false
                    onNavigateBack()
                }) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("İptal")
                }
            },
        )
    }
}
