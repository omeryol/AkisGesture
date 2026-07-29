package com.omer.akisgesture.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.graphics.Color as AndroidColor
import com.omer.akisgesture.ui.viewmodel.HomeViewModel
import com.omer.akisgesture.ui.viewmodel.RootAccessState
import com.omer.akisgesture.feedback.FeedbackAnimation
import com.omer.akisgesture.feedback.FeedbackIcon
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val config by viewModel.configState.collectAsState()
    val rootAccess by viewModel.rootAccess.collectAsState()
    val pausedPackages by viewModel.pausedPackages.collectAsState()
    val selectableApps by viewModel.selectableApps.collectAsState()
    var showAppPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── Edge trigger width settings ──
        Text(
            text = "Yan kenar genişliği",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = "Algılama genişliği", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "10", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = config.edgeTriggerWidthDp,
                        onValueChange = { viewModel.setEdgeTriggerWidth(it) },
                        valueRange = 10f..50f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                    )
                    Text(text = "50", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "${config.edgeTriggerWidthDp.roundToInt()}dp",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Bottom trigger settings (kept) ──
        Text(
            text = "Alt kenar alanı",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Height slider
                Text(text = "Algılama yüksekliği", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "20", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = config.bottomTriggerHeightDp,
                        onValueChange = { viewModel.setBottomTriggerHeight(it) },
                        valueRange = 20f..80f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                    )
                    Text(text = "80", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "${config.bottomTriggerHeightDp.roundToInt()}dp",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }

        Text(
            text = "Uygulamaya göre davranış",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Hareketlerin duracağı uygulamalar",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (pausedPackages.isEmpty())
                        "Hiçbir uygulamada duraklatılmıyor."
                    else
                        "${pausedPackages.size} uygulamada otomatik duraklatılıyor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = { showAppPicker = true }) {
                    Text("Uygulamaları seç")
                }
            }
        }

        Text(
            text = "Hareket görünümü",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Animasyon rengi", style = MaterialTheme.typography.titleMedium)
                val hsv = FloatArray(3).also {
                    AndroidColor.colorToHSV(config.feedbackColorArgb, it)
                }
                fun updateColor(hue: Float = hsv[0], saturation: Float = hsv[1], value: Float = hsv[2]) {
                    viewModel.setFeedbackColor(
                        AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value)),
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(Color(config.feedbackColorArgb), CircleShape),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setFeedbackColor(0xFF000000.toInt()) },
                    ) {
                        Text("Siyah")
                    }
                    OutlinedButton(
                        onClick = { viewModel.setFeedbackColor(0xFFFFFFFF.toInt()) },
                    ) {
                        Text("Beyaz")
                    }
                    OutlinedButton(
                        onClick = { viewModel.setFeedbackColor(0xFF3D5AFE.toInt()) },
                    ) {
                        Text("Varsayılan mavi")
                    }
                }
                Text("Renk tonu · ${hsv[0].roundToInt()}°")
                Slider(
                    value = hsv[0],
                    onValueChange = { updateColor(hue = it) },
                    valueRange = 0f..360f,
                )
                Text("Canlılık · %${(hsv[1] * 100).roundToInt()}")
                Slider(
                    value = hsv[1],
                    onValueChange = { updateColor(saturation = it) },
                    valueRange = 0f..1f,
                )
                Text("Parlaklık · %${(hsv[2] * 100).roundToInt()}")
                Slider(
                    value = hsv[2],
                    onValueChange = { updateColor(value = it) },
                    valueRange = 0f..1f,
                )
                Text("Saydamlık", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = config.feedbackOpacity,
                    onValueChange = viewModel::setFeedbackOpacity,
                    valueRange = 0.1f..1f,
                    steps = 8,
                )
                Text(
                    "%${(config.feedbackOpacity * 100).roundToInt()} görünürlük",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Text("Animasyon biçimi", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FeedbackAnimation.entries.forEach { animation ->
                        FilterChip(
                            selected = config.feedbackAnimation == animation,
                            onClick = { viewModel.setFeedbackAnimation(animation) },
                            label = { Text(animation.label) },
                        )
                    }
                }
                Text("Hızlı çekme simgesi", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FeedbackIcon.entries.forEach { icon ->
                        FilterChip(
                            selected = config.quickFeedbackIcon == icon,
                            onClick = { viewModel.setQuickFeedbackIcon(icon) },
                            label = {
                                Text(
                                    if (icon.symbol.isBlank()) icon.label
                                    else "${icon.symbol} ${icon.label}",
                                )
                            },
                        )
                    }
                }
                Text("Çekip bekletme simgesi", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FeedbackIcon.entries.forEach { icon ->
                        FilterChip(
                            selected = config.holdFeedbackIcon == icon,
                            onClick = { viewModel.setHoldFeedbackIcon(icon) },
                            label = {
                                Text(
                                    if (icon.symbol.isBlank()) icon.label
                                    else "${icon.symbol} ${icon.label}",
                                )
                            },
                        )
                    }
                }
            }
        }

        Text(
            text = "Çekip bekletme",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Bekleme süresi",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Kısa süre daha hızlı, uzun süre yanlış tetiklemeye karşı daha güvenlidir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = config.holdTimeMs.toFloat(),
                    onValueChange = { viewModel.setHoldTime(it.roundToInt().toLong()) },
                    valueRange = 150f..700f,
                    steps = 10,
                )
                Text(
                    text = "${config.holdTimeMs} ms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }

        Text(
            text = "Root erişimi",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (rootAccess) {
                            RootAccessState.CHECKING -> "Kontrol ediliyor"
                            RootAccessState.AVAILABLE -> "Root hazır"
                            RootAccessState.UNAVAILABLE -> "Root kullanılamıyor"
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Zorla durdurma yalnızca kişisel profilde çalışır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(onClick = viewModel::checkRootAccess) {
                    Text("Yenile")
                }
            }
        }
    }

    if (showAppPicker) {
        AlertDialog(
            onDismissRequest = { showAppPicker = false },
            title = { Text("Hareketlerin duracağı uygulamalar") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
                    items(selectableApps, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = app.packageName in pausedPackages,
                                onCheckedChange = {
                                    viewModel.setPackagePaused(app.packageName, it)
                                },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.label, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    app.packageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppPicker = false }) {
                    Text("Tamam")
                }
            },
        )
    }
}
