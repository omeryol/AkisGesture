package io.github.omeryol.akisgesture.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.R
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.model.TriggerMode
import io.github.omeryol.akisgesture.model.TriggerNode
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.ui.util.edgeLabel
import io.github.omeryol.akisgesture.ui.util.localizedLabel
import io.github.omeryol.akisgesture.navigation.InternalNavigationBus
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRuleForEdgeDialog(
    edge: Edge,
    onDismiss: () -> Unit,
    onConfirm: (Edge, SectionRange, ActionNode?, ActionNode?, ActionNode?, ActionNode?, TriggerMode) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedSection by remember { mutableStateOf<SectionRange>(SectionRange.ALL) }
    var actionPickerTarget by remember { mutableStateOf<GestureType?>(null) }
    var quickAction by remember { mutableStateOf<ActionNode?>(null) }
    var holdAction by remember { mutableStateOf<ActionNode?>(null) }
    var lUpAction by remember { mutableStateOf<ActionNode?>(null) }
    var lDownAction by remember { mutableStateOf<ActionNode?>(null) }
    var selectedTriggerMode by remember { mutableStateOf(TriggerMode.SWIPE) }
    var actionPickerToken by remember { mutableStateOf<String?>(null) }

    fun openActionPicker(target: GestureType) {
        actionPickerTarget = target
        val token = UUID.randomUUID().toString()
        actionPickerToken = token
        InternalNavigationBus.requestActionPicker(
            InternalNavigationBus.ActionPickerRequest(token),
        )
    }

    LaunchedEffect(actionPickerToken) {
        val token = actionPickerToken ?: return@LaunchedEffect
        InternalNavigationBus.actionPickerResults.collect { result ->
            if (result.token == token) {
                when (actionPickerTarget) {
                    GestureType.QUICK_SWIPE -> quickAction = result.action
                    GestureType.SWIPE_HOLD -> holdAction = result.action
                    GestureType.SWIPE_UP_L -> lUpAction = result.action
                    GestureType.SWIPE_DOWN_L -> lDownAction = result.action
                    null -> Unit
                }
                actionPickerTarget = null
                actionPickerToken = null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        title = {
            Column {
                Text(
                    "${edgeLabel(context, edge)} · Kural Ekle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.choose_area_intro),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    stringResource(R.string.assign_area_intro),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                SectionSelector(
                    edge = edge,
                    selected = selectedSection,
                    onSelect = { selectedSection = it },
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Hareket Eylemleri",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                ActionChoiceButton(
                    title = stringResource(R.string.quick_with_icon),
                    description = stringResource(R.string.quick_description),
                    action = quickAction,
                    onSelect = { openActionPicker(GestureType.QUICK_SWIPE) },
                    onClear = { quickAction = null },
                )
                Spacer(Modifier.height(8.dp))
                ActionChoiceButton(
                    title = stringResource(R.string.hold_with_icon),
                    description = stringResource(R.string.hold_description),
                    action = holdAction,
                    onSelect = { openActionPicker(GestureType.SWIPE_HOLD) },
                    onClear = { holdAction = null },
                )
                Spacer(Modifier.height(8.dp))
                ActionChoiceButton(
                    title = stringResource(if (edge == Edge.BOTTOM) R.string.l_right_with_icon else R.string.l_up_with_icon),
                    description = stringResource(if (edge == Edge.BOTTOM) R.string.l_right_description else R.string.l_up_description),
                    action = lUpAction,
                    onSelect = { openActionPicker(GestureType.SWIPE_UP_L) },
                    onClear = { lUpAction = null },
                )
                Spacer(Modifier.height(8.dp))
                ActionChoiceButton(
                    title = stringResource(if (edge == Edge.BOTTOM) R.string.l_left_with_icon else R.string.l_down_with_icon),
                    description = stringResource(if (edge == Edge.BOTTOM) R.string.l_left_description else R.string.l_down_description),
                    action = lDownAction,
                    onSelect = { openActionPicker(GestureType.SWIPE_DOWN_L) },
                    onClear = { lDownAction = null },
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.trigger_mode),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedTriggerMode == TriggerMode.SWIPE,
                        onClick = { selectedTriggerMode = TriggerMode.SWIPE },
                        label = { Text(stringResource(R.string.swipe_recommended)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        ),
                        border = if (selectedTriggerMode == TriggerMode.SWIPE) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    )
                    FilterChip(
                        selected = selectedTriggerMode == TriggerMode.TOUCH,
                        onClick = { selectedTriggerMode = TriggerMode.TOUCH },
                        label = { Text(stringResource(R.string.touch)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        ),
                        border = if (selectedTriggerMode == TriggerMode.TOUCH) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (quickAction != null || holdAction != null || lUpAction != null || lDownAction != null) {
                        onConfirm(
                            edge,
                            selectedSection,
                            quickAction,
                            holdAction,
                            lUpAction,
                            lDownAction,
                            selectedTriggerMode,
                        )
                    }
                },
                enabled = quickAction != null || holdAction != null || lUpAction != null || lDownAction != null,
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (Edge, SectionRange, ActionNode?, ActionNode?, ActionNode?, ActionNode?, TriggerMode) -> Unit,
) {
    // Compact flow: edge and area -> both actions.
    var step by remember { mutableIntStateOf(0) }
    var selectedEdge by remember { mutableStateOf<Edge?>(null) }
    var selectedSection by remember { mutableStateOf<SectionRange?>(null) }
    var actionPickerTarget by remember { mutableStateOf<GestureType?>(null) }
    var quickAction by remember { mutableStateOf<ActionNode?>(null) }
    var holdAction by remember { mutableStateOf<ActionNode?>(null) }
    var lUpAction by remember { mutableStateOf<ActionNode?>(null) }
    var lDownAction by remember { mutableStateOf<ActionNode?>(null) }
    var selectedTriggerMode by remember { mutableStateOf(TriggerMode.SWIPE) }
    var actionPickerToken by remember { mutableStateOf<String?>(null) }

    fun openActionPicker(target: GestureType) {
        actionPickerTarget = target
        val token = UUID.randomUUID().toString()
        actionPickerToken = token
        InternalNavigationBus.requestActionPicker(
            InternalNavigationBus.ActionPickerRequest(token),
        )
    }

    LaunchedEffect(actionPickerToken) {
        val token = actionPickerToken ?: return@LaunchedEffect
        InternalNavigationBus.actionPickerResults.collect { result ->
            if (result.token == token) {
                when (actionPickerTarget) {
                    GestureType.QUICK_SWIPE -> quickAction = result.action
                    GestureType.SWIPE_HOLD -> holdAction = result.action
                    GestureType.SWIPE_UP_L -> lUpAction = result.action
                    GestureType.SWIPE_DOWN_L -> lDownAction = result.action
                    null -> Unit
                }
                actionPickerTarget = null
                actionPickerToken = null
            }
        }
    }

    val stepTitles = listOf(stringResource(R.string.choose_area), stringResource(R.string.assign_gestures))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        title = {
            Column {
                Text(
                    "Kural ekle - ${stepTitles[step]}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.step_progress, step + 1, 2),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Step indicator
                Text(
                    text = stringResource(R.string.step_progress, step + 1, 2),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))

                when (step) {
                    0 -> {
                        EdgeSelector(
                            selected = selectedEdge,
                            onSelect = {
                                selectedEdge = it
                                selectedSection = SectionRange.ALL
                            },
                        )
                        selectedEdge?.let { edge ->
                            Spacer(Modifier.height(18.dp))
                            SectionSelector(
                                edge = edge,
                                selected = selectedSection,
                                onSelect = { selectedSection = it },
                            )
                        }
                    }
                    1 -> {
                        Text(
                            stringResource(R.string.assign_area_gestures),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(12.dp))
                        ActionChoiceButton(
                            title = stringResource(R.string.quick_with_icon),
                            description = stringResource(R.string.quick_description),
                            action = quickAction,
                            onSelect = {
                                openActionPicker(GestureType.QUICK_SWIPE)
                            },
                            onClear = { quickAction = null },
                        )
                        Spacer(Modifier.height(8.dp))
                        ActionChoiceButton(
                            title = stringResource(R.string.hold_with_icon),
                            description = stringResource(R.string.hold_description),
                            action = holdAction,
                            onSelect = {
                                openActionPicker(GestureType.SWIPE_HOLD)
                            },
                            onClear = { holdAction = null },
                        )
                        Spacer(Modifier.height(8.dp))
                        ActionChoiceButton(
                            title = stringResource(if (selectedEdge == Edge.BOTTOM) R.string.l_right_with_icon else R.string.l_up_with_icon),
                            description = stringResource(if (selectedEdge == Edge.BOTTOM) R.string.l_right_description else R.string.l_up_description),
                            action = lUpAction,
                            onSelect = {
                                openActionPicker(GestureType.SWIPE_UP_L)
                            },
                            onClear = { lUpAction = null },
                        )
                        Spacer(Modifier.height(8.dp))
                        ActionChoiceButton(
                            title = stringResource(if (selectedEdge == Edge.BOTTOM) R.string.l_left_with_icon else R.string.l_down_with_icon),
                            description = stringResource(if (selectedEdge == Edge.BOTTOM) R.string.l_left_description else R.string.l_down_description),
                            action = lDownAction,
                            onSelect = {
                                openActionPicker(GestureType.SWIPE_DOWN_L)
                            },
                            onClear = { lDownAction = null },
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.trigger_mode),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = selectedTriggerMode == TriggerMode.SWIPE,
                                onClick = { selectedTriggerMode = TriggerMode.SWIPE },
                                label = { Text(stringResource(R.string.swipe_recommended)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                ),
                                border = if (selectedTriggerMode == TriggerMode.SWIPE) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            )
                            FilterChip(
                                selected = selectedTriggerMode == TriggerMode.TOUCH,
                                onClick = { selectedTriggerMode = TriggerMode.TOUCH },
                                label = { Text(stringResource(R.string.touch)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                ),
                                border = if (selectedTriggerMode == TriggerMode.TOUCH) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (step == 0) {
                Button(
                    onClick = { step = 1 },
                    enabled = selectedEdge != null && selectedSection != null,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(stringResource(R.string.choose_gestures))
                }
            } else {
                Button(
                    onClick = {
                        if (selectedEdge != null && selectedSection != null &&
                            (quickAction != null || holdAction != null || lUpAction != null || lDownAction != null)
                        ) {
                            onConfirm(
                                selectedEdge!!,
                                selectedSection!!,
                                quickAction,
                                holdAction,
                                lUpAction,
                                lDownAction,
                                selectedTriggerMode,
                            )
                        }
                    },
                    enabled = quickAction != null || holdAction != null || lUpAction != null || lDownAction != null,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        },
        dismissButton = {
            if (step > 0) {
                OutlinedButton(onClick = { step-- }, shape = RoundedCornerShape(14.dp)) {
                    Text(stringResource(R.string.back))
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        },
    )

}
@Composable
private fun ActionChoiceButton(
    title: String,
    description: String,
    action: ActionNode?,
    onSelect: () -> Unit,
    onClear: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    OutlinedButton(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 14.dp,
            vertical = 12.dp,
        ),
    ) {
        if (action != null) {
            ActionIcon(
                action = action,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(
                action?.localizedLabel(context) ?: description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (action == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
        if (action != null) {
            IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.remove_action, title),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EdgeSelector(
    selected: Edge?,
    onSelect: (Edge) -> Unit,
) {
    val edges = listOf(
        Triple(Edge.LEFT, stringResource(R.string.edge_left_arrow), stringResource(R.string.edge_left_hint)),
        Triple(Edge.RIGHT, stringResource(R.string.edge_right_arrow), stringResource(R.string.edge_right_hint)),
        Triple(Edge.BOTTOM, stringResource(R.string.edge_bottom_arrow), stringResource(R.string.edge_bottom_hint)),
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
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                ),
                border = if (selected == edge) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
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
    val options = SectionRange.presets(edge)

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
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                ),
                border = if (selected == section) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    Text(
        stringResource(R.string.area_bounds),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        stringResource(if (edge == Edge.BOTTOM) R.string.drag_horizontal_bounds else R.string.drag_vertical_bounds),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val range = selected ?: SectionRange.ALL
    RangeSlider(
        value = range.start..range.end,
        onValueChange = { newRange ->
            val start = newRange.start.coerceIn(0f, 0.9f)
            val end = newRange.endInclusive.coerceIn(start + 0.1f, 1f)
            onSelect(SectionRange(start, end))
        },
        valueRange = 0f..1f,
        steps = 9,
    )
    Text(
        stringResource(R.string.selected_area_percent, (range.start * 100).toInt(), (range.end * 100).toInt()),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth(),
    )
}
