package com.omer.akisgesture.ui.screen

import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.omer.akisgesture.feedback.FeedbackAnimation
import com.omer.akisgesture.feedback.FeedbackIcon
import com.omer.akisgesture.ui.viewmodel.HomeViewModel
import com.omer.akisgesture.ui.viewmodel.RootAccessState
import kotlin.math.roundToInt

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
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "Hareketlerin hissini ve görünümünü buradan ayarla.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )

        SettingsSection(
            title = "Hareket hissi",
            summary = "Kenar genişliği, alt alan ve bekleme süresi",
            icon = Icons.Filled.Swipe,
            initiallyExpanded = true,
        ) {
            ValueSlider(
                title = "Yan kenar genişliği",
                valueText = "${config.edgeTriggerWidthDp.roundToInt()} dp",
                value = config.edgeTriggerWidthDp,
                range = 10f..50f,
                onValueChange = viewModel::setEdgeTriggerWidth,
            )
            ValueSlider(
                title = "Alt kenar yüksekliği",
                valueText = "${config.bottomTriggerHeightDp.roundToInt()} dp",
                value = config.bottomTriggerHeightDp,
                range = 20f..80f,
                onValueChange = viewModel::setBottomTriggerHeight,
            )
            ValueSlider(
                title = "Çekip bekletme",
                valueText = "${config.holdTimeMs} ms",
                value = config.holdTimeMs.toFloat(),
                range = 150f..700f,
                onValueChange = { viewModel.setHoldTime(it.roundToInt().toLong()) },
            )
        }

        SettingsSection(
            title = "Görünüm",
            summary = "${config.feedbackAnimation.label} · %${(config.feedbackOpacity * 100).roundToInt()} görünürlük",
            icon = Icons.Filled.Palette,
        ) {
            ChoiceDropdown(
                title = "Animasyon",
                selected = config.feedbackAnimation,
                options = FeedbackAnimation.entries,
                label = { it.label },
                onSelect = viewModel::setFeedbackAnimation,
            )
            ChoiceDropdown(
                title = "Hızlı çekme simgesi",
                selected = config.quickFeedbackIcon,
                options = FeedbackIcon.entries,
                label = { iconLabel(it) },
                onSelect = viewModel::setQuickFeedbackIcon,
            )
            ChoiceDropdown(
                title = "Bekletme simgesi",
                selected = config.holdFeedbackIcon,
                options = FeedbackIcon.entries,
                label = { iconLabel(it) },
                onSelect = viewModel::setHoldFeedbackIcon,
            )
            ValueSlider(
                title = "Saydamlık",
                valueText = "%${(config.feedbackOpacity * 100).roundToInt()}",
                value = config.feedbackOpacity,
                range = 0.1f..1f,
                onValueChange = viewModel::setFeedbackOpacity,
            )
            ColorControls(
                argb = config.feedbackColorArgb,
                onColorChange = viewModel::setFeedbackColor,
            )
        }

        SettingsSection(
            title = "Çalışmayacağı yerler",
            summary = buildList {
                if (config.pauseOnLockScreen) add("kilit ekranı")
                if (config.pauseWhenKeyboardVisible) add("klavye")
                if (config.pauseInLandscape) add("yatay ekran")
                if (pausedPackages.isNotEmpty()) add("${pausedPackages.size} uygulama")
            }.joinToString(" · ").ifEmpty { "Her yerde etkin" },
            icon = Icons.Filled.Block,
        ) {
            SwitchSetting(
                title = "Kilit ekranında",
                description = "Telefon kilitliyken kenar hareketlerini kapat",
                checked = config.pauseOnLockScreen,
                onCheckedChange = viewModel::setPauseOnLockScreen,
            )
            SwitchSetting(
                title = "Klavye açıkken",
                description = "Yazı yazarken yanlış dokunmaları önle",
                checked = config.pauseWhenKeyboardVisible,
                onCheckedChange = viewModel::setPauseWhenKeyboardVisible,
            )
            SwitchSetting(
                title = "Yatay ekranda",
                description = "Oyun ve video görünümünde hareketleri kapat",
                checked = config.pauseInLandscape,
                onCheckedChange = viewModel::setPauseInLandscape,
            )
            Text(
                if (pausedPackages.isEmpty()) {
                    "Uygulamaya özel bir engel yok."
                } else {
                    "${pausedPackages.size} uygulamada hareketler çalışmayacak."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = { showAppPicker = true }) {
                Icon(Icons.Filled.Apps, contentDescription = null)
                Text("  Uygulamaları seç")
            }
        }

        SettingsSection(
            title = "Gelişmiş",
            summary = when (rootAccess) {
                RootAccessState.CHECKING -> "Root denetleniyor"
                RootAccessState.AVAILABLE -> "Root hazır"
                RootAccessState.UNAVAILABLE -> "Root kullanılamıyor"
            },
            icon = Icons.Filled.Security,
        ) {
            Text(
                "Zorla durdurma yalnızca kişisel profilde çalışır. Sistem uygulamaları korunur.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = viewModel::checkRootAccess) {
                Text("Root durumunu yenile")
            }
        }
    }

    if (showAppPicker) {
        AlertDialog(
            onDismissRequest = { showAppPicker = false },
            title = { Text("Duraklatılacak uygulamalar") },
            text = {
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
                    items(selectableApps, key = { it.packageName }) { app ->
                        ListItem(
                            headlineContent = { Text(app.label) },
                            supportingContent = { Text(app.packageName) },
                            leadingContent = {
                                Checkbox(
                                    checked = app.packageName in pausedPackages,
                                    onCheckedChange = {
                                        viewModel.setPackagePaused(app.packageName, it)
                                    },
                                )
                            },
                            modifier = Modifier.clickable {
                                viewModel.setPackagePaused(
                                    app.packageName,
                                    app.packageName !in pausedPackages,
                                )
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppPicker = false }) { Text("Tamam") }
            },
        )
    }
}

