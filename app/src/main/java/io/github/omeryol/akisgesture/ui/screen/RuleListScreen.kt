package io.github.omeryol.akisgesture.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import io.github.omeryol.akisgesture.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.model.ActionIconPack
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.ui.component.ActionIcon
import io.github.omeryol.akisgesture.ui.component.AddRuleDialog
import io.github.omeryol.akisgesture.ui.component.AkisGlassCard
import io.github.omeryol.akisgesture.ui.component.EdgeZoneVisual
import io.github.omeryol.akisgesture.ui.component.GestureMapCard
import io.github.omeryol.akisgesture.ui.util.appLabel
import io.github.omeryol.akisgesture.ui.util.edgeLabel
import io.github.omeryol.akisgesture.ui.util.sectionLabel
import io.github.omeryol.akisgesture.ui.util.localizedLabel
import io.github.omeryol.akisgesture.ui.util.actionEmoji
import io.github.omeryol.akisgesture.ui.viewmodel.RuleConfigViewModel
import io.github.omeryol.akisgesture.rule.Presets
import io.github.omeryol.akisgesture.navigation.InternalNavigationBus
import io.github.omeryol.akisgesture.ui.theme.AkisPrimary
import io.github.omeryol.akisgesture.ui.theme.AkisSecondary
import io.github.omeryol.akisgesture.ui.theme.AkisTertiary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleListScreen(
    viewModel: RuleConfigViewModel,
    onRuleClick: (String) -> Unit,
    initialEdge: Edge = Edge.LEFT,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val rules by viewModel.rules.collectAsState()
    val gestureConfig by viewModel.gestureConfig.collectAsState()
    val conflicts by viewModel.conflicts.collectAsState()
    val activePreset by viewModel.activePresetName.collectAsState()
    val activeProfilePackage by viewModel.activeProfilePackage.collectAsState()
    val presetNames = stringArrayResource(R.array.preset_names)
    val presetDescriptions = stringArrayResource(R.array.preset_descriptions)

    val activeProfileLabel = remember(activeProfilePackage, context) {
        activeProfilePackage?.let { appLabel(context, it) } ?: context.getString(R.string.general_layout)
    }

    var selectedEdge by remember(initialEdge) { mutableStateOf(initialEdge) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }
    var selectedGroupKey by remember { mutableStateOf<String?>(null) }
    var editingActionRuleId by remember { mutableStateOf<String?>(null) }
    var addingGestureType by remember { mutableStateOf<GestureType?>(null) }
    var actionPickerToken by remember { mutableStateOf<String?>(null) }
    var showPresetMenu by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }
    var showProfileAppPicker by remember { mutableStateOf(false) }
    var deleteProfilePackage by remember { mutableStateOf<String?>(null) }

    fun openActionPicker() {
        val token = UUID.randomUUID().toString()
        actionPickerToken = token
        InternalNavigationBus.requestActionPicker(
            InternalNavigationBus.ActionPickerRequest(token),
        )
    }

    val ruleGroups = remember(rules) {
        rules
            .groupBy { Triple(it.trigger.edge, it.trigger.section, it.triggerMode) }
            .map { (_, groupedRules) ->
                RuleGroup(
                    quick = groupedRules.firstOrNull { it.trigger.gestureType == GestureType.QUICK_SWIPE },
                    hold = groupedRules.firstOrNull { it.trigger.gestureType == GestureType.SWIPE_HOLD },
                    lUp = groupedRules.firstOrNull { it.trigger.gestureType == GestureType.SWIPE_UP_L },
                    lDown = groupedRules.firstOrNull { it.trigger.gestureType == GestureType.SWIPE_DOWN_L },
                )
            }
    }

    LaunchedEffect(actionPickerToken) {
        val token = actionPickerToken ?: return@LaunchedEffect
        InternalNavigationBus.actionPickerResults.collect { result ->
            if (result.token == token) {
                editingActionRuleId?.let { ruleId ->
                    viewModel.updateRuleAction(ruleId, result.action)
                } ?: addingGestureType?.let { gestureType ->
                    val repRule = ruleGroups.firstOrNull { it.key == selectedGroupKey }?.representative
                    repRule?.let { rule ->
                        viewModel.addGesturePair(
                            edge = rule.trigger.edge,
                            section = rule.trigger.section,
                            quickAction = if (gestureType == GestureType.QUICK_SWIPE) result.action else null,
                            holdAction = if (gestureType == GestureType.SWIPE_HOLD) result.action else null,
                            lUpAction = if (gestureType == GestureType.SWIPE_UP_L) result.action else null,
                            lDownAction = if (gestureType == GestureType.SWIPE_DOWN_L) result.action else null,
                            triggerMode = rule.triggerMode,
                        )
                    }
                }
                editingActionRuleId = null
                addingGestureType = null
                actionPickerToken = null
            }
        }
    }
    val visibleGroups = ruleGroups.filter { it.representative.trigger.edge == selectedEdge }
    val ruleListState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        "Hareketler",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                actions = {
                    Box(modifier = Modifier.padding(end = 12.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .clickable { showPresetMenu = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                activePreset?.let { preset ->
                                    RuleConfigViewModel.presets.indexOfFirst { it.first == preset }.takeIf { it >= 0 }?.let(presetNames::get)
                                } ?: stringResource(R.string.templates),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Kural ekle", tint = Color.White)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            // ── Glassmorphic Profile Selector ──
            AkisGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                onClick = { showProfileMenu = true },
                accentTint = MaterialTheme.colorScheme.primary,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val packageName = activeProfilePackage
                    if (packageName == null) {
                        Icon(
                            Icons.Filled.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        ActionIcon(
                            action = ActionNode.LaunchApp(packageName, activeProfileLabel),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            activeProfileLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            if (activeProfilePackage == null) stringResource(R.string.all_apps_gestures)
                            else stringResource(R.string.profile_only, activeProfileLabel),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = stringResource(R.string.change_profile),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ── Glassmorphic Edge Selector Tabs ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    Triple(Edge.LEFT, edgeLabel(context, Edge.LEFT), MaterialTheme.colorScheme.primary),
                    Triple(Edge.RIGHT, edgeLabel(context, Edge.RIGHT), MaterialTheme.colorScheme.secondary),
                    Triple(Edge.BOTTOM, edgeLabel(context, Edge.BOTTOM), MaterialTheme.colorScheme.tertiary),
                ).forEach { (edge, title, edgeColor) ->
                    val isSelected = selectedEdge == edge
                    val count = ruleGroups.count { it.representative.trigger.edge == edge }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) edgeColor.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) edgeColor else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable { selectedEdge = edge }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "$count Hareket",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) edgeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // ── Conflict Banner ──
            AnimatedVisibility(visible = conflicts.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(14.dp),
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
                            text = stringResource(R.string.conflict_count, conflicts.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            // ── Rule List ──
            if (rules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.no_rules),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    state = ruleListState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item(key = "header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(R.string.edge_areas, edgeLabel(context, selectedEdge)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { showMap = true }) {
                                Icon(Icons.Filled.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.map_frame), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    itemsIndexed(visibleGroups, key = { _, group -> group.key }) { index, group ->
                        RuleTableRow(
                            group = group,
                            number = index + 1,
                            onClick = { selectedGroupKey = group.key },
                            onDelete = { viewModel.removeRules(group.ids) },
                            onSelectAction = { gestureType, rule ->
                                selectedGroupKey = group.key
                                if (rule != null) {
                                    editingActionRuleId = rule.id
                                } else {
                                    addingGestureType = gestureType
                                }
                                openActionPicker()
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

    if (showPresetMenu) {
        AlertDialog(
            onDismissRequest = { showPresetMenu = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            title = {
                Column {
                    Text(stringResource(R.string.templates), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.templates_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 560.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    itemsIndexed(RuleConfigViewModel.presets) { index, (name, graph) ->
                        val templateColor = templateAccent(index)
                        DropdownMenuItem(
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(templateColor.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        (index + 1).toString(),
                                        color = templateColor,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            },
                            text = {
                                Column {
                                    Text(presetNames[index], color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        presetDescriptions[index],
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(templateColor.copy(alpha = 0.10f)),
                            onClick = {
                                viewModel.loadPreset(name, graph)
                                showPresetMenu = false
                            },
                        )
                    }
                }
            },
            confirmButton = {},
        )
    }

    // ── Modals & Dialogs ──

    if (showAddDialog) {
        io.github.omeryol.akisgesture.ui.component.AddRuleForEdgeDialog(
            edge = selectedEdge,
            onDismiss = { showAddDialog = false },
            onConfirm = { edge, section, quickAction, holdAction, lUpAction, lDownAction, triggerMode ->
                viewModel.addGesturePair(
                    edge = edge,
                    section = section,
                    quickAction = quickAction,
                    holdAction = holdAction,
                    lUpAction = lUpAction,
                    lDownAction = lDownAction,
                    triggerMode = triggerMode,
                )
                showAddDialog = false
            },
        )
    }

    if (showProfileMenu) {
        AlertDialog(
            onDismissRequest = { showProfileMenu = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            title = { Text(stringResource(R.string.gesture_profile), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                ) {
                    item(key = "general_profile") {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.general_layout), color = MaterialTheme.colorScheme.onSurface) },
                            supportingContent = { Text(stringResource(R.string.general_layout_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            leadingContent = {
                                Icon(
                                    Icons.Filled.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                            modifier = Modifier.clickable {
                                viewModel.selectProfile(null)
                                showProfileMenu = false
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileMenu = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }

    if (showMap) {
        AlertDialog(
            onDismissRequest = { showMap = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            title = { Text(stringResource(R.string.gesture_map), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
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
                TextButton(onClick = { showMap = false }) { Text(stringResource(R.string.done)) }
            },
        )
    }

    val selectedGroup = ruleGroups.firstOrNull { it.key == selectedGroupKey }
    if (selectedGroup != null) {
        AlertDialog(
            onDismissRequest = { selectedGroupKey = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EdgeZoneVisual(
                        edge = selectedGroup.representative.trigger.edge,
                        section = selectedGroup.representative.trigger.section,
                        zoneColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "${edgeLabel(context, selectedGroup.representative.trigger.edge)} · " +
                                sectionLabel(
                                    context,
                                    selectedGroup.representative.trigger.section,
                                    selectedGroup.representative.trigger.edge,
                                ),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.edit_area_gestures),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GestureSlotButton(
                        title = stringResource(R.string.quick_with_icon),
                        rule = selectedGroup.quick,
                        onClick = {
                            selectedGroup.quick?.let { editingActionRuleId = it.id }
                                ?: run { addingGestureType = GestureType.QUICK_SWIPE }
                            openActionPicker()
                        },
                        onClear = { selectedGroup.quick?.let { viewModel.removeRule(it.id) } },
                    )
                    GestureSlotButton(
                        title = stringResource(R.string.hold_with_icon),
                        rule = selectedGroup.hold,
                        onClick = {
                            selectedGroup.hold?.let { editingActionRuleId = it.id }
                                ?: run { addingGestureType = GestureType.SWIPE_HOLD }
                            openActionPicker()
                        },
                        onClear = { selectedGroup.hold?.let { viewModel.removeRule(it.id) } },
                    )
                    GestureSlotButton(
                        title = stringResource(R.string.l_up_with_icon),
                        rule = selectedGroup.lUp,
                        onClick = {
                            selectedGroup.lUp?.let { editingActionRuleId = it.id }
                                ?: run { addingGestureType = GestureType.SWIPE_UP_L }
                            openActionPicker()
                        },
                        onClear = { selectedGroup.lUp?.let { viewModel.removeRule(it.id) } },
                    )
                    GestureSlotButton(
                        title = stringResource(R.string.l_down_with_icon),
                        rule = selectedGroup.lDown,
                        onClick = {
                            selectedGroup.lDown?.let { editingActionRuleId = it.id }
                                ?: run { addingGestureType = GestureType.SWIPE_DOWN_L }
                            openActionPicker()
                        },
                        onClear = { selectedGroup.lDown?.let { viewModel.removeRule(it.id) } },
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
                    Text(stringResource(R.string.fine_tuning))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGroupKey = null }) {
                    Text(stringResource(R.string.finish))
                }
            },
        )
    }

}

private fun templateAccent(index: Int): Color = listOf(
    AkisPrimary, AkisSecondary, AkisTertiary,
    AkisPrimary.copy(alpha = 0.78f), AkisSecondary.copy(alpha = 0.78f), AkisTertiary.copy(alpha = 0.78f),
).getOrElse(index % 6) { AkisPrimary }

private data class RuleGroup(
    val quick: GestureRule?,
    val hold: GestureRule?,
    val lUp: GestureRule?,
    val lDown: GestureRule?,
) {
    val representative: GestureRule get() = quick ?: hold ?: lUp ?: requireNotNull(lDown)
    val ids: Set<String> get() = listOfNotNull(quick?.id, hold?.id, lUp?.id, lDown?.id).toSet()
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
    onClear: () -> Unit,
    iconPack: ActionIconPack = ActionIconPack.EMOJI_MODERN,
) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        rule?.let {
            ActionIcon(
                action = it.action,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(actionEmoji(it.action, iconPack), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(
                text = rule?.action?.localizedLabel(context) ?: stringResource(R.string.assign_action),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (rule == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
        if (rule == null) {
            Text(stringResource(R.string.add_symbol), color = MaterialTheme.colorScheme.primary)
        } else {
            IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.remove_action, title),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun RuleTableRow(
    group: RuleGroup,
    number: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSelectAction: (GestureType, GestureRule?) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
) {
    val rule = group.representative
    val enabled = listOfNotNull(group.quick, group.hold, group.lUp, group.lDown).any { it.enabled }
    val scheme = MaterialTheme.colorScheme
    val accent = listOf(
        scheme.primary, scheme.secondary, scheme.tertiary,
        scheme.primary.copy(alpha = 0.78f), scheme.secondary.copy(alpha = 0.78f), scheme.tertiary.copy(alpha = 0.78f),
    )[number.minus(1) % 6]
    var contentAlpha = if (enabled) 1f else 0.45f
    var menuOpen by remember { mutableStateOf(false) }

    AkisGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = contentAlpha },
        onClick = onClick,
        accentTint = if (enabled) scheme.primary else null,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = if (enabled) 0.24f else 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    number.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) accent else scheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            EdgeZoneVisual(
                edge = rule.trigger.edge,
                section = rule.trigger.section,
                zoneColor = if (enabled) accent else scheme.outline,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // 1. QUICK SWIPE (⚡ Hızlı Çekme)
                ActionCell(
                    badge = stringResource(R.string.quick_badge),
                    badgeBg = scheme.primary.copy(alpha = 0.16f),
                    badgeText = scheme.primary,
                    rule = group.quick,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectAction(GestureType.QUICK_SWIPE, group.quick) },
                )
                // 2. SWIPE & HOLD (⏱️ Çekip Beklet)
                ActionCell(
                    badge = stringResource(R.string.hold_badge),
                    badgeBg = scheme.secondary.copy(alpha = 0.16f),
                    badgeText = scheme.secondary,
                    rule = group.hold,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectAction(GestureType.SWIPE_HOLD, group.hold) },
                )
                // 3. L-SWIPE UP (↗️ L-Yukarı)
                ActionCell(
                    badge = stringResource(R.string.l_up_badge),
                    badgeBg = scheme.tertiary.copy(alpha = 0.16f),
                    badgeText = scheme.tertiary,
                    rule = group.lUp,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectAction(GestureType.SWIPE_UP_L, group.lUp) },
                )
                // 4. L-SWIPE DOWN (↘️ L-Aşağı)
                ActionCell(
                    badge = stringResource(R.string.l_down_badge),
                    badgeBg = scheme.primary.copy(alpha = 0.12f),
                    badgeText = scheme.primary,
                    rule = group.lDown,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectAction(GestureType.SWIPE_DOWN_L, group.lDown) },
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.MoreVert, stringResource(R.string.options), tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    modifier = Modifier.background(scheme.surface),
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(if (enabled) R.string.disable else R.string.enable)) },
                        onClick = { onToggleEnabled(!enabled); menuOpen = false },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.fine_tuning)) },
                        onClick = { menuOpen = false; onClick() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete), color = scheme.error) },
                        onClick = { menuOpen = false; onDelete() },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCell(
    badge: String,
    badgeBg: Color,
    badgeText: Color,
    rule: GestureRule?,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(scheme.surfaceVariant.copy(alpha = 0.42f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Glass Badge Tag
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeBg)
                .padding(horizontal = 6.dp, vertical = 3.dp),
        ) {
            Text(
                badge,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = badgeText,
            )
        }
        Spacer(Modifier.width(10.dp))
        if (rule != null) {
            ActionIcon(
                action = rule.action,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                rule.action.localizedLabel(context),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                "+ Eylem Ekle",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
