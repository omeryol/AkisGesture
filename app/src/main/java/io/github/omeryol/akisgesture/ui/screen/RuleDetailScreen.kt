package io.github.omeryol.akisgesture.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.R
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.model.TriggerMode
import io.github.omeryol.akisgesture.navigation.InternalNavigationBus
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.ui.component.ActionIcon
import io.github.omeryol.akisgesture.ui.component.AkisGlassCard
import io.github.omeryol.akisgesture.ui.component.AkisRangeSliderRow
import io.github.omeryol.akisgesture.ui.component.AkisSectionHeader
import io.github.omeryol.akisgesture.ui.component.AkisSwitchRow
import io.github.omeryol.akisgesture.ui.util.edgeIcon
import io.github.omeryol.akisgesture.ui.util.edgeLabel
import io.github.omeryol.akisgesture.ui.util.gestureLabel
import io.github.omeryol.akisgesture.ui.util.localizedLabel
import io.github.omeryol.akisgesture.ui.util.sectionLabel
import io.github.omeryol.akisgesture.ui.viewmodel.RuleConfigViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleDetailScreen(
    ruleId: String,
    viewModel: RuleConfigViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val rules by viewModel.rules.collectAsState()
    val rule = rules.find { it.id == ruleId }

    var actionPickerToken by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSectionMenu by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(actionPickerToken) {
        val token = actionPickerToken ?: return@LaunchedEffect
        InternalNavigationBus.actionPickerResults.collect { result ->
            if (result.token == token) {
                viewModel.updateRuleAction(ruleId, result.action)
                actionPickerToken = null
            }
        }
    }

    Scaffold(
        containerColor = scheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.fine_tuning), fontWeight = FontWeight.Bold)
                        rule?.let {
                            Text(
                                "${edgeLabel(context, it.trigger.edge)} · " +
                                    sectionLabel(context, it.trigger.section, it.trigger.edge),
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.applyRules()
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.saved))
                        }
                        onNavigateBack()
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.save), tint = scheme.primary)
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
                Text(stringResource(R.string.rule_not_found), style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── 1. KURAL ETKİNLİŞTİRME ──
            AkisGlassCard(accentTint = if (rule.enabled) scheme.primary else scheme.outline) {
                AkisSwitchRow(
                    title = stringResource(R.string.enable_rule),
                    subtitle = stringResource(if (rule.enabled) R.string.enabled else R.string.disabled),
                    checked = rule.enabled,
                    onCheckedChange = { viewModel.toggleRuleEnabled(ruleId) },
                    icon = Icons.Filled.Check,
                )
            }

            // ── 2. TETİKLEYİCİ KOŞULU & DOKUNMA ALANI ──
            AkisGlassCard(accentTint = scheme.primary) {
                AkisSectionHeader(
                    title = stringResource(R.string.gesture_condition),
                    subtitle = stringResource(R.string.assign_area_intro),
                    icon = Icons.Filled.Tune,
                )
                Spacer(Modifier.height(10.dp))

                // Edge Selector (Kenar)
                Text(
                    text = stringResource(R.string.edge),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Edge.entries.forEach { edge ->
                        val label = "${edgeIcon(edge)} ${edgeLabel(context, edge)}"
                        val selected = rule.trigger.edge == edge
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) scheme.primary else scheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable {
                                    viewModel.updateRuleTrigger(ruleId, rule.trigger.copy(edge = edge))
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) scheme.onPrimary else scheme.onSurface,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Area Range Slider (Sürükleme Aralığı)
                val boundsTitle = if (rule.trigger.edge == Edge.BOTTOM) {
                    stringResource(R.string.horizontal_bounds)
                } else {
                    stringResource(R.string.vertical_bounds)
                }
                val boundsValueText = "${(rule.trigger.section.start * 100).toInt()}% – ${(rule.trigger.section.end * 100).toInt()}%"

                AkisRangeSliderRow(
                    title = boundsTitle,
                    valueText = boundsValueText,
                    value = rule.trigger.section.start..rule.trigger.section.end,
                    valueRange = 0f..1f,
                    steps = 9,
                    onValueChange = { newRange ->
                        val start = newRange.start.coerceIn(0f, 0.9f)
                        val end = newRange.endInclusive.coerceIn(start + 0.1f, 1f)
                        viewModel.updateRuleTrigger(
                            ruleId,
                            rule.trigger.copy(section = SectionRange(start, end)),
                        )
                    },
                )

                Spacer(Modifier.height(8.dp))

                // Area Preset Dropdown Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.area),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                    )
                    Box {
                        OutlinedButton(
                            onClick = { showSectionMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                sectionLabel(context, rule.trigger.section, rule.trigger.edge),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        DropdownMenu(
                            expanded = showSectionMenu,
                            onDismissRequest = { showSectionMenu = false },
                            modifier = Modifier.background(scheme.surface),
                        ) {
                            SectionRange.presets(rule.trigger.edge).forEach { (label, section) ->
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

                // Gesture Type Pill Selector
                Text(
                    text = stringResource(R.string.gesture_type),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    GestureType.entries.forEach { gestureType ->
                        val label = gestureLabel(context, gestureType)
                        val selected = rule.trigger.gestureType == gestureType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) scheme.primaryContainer else scheme.surfaceVariant.copy(alpha = 0.35f))
                                .clickable {
                                    viewModel.updateRuleTrigger(ruleId, rule.trigger.copy(gestureType = gestureType))
                                }
                                .padding(vertical = 8.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            // ── 3. TETİKLEME MODU ──
            AkisGlassCard(accentTint = scheme.secondary) {
                AkisSectionHeader(
                    title = stringResource(R.string.trigger_mode),
                    subtitle = stringResource(R.string.gesture_condition),
                    icon = Icons.Filled.Swipe,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(scheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf(
                        TriggerMode.SWIPE to Pair(stringResource(R.string.swipe), stringResource(R.string.swipe_description)),
                        TriggerMode.TOUCH to Pair(stringResource(R.string.touch), stringResource(R.string.touch_description)),
                    ).forEach { (mode, textPair) ->
                        val selected = rule.triggerMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) scheme.primary else Color.Transparent)
                                .clickable { viewModel.updateRuleTriggerMode(ruleId, mode) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = textPair.first,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) scheme.onPrimary else scheme.onSurface,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = textPair.second,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) scheme.onPrimary.copy(alpha = 0.85f) else scheme.onSurfaceVariant,
                                    maxLines = 2,
                                )
                            }
                        }
                    }
                }
            }

            // ── 4. ATANAN EYLEM ──
            AkisGlassCard(accentTint = scheme.tertiary) {
                AkisSectionHeader(
                    title = stringResource(R.string.assigned_action),
                    subtitle = stringResource(R.string.edit_area_gestures),
                    icon = Icons.Filled.Apps,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(scheme.surfaceVariant.copy(alpha = 0.42f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionIcon(
                        action = rule.action,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = rule.action.localizedLabel(context),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(
                        onClick = {
                            val token = UUID.randomUUID().toString()
                            actionPickerToken = token
                            InternalNavigationBus.requestActionPicker(
                                InternalNavigationBus.ActionPickerRequest(token),
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(stringResource(R.string.change), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // ── 5. KURAL SİL ──
            AkisGlassCard(
                accentTint = scheme.error,
                containerColor = scheme.errorContainer.copy(alpha = 0.15f),
            ) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = scheme.error,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.delete_rule), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // ── Silme Onay Diyaloğu ──
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.confirm)) },
            text = { Text(stringResource(R.string.delete_rule_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeRule(ruleId)
                    showDeleteConfirm = false
                    onNavigateBack()
                }) {
                    Text(stringResource(R.string.delete), color = scheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
