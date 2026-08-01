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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.ui.component.ActionIcon
import io.github.omeryol.akisgesture.ui.component.ActionPickerDialog
import io.github.omeryol.akisgesture.ui.component.AddRuleDialog
import io.github.omeryol.akisgesture.ui.component.AkisGlassCard
import io.github.omeryol.akisgesture.ui.component.EdgeZoneVisual
import io.github.omeryol.akisgesture.ui.component.GestureMapCard
import io.github.omeryol.akisgesture.ui.util.appLabel
import io.github.omeryol.akisgesture.ui.util.edgeLabel
import io.github.omeryol.akisgesture.ui.util.sectionLabel
import io.github.omeryol.akisgesture.ui.viewmodel.RuleConfigViewModel
import io.github.omeryol.akisgesture.rule.Presets

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

    val activeProfileLabel = remember(activeProfilePackage, context) {
        activeProfilePackage?.let { appLabel(context, it) } ?: "Genel Düzen"
    }

    var selectedEdge by remember(initialEdge) { mutableStateOf(initialEdge) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }
    var selectedGroupKey by remember { mutableStateOf<String?>(null) }
    var editingActionRuleId by remember { mutableStateOf<String?>(null) }
    var addingGestureType by remember { mutableStateOf<GestureType?>(null) }
    var showPresetMenu by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }
    var showProfileAppPicker by remember { mutableStateOf(false) }
    var deleteProfilePackage by remember { mutableStateOf<String?>(null) }

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
    val visibleGroups = ruleGroups.filter { it.representative.trigger.edge == selectedEdge }
    val ruleListState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Hareketler",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xEE0D0F18),
                    titleContentColor = Color.White,
                ),
                actions = {
                    Box {
                        TextButton(onClick = { showPresetMenu = true }) {
                            Text(
                                activePreset ?: "Şablonlar",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF00E5FF),
                            )
                        }
                        DropdownMenu(
                            expanded = showPresetMenu,
                            onDismissRequest = { showPresetMenu = false },
                            modifier = Modifier.background(Color(0xEE161827)),
                        ) {
                            RuleConfigViewModel.presets.forEach { (name, graph) ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                name,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Text(
                                                Presets.DESCRIPTIONS[name].orEmpty(),
                                                color = Color(0xFFB7B9C9),
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.loadPreset(name, graph)
                                        showPresetMenu = false
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF3D5AFE),
                contentColor = Color.White,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Kural ekle")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090A0F))
                .padding(innerPadding),
        ) {
            // ── Glassmorphic Profile Selector ──
            AkisGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                onClick = { showProfileMenu = true },
                accentTint = Color(0xFF3D5AFE),
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
                            tint = Color(0xFF00E5FF),
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
                            if (activeProfilePackage == null) "Tüm uygulamalarda kullanılan hareketler"
                            else "Yalnızca $activeProfileLabel öndeyken kullanılır",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E92B0),
                        )
                    }
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = "Profili değiştir",
                        tint = Color(0xFF8E92B0),
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
                    Triple(Edge.LEFT, "Sol Kenar", Color(0xFF3D5AFE)),
                    Triple(Edge.RIGHT, "Sağ Kenar", Color(0xFFD500F9)),
                    Triple(Edge.BOTTOM, "Alt Kenar", Color(0xFFFF9100)),
                ).forEach { (edge, title, edgeColor) ->
                    val isSelected = selectedEdge == edge
                    val count = ruleGroups.count { it.representative.trigger.edge == edge }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) edgeColor.copy(alpha = 0.25f)
                                else Color(0x1A202438),
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) edgeColor else Color(0x20FFFFFF),
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
                                color = if (isSelected) Color.White else Color(0xFF8E92B0),
                            )
                            Text(
                                "$count Hareket",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) edgeColor else Color(0x66FFFFFF),
                            )
                        }
                    }
                }
            }

            // ── Conflict Banner ──
            AnimatedVisibility(visible = conflicts.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3D0C15)),
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
                            tint = Color(0xFFFF1744),
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${conflicts.size} çakışma var",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF80AB),
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
                        text = "Henüz kural yok. Hazır bir düzen seçin veya kural ekleyin.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF8E92B0),
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
                                "${edgeLabel(selectedEdge)} Bölgeleri",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { showMap = true }) {
                                Icon(Icons.Filled.Map, contentDescription = null, tint = Color(0xFF00E5FF))
                                Spacer(Modifier.width(4.dp))
                                Text("Harita Çerçevesi", color = Color(0xFF00E5FF))
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
            containerColor = Color(0xEE161827),
            shape = RoundedCornerShape(22.dp),
            title = { Text("Hareket Profili", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                ) {
                    item(key = "general_profile") {
                        ListItem(
                            headlineContent = { Text("Genel Düzen", color = Color.White) },
                            supportingContent = { Text("Diğer tüm uygulamalarda kullanılır", color = Color(0xFF8E92B0)) },
                            leadingContent = {
                                Icon(
                                    Icons.Filled.Apps,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
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
                TextButton(onClick = { showProfileMenu = false }) { Text("Kapat", color = Color(0xFF00E5FF)) }
            },
        )
    }

    if (showMap) {
        AlertDialog(
            onDismissRequest = { showMap = false },
            containerColor = Color(0xEE161827),
            shape = RoundedCornerShape(22.dp),
            title = { Text("Hareket Haritası", color = Color.White, fontWeight = FontWeight.Bold) },
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
                TextButton(onClick = { showMap = false }) { Text("Tamam", color = Color(0xFF00E5FF)) }
            },
        )
    }

    val selectedGroup = ruleGroups.firstOrNull { it.key == selectedGroupKey }
    if (selectedGroup != null) {
        AlertDialog(
            onDismissRequest = { selectedGroupKey = null },
            containerColor = Color(0xEE161827),
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    "${edgeLabel(selectedGroup.representative.trigger.edge)} · " +
                        sectionLabel(
                            selectedGroup.representative.trigger.section,
                            selectedGroup.representative.trigger.edge,
                        ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Bu alan için hareketleri düzenle (Hızlı, Bekletme, L-Çekme)",
                        color = Color(0xFF8E92B0),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    GestureSlotButton(
                        title = "⚡ Hızlı Çekme",
                        rule = selectedGroup.quick,
                        onClick = {
                            selectedGroup.quick?.let { editingActionRuleId = it.id }
                                ?: run { addingGestureType = GestureType.QUICK_SWIPE }
                        },
                    )
                    GestureSlotButton(
                        title = "⏱️ Çekip Bekletme",
                        rule = selectedGroup.hold,
                        onClick = {
                            selectedGroup.hold?.let { editingActionRuleId = it.id }
                                ?: run { addingGestureType = GestureType.SWIPE_HOLD }
                        },
                    )
                    GestureSlotButton(
                        title = "↗️ L-Çekme (Yukarı)",
                        rule = selectedGroup.lUp,
                        onClick = {
                            selectedGroup.lUp?.let { editingActionRuleId = it.id }
                                ?: run { addingGestureType = GestureType.SWIPE_UP_L }
                        },
                    )
                    GestureSlotButton(
                        title = "↘️ L-Çekme (Aşağı)",
                        rule = selectedGroup.lDown,
                        onClick = {
                            selectedGroup.lDown?.let { editingActionRuleId = it.id }
                                ?: run { addingGestureType = GestureType.SWIPE_DOWN_L }
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
                    Text("İnce Ayarlar", color = Color(0xFF00E5FF))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGroupKey = null }) {
                    Text("Bitti", color = Color(0xFF8E92B0))
                }
            },
        )
    }

    editingActionRuleId?.let { ruleId ->
        val rule = rules.firstOrNull { it.id == ruleId }
        ActionPickerDialog(
            onDismiss = { editingActionRuleId = null },
            onSelect = { action ->
                if (rule != null) {
                    viewModel.updateRuleAction(rule.id, action)
                }
                editingActionRuleId = null
            },
        )
    }

    addingGestureType?.let { gestureType ->
        val key = selectedGroupKey
        val repRule = ruleGroups.firstOrNull { it.key == key }?.representative
        if (repRule != null) {
            ActionPickerDialog(
                onDismiss = { addingGestureType = null },
                onSelect = { action ->
                    viewModel.addGesturePair(
                        edge = repRule.trigger.edge,
                        section = repRule.trigger.section,
                        quickAction = if (gestureType == GestureType.QUICK_SWIPE) action else null,
                        holdAction = if (gestureType == GestureType.SWIPE_HOLD) action else null,
                        lUpAction = if (gestureType == GestureType.SWIPE_UP_L) action else null,
                        lDownAction = if (gestureType == GestureType.SWIPE_DOWN_L) action else null,
                        triggerMode = repRule.triggerMode,
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
) {
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
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            Text(
                text = rule?.action?.label ?: "Eylem ata",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (rule == null) Color(0xFF3D5AFE) else Color.White,
            )
        }
        Text(if (rule == null) "+" else "Değiştir", color = Color(0xFF00E5FF))
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
    val accent = listOf(
        Color(0xFF5B8CFF), Color(0xFFFF6B6B), Color(0xFFFFB74D),
        Color(0xFF4DD0E1), Color(0xFFB39DDB), Color(0xFF66BB6A),
    )[number.minus(1) % 6]
    var contentAlpha = if (enabled) 1f else 0.45f
    var menuOpen by remember { mutableStateOf(false) }

    AkisGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = contentAlpha },
        onClick = onClick,
        accentTint = if (enabled) Color(0xFF3D5AFE) else null,
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
                    color = if (enabled) accent else Color(0xFF6F738A),
                )
            }
            Spacer(Modifier.width(8.dp))
            EdgeZoneVisual(
                edge = rule.trigger.edge,
                section = rule.trigger.section,
                zoneColor = if (enabled) accent else Color(0xFF4A4E69),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // 1. QUICK SWIPE (⚡ Hızlı Çekme)
                ActionCell(
                    badge = "⚡ Hızlı",
                    badgeBg = Color(0x333D5AFE),
                    badgeText = Color(0xFF82B1FF),
                    rule = group.quick,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectAction(GestureType.QUICK_SWIPE, group.quick) },
                )
                // 2. SWIPE & HOLD (⏱️ Çekip Beklet)
                ActionCell(
                    badge = "⏱️ Beklet",
                    badgeBg = Color(0x3300E5FF),
                    badgeText = Color(0xFF84FFFF),
                    rule = group.hold,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectAction(GestureType.SWIPE_HOLD, group.hold) },
                )
                // 3. L-SWIPE UP (↗️ L-Yukarı)
                ActionCell(
                    badge = "↗️ L-Yukarı",
                    badgeBg = Color(0x33FF9100),
                    badgeText = Color(0xFFFFD180),
                    rule = group.lUp,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectAction(GestureType.SWIPE_UP_L, group.lUp) },
                )
                // 4. L-SWIPE DOWN (↘️ L-Aşağı)
                ActionCell(
                    badge = "↘️ L-Aşağı",
                    badgeBg = Color(0x33D500F9),
                    badgeText = Color(0xFFEA80FC),
                    rule = group.lDown,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectAction(GestureType.SWIPE_DOWN_L, group.lDown) },
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.MoreVert, "Seçenekler", tint = Color(0xFF8E92B0), modifier = Modifier.size(20.dp))
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    modifier = Modifier.background(Color(0xEE161827)),
                ) {
                    DropdownMenuItem(
                        text = { Text(if (enabled) "Devre dışı bırak" else "Etkinleştir", color = Color.White) },
                        onClick = { onToggleEnabled(!enabled); menuOpen = false },
                    )
                    DropdownMenuItem(
                        text = { Text("İnce ayarlar", color = Color.White) },
                        onClick = { menuOpen = false; onClick() },
                    )
                    DropdownMenuItem(
                        text = { Text("Sil", color = Color(0xFFFF1744)) },
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
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x18FFFFFF))
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
                rule.action.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                "+ Eylem Ekle",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
