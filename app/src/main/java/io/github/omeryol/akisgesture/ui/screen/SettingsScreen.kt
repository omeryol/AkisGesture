package io.github.omeryol.akisgesture.ui.screen

import android.graphics.Color as AndroidColor
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Swipe
import io.github.omeryol.akisgesture.model.ActionIconPack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.AkisGestureApp
import io.github.omeryol.akisgesture.R
import io.github.omeryol.akisgesture.backup.SettingsBackupManager
import io.github.omeryol.akisgesture.feedback.FeedbackAnimation
import io.github.omeryol.akisgesture.gesture.HoldFireMode
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.service.GestureAccessibilityService
import io.github.omeryol.akisgesture.ui.component.AkisFluidSlider
import io.github.omeryol.akisgesture.ui.component.AkisFluidSwitch
import io.github.omeryol.akisgesture.ui.component.AkisGlassCard
import io.github.omeryol.akisgesture.ui.component.AkisSectionHeader
import io.github.omeryol.akisgesture.ui.component.AkisSliderRow
import io.github.omeryol.akisgesture.ui.component.AkisSwitchRow
import io.github.omeryol.akisgesture.ui.viewmodel.HomeViewModel
import io.github.omeryol.akisgesture.ui.viewmodel.RootAccessState
import io.github.omeryol.akisgesture.ui.util.edgeLabel
import io.github.omeryol.akisgesture.ui.util.localizedLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val config by viewModel.configState.collectAsState()
    val serviceState by GestureAccessibilityService.serviceState.collectAsState()
    val rootAccess by viewModel.rootAccess.collectAsState()
    val pausedPackages by viewModel.pausedPackages.collectAsState()
    val selectableApps by viewModel.selectableApps.collectAsState()

    var showAppPicker by remember { mutableStateOf(false) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var selectedEdge by remember { mutableStateOf(Edge.LEFT) }
    var selectedSection by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val app = context.applicationContext as AkisGestureApp
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme
    val versionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty().ifBlank { "Bilinmiyor" }
    }

    val exportBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val json = SettingsBackupManager.export(app)
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                        it.write(json)
                    } ?: error(context.getString(R.string.file_open_failed))
                }
            }.onSuccess {
                Toast.makeText(context, context.getString(R.string.backup_saved), Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, it.message ?: context.getString(R.string.backup_save_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    val importBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                        it.readText()
                    } ?: error(context.getString(R.string.file_open_failed))
                }
                pendingImportJson = json
            }.onFailure {
                Toast.makeText(context, it.message ?: context.getString(R.string.backup_load_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Service Status Top Card ──
        AkisGlassCard(accentTint = if (serviceState == GestureAccessibilityService.ServiceState.CONNECTED) Color(0xFF00E676) else Color(0xFFFF1744)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (serviceState == GestureAccessibilityService.ServiceState.CONNECTED) {
                                Color(0xFF00E676)
                            } else {
                                Color(0xFFFF1744)
                            }
                        )
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (serviceState == GestureAccessibilityService.ServiceState.CONNECTED) {
                            stringResource(R.string.service_ready)
                        } else {
                            stringResource(R.string.service_disconnected)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                    Text(
                        text = if (serviceState == GestureAccessibilityService.ServiceState.CONNECTED) {
                            stringResource(R.string.service_enabled_user)
                        } else {
                            stringResource(R.string.service_permission_prompt)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                if (serviceState != GestureAccessibilityService.ServiceState.CONNECTED) {
                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.grant_permission), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // ── Tab Bar Navigation ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(scheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val tabs = listOf(
                stringResource(R.string.tab_motion),
                stringResource(R.string.tab_appearance),
                stringResource(R.string.tab_pause),
                stringResource(R.string.tab_backup),
                stringResource(R.string.tab_about),
            )
            tabs.forEachIndexed { index, label ->
                val selected = selectedSection == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) scheme.primary else Color.Transparent)
                        .clickable { selectedSection = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }

        // ── 1A. KENAR HASSASİYETİ VE TETİKLEME (Electric Blue) ──
        if (selectedSection == 0) AkisGlassCard(accentTint = Color(0xFF3D5AFE)) {
            AkisSectionHeader(
                title = stringResource(R.string.motion_section),
                subtitle = stringResource(R.string.motion_section_subtitle),
                icon = Icons.Filled.Swipe
            )
            Spacer(Modifier.height(10.dp))

            // Edge Selection Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(Edge.LEFT, Edge.RIGHT, Edge.BOTTOM).forEach { edge ->
                    val label = edgeLabel(context, edge)
                    val selected = selectedEdge == edge
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Color(0xFF3D5AFE) else scheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { selectedEdge = edge }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.White else scheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            val currentWidth = when (selectedEdge) {
                Edge.LEFT -> config.leftTriggerWidthDp
                Edge.RIGHT -> config.rightTriggerWidthDp
                Edge.BOTTOM -> config.bottomTriggerHeightDp
            }
            val currentDamping = when (selectedEdge) {
                Edge.LEFT -> config.leftDamping
                Edge.RIGHT -> config.rightDamping
                Edge.BOTTOM -> config.bottomDamping
            }
            val currentThreshold = when (selectedEdge) {
                Edge.LEFT -> config.leftSwipeThresholdDp
                Edge.RIGHT -> config.rightSwipeThresholdDp
                Edge.BOTTOM -> config.bottomSwipeThresholdDp
            }

            AkisSliderRow(
                title = stringResource(R.string.trigger_thickness),
                valueText = "${currentWidth.roundToInt()} dp",
                value = currentWidth,
                valueRange = 8f..60f,
                onValueChange = { viewModel.setEdgeTriggerSize(selectedEdge, it) }
            )
            Text(
                text = stringResource(R.string.trigger_help),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            if (selectedEdge != Edge.BOTTOM) {
                val (vStart, vEnd) = config.verticalRangeFor(selectedEdge) ?: (0f to 1f)
                val currentLengthPercent = ((vEnd - vStart) * 100f).roundToInt()
                val currentOffsetPercent = (vStart * 100f).roundToInt()

                AkisSliderRow(
                    title = "📏 Panel Dikey Boyu (Kısalıp Uzama)",
                    valueText = "%$currentLengthPercent",
                    value = currentLengthPercent.toFloat(),
                    valueRange = 20f..100f,
                    onValueChange = { percent ->
                        val newLen = percent / 100f
                        val center = (vStart + vEnd) / 2f
                        val s = (center - newLen / 2f).coerceIn(0f, (1f - newLen).coerceAtLeast(0f))
                        val e = (s + newLen).coerceAtMost(1f)
                        viewModel.setEdgeVerticalRange(selectedEdge, s, e)
                    }
                )
                Text(
                    text = "Panelin ekrandaki dikey uzunluğunu kısaltıp uzatın.",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                AkisSliderRow(
                    title = "📍 Panel Dikey Konumu",
                    valueText = "%$currentOffsetPercent",
                    value = currentOffsetPercent.toFloat(),
                    valueRange = 0f..(100f - currentLengthPercent.toFloat()).coerceAtLeast(0f),
                    onValueChange = { offset ->
                        val newStart = offset / 100f
                        val len = vEnd - vStart
                        val newEnd = (newStart + len).coerceAtMost(1f)
                        viewModel.setEdgeVerticalRange(selectedEdge, newStart, newEnd)
                    }
                )
                Text(
                    text = "Panelin dikey ekran konumunu yukarı veya aşağı kaydırın.",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            AkisSliderRow(
                title = stringResource(R.string.sensitivity_damping),
                valueText = "%.1fx".format(currentDamping),
                value = currentDamping,
                valueRange = 0.5f..4.0f,
                onValueChange = { viewModel.setEdgeDamping(selectedEdge, it) }
            )
            Text(
                text = stringResource(R.string.sensitivity_help),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            AkisSliderRow(
                title = stringResource(R.string.threshold_distance),
                valueText = "${currentThreshold.roundToInt()} dp",
                value = currentThreshold,
                valueRange = 8f..40f,
                onValueChange = { viewModel.setEdgeSwipeThreshold(selectedEdge, it) }
            )
            Text(
                text = stringResource(R.string.threshold_help),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            AkisSliderRow(
                title = stringResource(R.string.l_threshold),
                valueText = "${config.lSwipeThresholdDp.roundToInt()} dp",
                value = config.lSwipeThresholdDp,
                valueRange = 15f..60f,
                onValueChange = { viewModel.setLSwipeThreshold(it) }
            )
            Text(
                text = stringResource(R.string.l_threshold_help),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // ── 1B. BEKLETME SÜRESİ VE FİZİĞİ (Electric Purple) ──
        if (selectedSection == 0) AkisGlassCard(accentTint = Color(0xFFD500F9)) {
            AkisSectionHeader(
                title = "⏱️ Çek ve Tut Fiziği",
                subtitle = "Bekletme süresi ve tetikleme anı seçimi",
                icon = Icons.Filled.Speed
            )
            Spacer(Modifier.height(10.dp))

            AkisSliderRow(
                title = stringResource(R.string.hold_duration),
                valueText = "${config.holdTimeMs} ms",
                value = config.holdTimeMs.toFloat(),
                valueRange = 150f..700f,
                onValueChange = { viewModel.setHoldTime(it.roundToInt().toLong()) }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.hold_mode),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    HoldFireMode.entries.forEach { mode ->
                        val active = config.holdFireMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) Color(0xFFD500F9).copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { viewModel.setHoldFireMode(mode) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = mode.localizedLabel(context),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) Color(0xFFE040FB) else scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ── 2A. ANİMASYON STİLİ VE BOYUT AYARLARI (Vibrant Cyan) ──
        if (selectedSection == 1) AkisGlassCard(accentTint = Color(0xFF00E5FF)) {
            AkisSectionHeader(
                title = stringResource(R.string.feedback_section),
                subtitle = stringResource(R.string.feedback_section_subtitle),
                icon = Icons.Filled.Palette
            )
            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.animation_style),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            val anims = listOf(
                FeedbackAnimation.OCEAN_WAVE,
                FeedbackAnimation.HYDRO_WIPE,
                FeedbackAnimation.MERCURY_TEARDROP,
                FeedbackAnimation.VORTEX,
                FeedbackAnimation.INK_FLOW,
                FeedbackAnimation.ATMOSPHERIC_MIST,
                FeedbackAnimation.GLASS_RIPPLE,
                FeedbackAnimation.COMET_TAIL,
                FeedbackAnimation.STARFIELD,
                FeedbackAnimation.PLASMA_FIRE,
                FeedbackAnimation.SOLAR_CORONA,
                FeedbackAnimation.BLACK_HOLE_PULL,
                FeedbackAnimation.PRISM_FLOW,
                FeedbackAnimation.QUANTUM_RING,
                FeedbackAnimation.AURORA_RIBBON,
            )
            val chunkedAnims = anims.chunked(2)
            chunkedAnims.forEach { rowAnims ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowAnims.forEach { anim ->
                        val selected = config.feedbackAnimation == anim
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) Color(0xFF00E5FF).copy(alpha = 0.20f) else scheme.surfaceVariant.copy(alpha = 0.35f))
                                .border(
                                    width = if (selected) 1.5.dp else 0.dp,
                                    color = if (selected) Color(0xFF00E5FF) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setFeedbackAnimation(anim) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = anim.localizedLabel(context),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (selected) Color(0xFF00E5FF) else scheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                    if (rowAnims.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            AkisSliderRow(
                title = stringResource(R.string.opacity),
                valueText = "%${(config.feedbackOpacity * 100).roundToInt()}",
                value = config.feedbackOpacity,
                valueRange = 0.1f..1.0f,
                onValueChange = viewModel::setFeedbackOpacity
            )

            AkisSliderRow(
                title = stringResource(R.string.animation_speed),
                valueText = "%.1fx".format(config.animationSpeed),
                value = config.animationSpeed,
                valueRange = 0.5f..2.0f,
                onValueChange = viewModel::setAnimationSpeed
            )

            AkisSliderRow(
                title = stringResource(R.string.animation_size),
                valueText = "%.1fx".format(config.animationSize),
                value = config.animationSize,
                valueRange = 0.5f..2.0f,
                onValueChange = viewModel::setAnimationSize
            )
        }

        // ── 2B. RENK VE TEMA SEÇENEKLERİ (Deep Indigo Violet) ──
        if (selectedSection == 1) AkisGlassCard(accentTint = Color(0xFF7C4DFF)) {
            AkisSectionHeader(
                title = "🎨 Renk Paleti ve Tema",
                subtitle = "Jest aşamaları için özel renk özelleştirme",
                icon = Icons.Filled.Palette
            )
            Spacer(Modifier.height(10.dp))

            AkisInfiniteColorPicker(
                title = stringResource(R.string.quick_color),
                currentColorArgb = config.feedbackColorArgb,
                onColorChanged = viewModel::setFeedbackColor
            )

            Spacer(Modifier.height(8.dp))

            AkisInfiniteColorPicker(
                title = stringResource(R.string.hold_color),
                currentColorArgb = config.secondaryColorArgb,
                onColorChanged = viewModel::setSecondaryColor
            )

            Spacer(Modifier.height(8.dp))

            AkisInfiniteColorPicker(
                title = stringResource(R.string.l_color),
                currentColorArgb = config.lSwipeColorArgb,
                onColorChanged = viewModel::setLSwipeColor
            )

            Spacer(Modifier.height(8.dp))

            AkisSwitchRow(
                title = stringResource(R.string.adaptive_color),
                subtitle = stringResource(R.string.adaptive_color_subtitle),
                checked = config.useAppAdaptiveColor,
                onCheckedChange = viewModel::setUseAppAdaptiveColor
            )
        }

        // ── 2C. DOKUNSAL TİTREŞİM VE SES (Vibrant Amber / Orange) ──
        if (selectedSection == 1) AkisGlassCard(accentTint = Color(0xFFFF9100)) {
            AkisSectionHeader(
                title = "⚡ Dokunsal Titreşim & Ses",
                subtitle = "Titreşim şiddeti ve geri bildirim tonu",
                icon = Icons.Filled.Speed
            )
            Spacer(Modifier.height(10.dp))

            AkisSwitchRow(
                title = stringResource(R.string.haptic_feedback),
                subtitle = stringResource(R.string.haptic_feedback_subtitle),
                checked = config.hapticEnabled,
                onCheckedChange = viewModel::setHapticEnabled
            )

            AkisSliderRow(
                title = stringResource(R.string.vibration_intensity),
                valueText = "%${(config.hapticIntensity * 100).roundToInt()}",
                value = config.hapticIntensity,
                valueRange = 0.0f..1.0f,
                onValueChange = viewModel::setHapticIntensity,
            )

            AkisSwitchRow(
                title = stringResource(R.string.click_sound),
                subtitle = stringResource(R.string.click_sound_subtitle),
                checked = config.hapticSoundEnabled,
                onCheckedChange = viewModel::setHapticSoundEnabled
            )
        }

        // ── 2D. ANA SAYFA KART DÜZENİ VE SADELİK (Neon Teal Blue) ──
        if (selectedSection == 1) AkisGlassCard(accentTint = Color(0xFF00B0FF)) {
            AkisSectionHeader(
                title = stringResource(R.string.home_cards_title),
                subtitle = stringResource(R.string.home_cards_subtitle),
                icon = Icons.Filled.Apps
            )
            Spacer(Modifier.height(10.dp))

            AkisSwitchRow(
                title = stringResource(R.string.show_phone_map),
                subtitle = stringResource(R.string.show_phone_map_subtitle),
                checked = config.showPhoneMap,
                onCheckedChange = viewModel::setShowPhoneMap
            )

            AkisSwitchRow(
                title = stringResource(R.string.show_summary_chart),
                subtitle = stringResource(R.string.show_summary_chart_subtitle),
                checked = config.showSummaryChart,
                onCheckedChange = viewModel::setShowSummaryChart
            )

            AkisSwitchRow(
                title = stringResource(R.string.show_presets_card),
                subtitle = stringResource(R.string.show_presets_card_subtitle),
                checked = config.showPresetsCard,
                onCheckedChange = viewModel::setShowPresetsCard
            )
        }

        // ── 2E. EYLEM SİMGE PAKETİ (Electric Pink / Rose) ──
        if (selectedSection == 1) AkisGlassCard(accentTint = Color(0xFFFF4081)) {
            AkisSectionHeader(
                title = stringResource(R.string.icon_pack_section),
                subtitle = stringResource(R.string.icon_pack_section_subtitle),
                icon = Icons.Filled.Style
            )
            Spacer(Modifier.height(10.dp))

            ActionIconPack.entries.forEach { pack ->
                val selected = config.actionIconPack == pack
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Color(0xFFFF4081).copy(alpha = 0.20f) else scheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(
                            width = if (selected) 1.5.dp else 0.dp,
                            color = if (selected) Color(0xFFFF4081) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.setActionIconPack(pack) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(pack.titleResId),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selected) Color(0xFFFF4081) else scheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = pack.samplePreview,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFFFF4081),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        // ── 3A. OTOMATİK DURAKLATMA KOŞULLARI (Crimson Red) ──
        if (selectedSection == 2) AkisGlassCard(accentTint = Color(0xFFFF1744)) {
            AkisSectionHeader(
                title = stringResource(R.string.pause_section),
                subtitle = stringResource(R.string.pause_section_subtitle),
                icon = Icons.Filled.Security
            )
            Spacer(Modifier.height(6.dp))

            AkisSwitchRow(
                title = stringResource(R.string.lock_screen),
                subtitle = stringResource(R.string.lock_screen_subtitle),
                checked = config.pauseOnLockScreen,
                onCheckedChange = viewModel::setPauseOnLockScreen
            )

            AkisSwitchRow(
                title = stringResource(R.string.keyboard_open),
                subtitle = stringResource(R.string.keyboard_open_subtitle),
                checked = config.pauseWhenKeyboardVisible,
                onCheckedChange = viewModel::setPauseWhenKeyboardVisible
            )

            AkisSwitchRow(
                title = stringResource(R.string.landscape_screen),
                subtitle = stringResource(R.string.landscape_screen_subtitle),
                checked = config.pauseInLandscape,
                onCheckedChange = viewModel::setPauseInLandscape
            )

            AkisSwitchRow(
                title = stringResource(R.string.immersive_fullscreen),
                subtitle = stringResource(R.string.immersive_fullscreen_subtitle),
                checked = config.pauseOnFullScreen,
                onCheckedChange = viewModel::setPauseOnFullScreen
            )

            AkisSwitchRow(
                title = stringResource(R.string.permission_screens),
                subtitle = stringResource(R.string.permission_screens_subtitle),
                checked = config.pauseOnPermissionScreen,
                onCheckedChange = viewModel::setPauseOnPermissionScreen
            )
        }

        // ── 3B. UYGULAMA İSTİSNALARI (Bright Magenta) ──
        if (selectedSection == 2) AkisGlassCard(accentTint = Color(0xFFE040FB)) {
            AkisSectionHeader(
                title = "📱 Uygulama İstisnaları",
                subtitle = "Jestlerin devre dışı kalacağı uygulamalar",
                icon = Icons.Filled.Apps
            )
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAppPicker = true }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_exclusions),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = if (pausedPackages.isEmpty()) {
                            stringResource(R.string.no_paused_apps)
                        } else {
                            stringResource(R.string.paused_apps_count, pausedPackages.size)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Filled.ChevronRight, null, tint = scheme.onSurfaceVariant)
            }
        }

        // ── 4A. YEDEKLEME VE ROOT (Emerald Green) ──
        if (selectedSection == 3) AkisGlassCard(accentTint = Color(0xFF00E676)) {
            AkisSectionHeader(
                title = stringResource(R.string.backup_section),
                subtitle = stringResource(R.string.backup_section_subtitle),
                icon = Icons.Filled.Save
            )
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { exportBackup.launch("akis-gesture-yedek.json") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.backup_action), style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { importBackup.launch(arrayOf("application/json", "text/plain")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Restore, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.restore_action), style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.root_status),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = when (rootAccess) {
                        RootAccessState.CHECKING -> stringResource(R.string.root_checking)
                        RootAccessState.AVAILABLE -> stringResource(R.string.root_available)
                        RootAccessState.UNAVAILABLE -> stringResource(R.string.root_unavailable)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (rootAccess == RootAccessState.AVAILABLE) Color(0xFF00E676) else scheme.onSurfaceVariant
                )
            }
        }

        // ── 5A. HAKKINDA VE DİL SEÇİMİ (Golden Amber) ──
        if (selectedSection == 4) AkisGlassCard(accentTint = Color(0xFFFFAB00)) {
            AkisSectionHeader(
                title = stringResource(R.string.about_title),
                subtitle = stringResource(R.string.about_subtitle),
                icon = Icons.Filled.Info,
            )
            Spacer(Modifier.height(10.dp))

            AboutInfoRow(label = stringResource(R.string.version), value = versionName)
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.language_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf(
                    "" to stringResource(R.string.language_system),
                    "tr" to stringResource(R.string.language_turkish),
                    "en" to stringResource(R.string.language_english),
                ).forEach { (tag, label) ->
                    OutlinedButton(
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(tag),
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                    ) {
                        Text(label, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = scheme.outlineVariant.copy(alpha = 0.45f),
            )

            Text(
                text = stringResource(R.string.about_support),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.about_root),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = scheme.outlineVariant.copy(alpha = 0.45f),
            )
            Text(
                text = stringResource(R.string.about_upstream),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ARCJ137442/OpenSwipe")),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(stringResource(R.string.open_upstream))
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }

    // Backup Confirmation Dialog
    pendingImportJson?.let { json ->
        AlertDialog(
            onDismissRequest = { pendingImportJson = null },
            title = { Text(stringResource(R.string.restore_confirm_title)) },
            text = { Text(stringResource(R.string.restore_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingImportJson = null
                    scope.launch {
                        runCatching { SettingsBackupManager.import(app, json) }
                            .onSuccess { Toast.makeText(context, context.getString(R.string.backup_loaded), Toast.LENGTH_SHORT).show() }
                            .onFailure { Toast.makeText(context, it.message ?: context.getString(R.string.generic_error), Toast.LENGTH_LONG).show() }
                    }
                }) { Text(stringResource(R.string.restore_action), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportJson = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // App Pause Selection Dialog
    if (showAppPicker) {
        AlertDialog(
            onDismissRequest = { showAppPicker = false },
            title = { Text(stringResource(R.string.paused_apps_title)) },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(selectableApps) { appInfo ->
                        val isPaused = pausedPackages.contains(appInfo.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setPackagePaused(appInfo.packageName, !isPaused) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isPaused,
                                onCheckedChange = { viewModel.setPackagePaused(appInfo.packageName, !isPaused) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(appInfo.label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppPicker = false }) { Text(stringResource(R.string.done), fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
        )
    }
}

@Composable
fun AkisInfiniteColorPicker(
    title: String,
    currentColorArgb: Int,
    onColorChanged: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var hsv by remember(currentColorArgb) { mutableStateOf(
        FloatArray(3).also { AndroidColor.colorToHSV(currentColorArgb, it) }
    ) }
    val previewColorArgb = AndroidColor.HSVToColor(hsv)
    val hexCode = String.format("#%06X", 0xFFFFFF and previewColorArgb)
    val scheme = MaterialTheme.colorScheme
    val presets = listOf(
        0xFF3D5AFE.toInt(), 0xFF00B8D4.toInt(), 0xFF00C853.toInt(), 0xFFFFD600.toInt(),
        0xFFFF6D00.toInt(), 0xFFD500F9.toInt(), 0xFFFF1744.toInt(), 0xFFFFFFFF.toInt(),
    )
    fun commitColor() = onColorChanged(AndroidColor.HSVToColor(hsv))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(scheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(previewColorArgb))
                        .border(2.dp, scheme.onSurface.copy(alpha = 0.3f), CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.hex_hue, hexCode, hsv[0].toInt()),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = stringResource(R.string.color_picker),
                    tint = scheme.onSurface
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Text(
                    text = stringResource(R.string.preset_colors),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                presets.chunked(4).forEach { rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowColors.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(preset))
                                    .border(
                                        if (preset == currentColorArgb) 3.dp else 1.dp,
                                        if (preset == currentColorArgb) scheme.primary else scheme.outline,
                                        CircleShape,
                                    )
                                    .clickable {
                                        hsv = FloatArray(3).also { AndroidColor.colorToHSV(preset, it) }
                                        onColorChanged(preset)
                                    }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Hue Slider (0 - 360)
                Text(
                    text = stringResource(R.string.hue_value, hsv[0].toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Slider(
                    value = hsv[0],
                    onValueChange = { newHue ->
                        hsv = hsv.copyOf().also {
                            it[0] = newHue
                            it[1] = it[1].coerceAtLeast(0.1f)
                            it[2] = it[2].coerceAtLeast(0.1f)
                        }
                    },
                    onValueChangeFinished = ::commitColor,
                    valueRange = 0f..360f
                )

                // Saturation Slider (0.0 - 1.0)
                Text(
                    text = stringResource(R.string.saturation_value, (hsv[1] * 100).toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurface
                )
                Slider(
                    value = hsv[1],
                    onValueChange = { newSat ->
                        hsv = hsv.copyOf().also { it[1] = newSat }
                    },
                    onValueChangeFinished = ::commitColor,
                    valueRange = 0f..1f
                )

                // Brightness / Value Slider (0.0 - 1.0)
                Text(
                    text = stringResource(R.string.brightness_value, (hsv[2] * 100).toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurface
                )
                Slider(
                    value = hsv[2],
                    onValueChange = { newVal ->
                        hsv = hsv.copyOf().also { it[2] = newVal }
                    },
                    onValueChangeFinished = ::commitColor,
                    valueRange = 0f..1f
                )
            }
        }
    }
}