@Composable
private fun SwitchSetting(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) },
    )
}

@Composable
private fun SettingsSection(
    title: String,
    summary: String,
    icon: ImageVector,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(summary, maxLines = 1) },
            leadingContent = {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingContent = {
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Daralt" else "Aç",
                )
            },
            modifier = Modifier.clickable { expanded = !expanded },
        )
        AnimatedVisibility(expanded) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun ValueSlider(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(valueText, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ChoiceDropdown(
    title: String,
    selected: T,
    options: List<T>,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        ListItem(
            headlineContent = { Text(label(selected)) },
            supportingContent = { Text(title) },
            trailingContent = { Icon(Icons.Filled.ExpandMore, contentDescription = null) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ColorControls(
    argb: Int,
    onColorChange: (Int) -> Unit,
) {
    val hsv = FloatArray(3).also { AndroidColor.colorToHSV(argb, it) }
    fun update(h: Float = hsv[0], s: Float = hsv[1], v: Float = hsv[2]) {
        onColorChange(AndroidColor.HSVToColor(floatArrayOf(h, s, v)))
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Renk", style = MaterialTheme.typography.bodyMedium)
            Box(
                Modifier
                    .height(28.dp)
                    .fillMaxWidth(0.35f)
                    .background(Color(argb), CircleShape),
            )
        }
        Text("Renk tonu · ${hsv[0].roundToInt()}°", style = MaterialTheme.typography.bodySmall)
        Slider(value = hsv[0], onValueChange = { update(h = it) }, valueRange = 0f..360f)
        Text("Canlılık · %${(hsv[1] * 100).roundToInt()}", style = MaterialTheme.typography.bodySmall)
        Slider(value = hsv[1], onValueChange = { update(s = it) }, valueRange = 0f..1f)
        Text("Parlaklık · %${(hsv[2] * 100).roundToInt()}", style = MaterialTheme.typography.bodySmall)
        Slider(value = hsv[2], onValueChange = { update(v = it) }, valueRange = 0f..1f)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onColorChange(0xFF000000.toInt()) }) { Text("Siyah") }
            TextButton(onClick = { onColorChange(0xFFFFFFFF.toInt()) }) { Text("Beyaz") }
            TextButton(onClick = { onColorChange(0xFF3D5AFE.toInt()) }) { Text("Mavi") }
        }
    }
}

private fun iconLabel(icon: FeedbackIcon): String =
    if (icon.symbol.isBlank()) icon.label else "${icon.symbol}  ${icon.label}"
