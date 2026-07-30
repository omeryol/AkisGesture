package com.omer.akisgesture.ui.component

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omer.akisgesture.model.ActionNode
import com.omer.akisgesture.model.GestureType
import com.omer.akisgesture.model.SectionRange
import com.omer.akisgesture.model.TriggerMode
import com.omer.akisgesture.model.TriggerNode
import com.omer.akisgesture.overlay.Edge
import com.omer.akisgesture.ui.theme.AkisGesturePrimary
import com.omer.akisgesture.ui.util.actionImageVector

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (Edge, SectionRange, ActionNode?, ActionNode?, TriggerMode) -> Unit,
) {
    // Compact flow: edge and area -> both actions.
    var step by remember { mutableIntStateOf(0) }
    var selectedEdge by remember { mutableStateOf<Edge?>(null) }
    var selectedSection by remember { mutableStateOf<SectionRange?>(null) }
    var actionPickerTarget by remember { mutableStateOf<GestureType?>(null) }
    var quickAction by remember { mutableStateOf<ActionNode?>(null) }
    var holdAction by remember { mutableStateOf<ActionNode?>(null) }
    var selectedTriggerMode by remember { mutableStateOf(TriggerMode.SWIPE) }

    val stepTitles = listOf("Alanı seç", "Hareketleri ata")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Kural ekle - ${stepTitles[step]}")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Step indicator
                Text(
                    text = "Adım ${step + 1} / 2",
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
                            "Kısa çekme ve bekletme hareketlerini aynı alana ata.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(12.dp))
                        ActionChoiceButton(
                            title = "Hızlı çekme",
                            description = "Parmağını çekip hemen bıraktığında",
                            action = quickAction,
                            onSelect = {
                                actionPickerTarget = GestureType.QUICK_SWIPE
                            },
                            onClear = { quickAction = null },
                        )
                        Spacer(Modifier.height(8.dp))
                        ActionChoiceButton(
                            title = "Çekip bekletme",
                            description = "Eşik dolduktan sonra bıraktığında",
                            action = holdAction,
                            onSelect = {
                                actionPickerTarget = GestureType.SWIPE_HOLD
                            },
                            onClear = { holdAction = null },
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Tetikleme biçimi",
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
                                label = { Text("Kaydırma (önerilen)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AkisGesturePrimary.copy(alpha = 0.15f),
                                ),
                                border = if (selectedTriggerMode == TriggerMode.SWIPE) BorderStroke(1.dp, AkisGesturePrimary) else null,
                            )
                            FilterChip(
                                selected = selectedTriggerMode == TriggerMode.TOUCH,
                                onClick = { selectedTriggerMode = TriggerMode.TOUCH },
                                label = { Text("Dokunma") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AkisGesturePrimary.copy(alpha = 0.15f),
                                ),
                                border = if (selectedTriggerMode == TriggerMode.TOUCH) BorderStroke(1.dp, AkisGesturePrimary) else null,
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
                ) {
                    Text("Hareketleri seç")
                }
            } else {
                Button(
                    onClick = {
                        if (selectedEdge != null && selectedSection != null &&
                            (quickAction != null || holdAction != null)
                        ) {
                            onConfirm(
                                selectedEdge!!,
                                selectedSection!!,
                                quickAction,
                                holdAction,
                                selectedTriggerMode,
                            )
                        }
                    },
                    enabled = quickAction != null || holdAction != null,
                ) {
                    Text("Kaydet")
                }
            }
        },
        dismissButton = {
            if (step > 0) {
                OutlinedButton(onClick = { step-- }) {
                    Text("Geri")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("İptal")
                }
            }
        },
    )

    actionPickerTarget?.let { target ->
        ActionPickerDialog(
            onDismiss = { actionPickerTarget = null },
            onSelect = { action ->
                if (target == GestureType.QUICK_SWIPE) {
                    quickAction = action
                } else {
                    holdAction = action
                }
                actionPickerTarget = null
            },
        )
    }
}
@Composable
private fun ActionChoiceButton(
    title: String,
    description: String,
    action: ActionNode?,
    onSelect: () -> Unit,
    onClear: () -> Unit,
) {
    OutlinedButton(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 14.dp,
            vertical = 12.dp,
        ),
    ) {
        Icon(
            imageVector = action?.let(::actionImageVector) ?: Icons.Filled.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.size(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(
                action?.label ?: description,
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
                    contentDescription = "$title eylemini kaldır",
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
        Triple(Edge.LEFT, "\u2190 Sol kenar", "Sol kenardan içeri kaydır"),
        Triple(Edge.RIGHT, "\u2192 Sağ kenar", "Sağ kenardan içeri kaydır"),
        Triple(Edge.BOTTOM, "\u2193 Alt kenar", "Alt kenardan yukarı kaydır"),
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
                    selectedContainerColor = AkisGesturePrimary.copy(alpha = 0.15f),
                ),
                border = if (selected == edge) BorderStroke(1.dp, AkisGesturePrimary) else null,
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
                    selectedContainerColor = AkisGesturePrimary.copy(alpha = 0.15f),
                ),
                border = if (selected == section) BorderStroke(1.dp, AkisGesturePrimary) else null,
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    Text(
        "Alan sınırları",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        if (edge == Edge.BOTTOM) "Başlangıç ve bitiş noktasını sürükle."
        else "Üst ve alt sınırı sürükle.",
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
        "Seçili alan: %${(range.start * 100).toInt()} – %${(range.end * 100).toInt()}",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth(),
    )
}
