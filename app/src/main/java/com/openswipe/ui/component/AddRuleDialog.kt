package com.omer.akisgesture.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omer.akisgesture.model.ActionNode
import com.omer.akisgesture.model.GestureType
import com.omer.akisgesture.model.SectionRange
import com.omer.akisgesture.model.TriggerMode
import com.omer.akisgesture.model.TriggerNode
import com.omer.akisgesture.overlay.Edge
import com.omer.akisgesture.ui.theme.AkisGesturePrimary
import com.omer.akisgesture.ui.viewmodel.RuleConfigViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (Edge, SectionRange, ActionNode?, ActionNode?, TriggerMode) -> Unit,
) {
    // Compact flow: edge -> section -> both actions on one screen.
    var step by remember { mutableIntStateOf(0) }
    var selectedEdge by remember { mutableStateOf<Edge?>(null) }
    var selectedSection by remember { mutableStateOf<SectionRange?>(null) }
    var activeGesture by remember { mutableStateOf(GestureType.QUICK_SWIPE) }
    var quickAction by remember { mutableStateOf<ActionNode?>(null) }
    var holdAction by remember { mutableStateOf<ActionNode?>(null) }
    var selectedTriggerMode by remember { mutableStateOf(TriggerMode.SWIPE) }

    val stepTitles = listOf("Kenar seç", "Alan seç", "İki hareketi ayarla")

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
                    text = "Adım ${step + 1} / 3",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))

                when (step) {
                    0 -> EdgeSelector(
                        selected = selectedEdge,
                        onSelect = {
                            selectedEdge = it
                            selectedSection = SectionRange.ALL
                        },
                    )
                    1 -> SectionSelector(
                        edge = selectedEdge!!,
                        selected = selectedSection,
                        onSelect = { selectedSection = it },
                    )
                    2 -> {
                        Text(
                            "Bu alanın iki hareketini buradan birlikte ayarla.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = activeGesture == GestureType.QUICK_SWIPE,
                                onClick = { activeGesture = GestureType.QUICK_SWIPE },
                                label = {
                                    Column {
                                        Text("Hızlı çekme")
                                        Text(
                                            quickAction?.let { "✓ ${it.label}" } ?: "Eylem seç",
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                },
                            )
                            FilterChip(
                                selected = activeGesture == GestureType.SWIPE_HOLD,
                                onClick = { activeGesture = GestureType.SWIPE_HOLD },
                                label = {
                                    Column {
                                        Text("Çekip bekletme")
                                        Text(
                                            holdAction?.let { "✓ ${it.label}" } ?: "Eylem seç",
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                },
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (activeGesture == GestureType.QUICK_SWIPE)
                                "Hızlı çekme ne yapsın?"
                            else
                                "Çekip bekletme ne yapsın?",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        ActionSelector(
                            selected = if (activeGesture == GestureType.QUICK_SWIPE) quickAction else holdAction,
                            onSelect = {
                                if (activeGesture == GestureType.QUICK_SWIPE) quickAction = it
                                else holdAction = it
                            },
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
            if (step < 2) {
                Button(
                    onClick = {
                        when (step) {
                            0 -> if (selectedEdge != null) {
                                step++
                            }
                            1 -> if (selectedSection != null) step++
                        }
                    },
                    enabled = when (step) {
                        0 -> selectedEdge != null
                        1 -> selectedSection != null
                        else -> false
                    },
                ) {
                    Text("İleri")
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
                    Text("İki hareketi kaydet")
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
        "Konum ve uzunluk",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        if (edge == Edge.BOTTOM) "Sol ve sağ sınırı sürükleyin."
        else "Üst ve alt sınırı sürükleyin.",
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
        "${(range.start * 100).toInt()}% – ${(range.end * 100).toInt()}%",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionSelector(
    selected: ActionNode?,
    onSelect: (ActionNode) -> Unit,
) {
    ActionDropdownField(
        label = "Bu hareket ne yapsın?",
        selected = selected,
        onSelect = onSelect,
    )
}
