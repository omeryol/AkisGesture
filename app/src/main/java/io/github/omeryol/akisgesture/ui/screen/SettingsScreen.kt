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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import io.github.omeryol.akisgesture.model.ColorPalettePreset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History


import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Refresh
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
import io.github.omeryol.akisgesture.diagnostics.DiagnosticsSettingsEntry
import io.github.omeryol.akisgesture.feedback.FeedbackAnimation
import io.github.omeryol.akisgesture.gesture.HoldFireMode
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.service.GestureAccessibilityService
import io.github.omeryol.akisgesture.util.PermissionHelper
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
import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.ui.theme.EdgeUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import io.github.omeryol.akisgesture.util.GithubRelease
import io.github.omeryol.akisgesture.util.GithubReleaseChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.content.FileProvider
import io.github.omeryol.akisgesture.util.VerifiedApkDownloader
import java.text.DateFormat
import java.util.Date

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
    var updateCheckState by remember { mutableStateOf<UpdateCheckState>(UpdateCheckState.IDLE) }
    var showReleaseDialog by remember { mutableStateOf(false) }
    var updateDownloading by remember { mutableStateOf(false) }
    var updateDownloadError by remember { mutableStateOf<String?>(null) }
    var showVersionHistoryDialog by remember { mutableStateOf(false) }
    var showCustomColorPickers by remember { mutableStateOf(false) }


    val context = LocalContext.current
    val app = context.applicationContext as AkisGestureApp
    val updatePreferences = remember { context.getSharedPreferences("update_check", android.content.Context.MODE_PRIVATE) }
    var lastCheckedAt by remember { mutableStateOf(updatePreferences.getLong("last_checked_at", 0L)) }
    val lastCheckedLabel = remember(lastCheckedAt) {
        if (lastCheckedAt > 0L) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(lastCheckedAt)) else null
    }
    val scope = rememberCoroutineScope()
    var sideRangeFeedback by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var sideRangeFeedbackJob by remember { mutableStateOf<Job?>(null) }

    fun showSideRangeFeedback(start: Float, end: Float) {
        sideRangeFeedback = (start * 100f).roundToInt() to ((end - start) * 100f).roundToInt()
        sideRangeFeedbackJob?.cancel()
        sideRangeFeedbackJob = scope.launch {
            delay(3_000)
            sideRangeFeedback = null
        }
    }
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
                EdgeUi.ordered.forEach { edge ->
                    val label = edgeLabel(context, edge)
                    val selected = selectedEdge == edge
                    val edgeColor = EdgeUi.color(edge)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) edgeColor else edgeColor.copy(alpha = 0.10f))
                            .clickable { selectedEdge = edge }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.White else edgeColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            val selectedEdgeEnabled = when (selectedEdge) {
                Edge.LEFT -> config.leftEnabled
                Edge.RIGHT -> config.rightEnabled
                Edge.BOTTOM -> config.bottomEnabled
            }
            AkisSwitchRow(
                title = edgeLabel(context, selectedEdge),
                subtitle = stringResource(R.string.edge_enabled_subtitle),
                checked = selectedEdgeEnabled,
                onCheckedChange = { viewModel.setEdgeEnabled(selectedEdge, it) },
            )

            Spacer(Modifier.height(6.dp))

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
                    title = stringResource(R.string.edge_vertical_length_title),
                    valueText = "%$currentLengthPercent",
                    value = currentLengthPercent.toFloat(),
                    valueRange = 20f..100f,
                    onValueChange = { percent ->
                        val newLen = percent / 100f
                        val center = (vStart + vEnd) / 2f
                        val s = (center - newLen / 2f).coerceIn(0f, (1f - newLen).coerceAtLeast(0f))
                        val e = (s + newLen).coerceAtMost(1f)
                        viewModel.setEdgeVerticalRange(selectedEdge, s, e)
                        showSideRangeFeedback(s, e)
                    }
                )
                Text(
                    text = stringResource(R.string.edge_vertical_length_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                AkisSliderRow(
                    title = stringResource(R.string.edge_vertical_position_title),
                    valueText = "%$currentOffsetPercent",
                    value = currentOffsetPercent.toFloat(),
                    valueRange = 0f..(100f - currentLengthPercent.toFloat()).coerceAtLeast(0f),
                    onValueChange = { offset ->
                        val newStart = offset / 100f
                        val len = vEnd - vStart
                        val newEnd = (newStart + len).coerceAtMost(1f)
                        viewModel.setEdgeVerticalRange(selectedEdge, newStart, newEnd)
                        showSideRangeFeedback(newStart, newEnd)
                    }
                )
                Text(
                    text = stringResource(R.string.edge_vertical_position_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            AnimatedVisibility(visible = sideRangeFeedback != null) {
                sideRangeFeedback?.let { (position, length) ->
                    Text(
                        text = stringResource(R.string.edge_range_live, position, length),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    lastCheckedLabel?.let {
                        Text(stringResource(R.string.update_check_last_checked, it), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    }
                }
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
                title = stringResource(R.string.hold_physics_card_title),
                subtitle = stringResource(R.string.hold_physics_card_subtitle),
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

        // ── 1C. DOKUNSAL TİTREŞİM VE SES (Vibrant Amber / Orange) ──
        if (selectedSection == 0) AkisGlassCard(accentTint = Color(0xFFFF9100)) {
            AkisSectionHeader(
                title = stringResource(R.string.haptic_sound_card_title),
                subtitle = stringResource(R.string.haptic_sound_card_subtitle),
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

            AkisSliderRow(
                title = stringResource(R.string.icon_size),
                valueText = "%.1fx".format(config.iconSize),
                value = config.iconSize,
                valueRange = 0.5f..2.0f,
                onValueChange = viewModel::setIconSize
            )

            AkisSwitchRow(
                title = stringResource(R.string.gesture_indicator_bar),
                subtitle = stringResource(R.string.gesture_indicator_bar_subtitle),
                checked = config.showGestureIndicatorBar,
                onCheckedChange = viewModel::setShowGestureIndicatorBar,
            )
        }

        // ── 2B. RENK PALETİ VE TEMA (Deep Indigo Violet) ──
        if (selectedSection == 1) AkisGlassCard(accentTint = Color(0xFF7C4DFF)) {
            AkisSectionHeader(
                title = stringResource(R.string.palette_icon_card_title),
                subtitle = stringResource(R.string.palette_icon_card_subtitle),
                icon = Icons.Filled.Palette
            )
            Spacer(Modifier.height(10.dp))

            // ── Hazır 3'lü Uyumlu Renk Şablonları ──
            Text(
                text = stringResource(R.string.color_palettes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.color_palettes_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(ColorPalettePreset.presets, key = { it.id }) { palette ->
                    val isSelected = config.feedbackColorArgb == palette.quickColor &&
                        config.secondaryColorArgb == palette.holdColor &&
                        config.lSwipeColorArgb == palette.lSwipeColor

                    val cardBorderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .border(
                                width = 1.5.dp,
                                color = cardBorderColor,
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable {
                                viewModel.applyColorPalette(palette.quickColor, palette.holdColor, palette.lSwipeColor)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(palette.quickColor))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(palette.holdColor))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(palette.lSwipeColor))
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(palette.nameResId),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Collapsible Custom Color Pickers Accordion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(scheme.surfaceVariant.copy(alpha = 0.35f))
                    .clickable { showCustomColorPickers = !showCustomColorPickers }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.manual_color_edit),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Icon(
                    imageVector = if (showCustomColorPickers) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = showCustomColorPickers) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    AkisInfiniteColorPicker(
                        title = stringResource(R.string.quick_color),
                        currentColorArgb = config.feedbackColorArgb,
                        onColorChanged = viewModel::setFeedbackColor
                    )
                    Spacer(Modifier.height(6.dp))
                    AkisInfiniteColorPicker(
                        title = stringResource(R.string.hold_color),
                        currentColorArgb = config.secondaryColorArgb,
                        onColorChanged = viewModel::setSecondaryColor
                    )
                    Spacer(Modifier.height(6.dp))
                    AkisInfiniteColorPicker(
                        title = stringResource(R.string.l_color),
                        currentColorArgb = config.lSwipeColorArgb,
                        onColorChanged = viewModel::setLSwipeColor
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            AkisSwitchRow(
                title = stringResource(R.string.adaptive_color),
                subtitle = stringResource(R.string.adaptive_color_subtitle),
                checked = config.useAppAdaptiveColor,
                onCheckedChange = viewModel::setUseAppAdaptiveColor
            )

            Spacer(Modifier.height(12.dp))

            // Action Icon Pack Selector integrated cleanly
            Text(
                text = stringResource(R.string.icon_pack_section),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            ActionIconPack.entries.forEach { pack ->
                val selected = config.actionIconPack == pack
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Color(0xFF7C4DFF).copy(alpha = 0.18f) else scheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(
                            width = if (selected) 1.5.dp else 0.dp,
                            color = if (selected) Color(0xFF7C4DFF) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.setActionIconPack(pack) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(pack.titleResId),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selected) Color(0xFF7C4DFF) else scheme.onSurface
                        )
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
                            tint = Color(0xFF7C4DFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        // ── 2C. ANA SAYFA KART DÜZENİ VE SADELİK (Neon Teal Blue) ──
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
            if (config.pauseOnLockScreen) PauseWarningCard(
                title = stringResource(R.string.pause_warning_lock_title),
                description = stringResource(R.string.pause_warning_lock_desc),
            )

            AkisSwitchRow(
                title = stringResource(R.string.keyboard_open),
                subtitle = stringResource(R.string.keyboard_open_subtitle),
                checked = config.pauseWhenKeyboardVisible,
                onCheckedChange = viewModel::setPauseWhenKeyboardVisible
            )

            if (config.pauseWhenKeyboardVisible) {
                PauseWarningCard(
                    title = stringResource(R.string.keyboard_open_warning_title),
                    description = stringResource(R.string.keyboard_open_warning_desc),
                )
            }

            AkisSwitchRow(
                title = stringResource(R.string.landscape_screen),
                subtitle = stringResource(R.string.landscape_screen_subtitle),
                checked = config.pauseInLandscape,
                onCheckedChange = viewModel::setPauseInLandscape
            )
            if (config.pauseInLandscape) PauseWarningCard(
                title = stringResource(R.string.pause_warning_landscape_title),
                description = stringResource(R.string.pause_warning_landscape_desc),
            )

            AkisSwitchRow(
                title = stringResource(R.string.immersive_fullscreen),
                subtitle = stringResource(R.string.immersive_fullscreen_subtitle),
                checked = config.pauseOnFullScreen,
                onCheckedChange = viewModel::setPauseOnFullScreen
            )
            if (config.pauseOnFullScreen) PauseWarningCard(
                title = stringResource(R.string.pause_warning_fullscreen_title),
                description = stringResource(R.string.pause_warning_fullscreen_desc),
            )

            AkisSwitchRow(
                title = stringResource(R.string.permission_screens),
                subtitle = stringResource(R.string.permission_screens_subtitle),
                checked = config.pauseOnPermissionScreen,
                onCheckedChange = viewModel::setPauseOnPermissionScreen
            )
            if (config.pauseOnPermissionScreen) PauseWarningCard(
                title = stringResource(R.string.pause_warning_permission_title),
                description = stringResource(R.string.pause_warning_permission_desc),
            )

            AkisSwitchRow(
                title = stringResource(R.string.camera_active),
                subtitle = stringResource(R.string.camera_active_subtitle),
                checked = config.pauseOnCamera,
                onCheckedChange = viewModel::setPauseOnCamera
            )
            if (config.pauseOnCamera) PauseWarningCard(
                title = stringResource(R.string.pause_warning_camera_title),
                description = stringResource(R.string.pause_warning_camera_desc),
            )

            AkisSwitchRow(
                title = stringResource(R.string.phone_call_active),
                subtitle = stringResource(R.string.phone_call_active_subtitle),
                checked = config.pauseOnPhoneCall,
                onCheckedChange = viewModel::setPauseOnPhoneCall
            )
            if (config.pauseOnPhoneCall) PauseWarningCard(
                title = stringResource(R.string.pause_warning_call_title),
                description = stringResource(R.string.pause_warning_call_desc),
            )

            AkisSwitchRow(
                title = stringResource(R.string.pause_on_launcher),
                subtitle = stringResource(R.string.pause_on_launcher_subtitle),
                checked = config.pauseOnLauncher,
                onCheckedChange = viewModel::setPauseOnLauncher,
            )
            if (config.pauseOnLauncher) PauseWarningCard(
                title = stringResource(R.string.pause_warning_launcher_title),
                description = stringResource(R.string.pause_warning_launcher_desc),
            )
        }


        // ── 3B. UYGULAMA İSTİSNALARI (Bright Magenta) ──
        if (selectedSection == 2) AkisGlassCard(accentTint = Color(0xFFE040FB)) {
            AkisSectionHeader(
                title = stringResource(
                    if (config.appPauseMode == io.github.omeryol.akisgesture.gesture.AppPauseMode.WHITELIST) {
                        R.string.paused_apps_run_title
                    } else {
                        R.string.app_exceptions_card_title
                    }
                ),
                subtitle = stringResource(R.string.app_exceptions_card_subtitle),
                icon = Icons.Filled.Apps
            )
            Spacer(Modifier.height(8.dp))

            // Mode Selector: Blacklist vs Whitelist
            Text(
                text = stringResource(R.string.app_pause_mode_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(scheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val modes = listOf(
                    io.github.omeryol.akisgesture.gesture.AppPauseMode.BLACKLIST to stringResource(R.string.app_pause_mode_blacklist_short),
                    io.github.omeryol.akisgesture.gesture.AppPauseMode.WHITELIST to stringResource(R.string.app_pause_mode_whitelist_short),
                )
                modes.forEach { (mode, label) ->
                    val selected = config.appPauseMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) Color(0xFFE040FB) else Color.Transparent)
                            .clickable { viewModel.setAppPauseMode(mode) }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) Color.White else scheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(scheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable { showAppPicker = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_exclusions),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
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
                Icon(Icons.Filled.Add, null, tint = Color(0xFFE040FB))
            }

            // Interactive list of selected app chips
            if (pausedPackages.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                val selectableAppsMap = selectableApps.associateBy { it.packageName }
                val exceptionTint = if (config.appPauseMode == io.github.omeryol.akisgesture.gesture.AppPauseMode.WHITELIST) {
                    Color(0xFF43A047)
                } else {
                    Color(0xFFFF8F00)
                }
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pausedPackages.forEach { pkg ->
                        val appLabel = selectableAppsMap[pkg]?.label ?: pkg.substringAfterLast('.')
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(exceptionTint.copy(alpha = 0.18f))
                                .border(1.dp, exceptionTint.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (config.appPauseMode == io.github.omeryol.akisgesture.gesture.AppPauseMode.WHITELIST) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = null,
                                tint = exceptionTint,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = appLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.remove),
                                tint = exceptionTint,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { viewModel.setPackagePaused(pkg, false) }
                            )
                        }
                    }
                }
            }
        }


        // ── 4A. KORUMA VE SİSTEM SAĞLIĞI (Emerald Green & Cyan Glass Cards) ──
        if (selectedSection == 3) {
            // 1. Erişilebilirlik Hizmet Sağlığı Kartı
            val isServiceConnected = serviceState == GestureAccessibilityService.ServiceState.CONNECTED
            AkisGlassCard(accentTint = if (isServiceConnected) Color(0xFF00E676) else Color(0xFFFF1744)) {
                AkisSectionHeader(
                    title = stringResource(R.string.health_card_title),
                    subtitle = stringResource(R.string.health_card_subtitle),
                    icon = Icons.Filled.Security
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isServiceConnected) Color(0xFF00E676) else Color(0xFFFF1744))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isServiceConnected) stringResource(R.string.health_status_connected) else stringResource(R.string.health_status_disconnected),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isServiceConnected) Color(0xFF00E676) else Color(0xFFFF1744)
                        )
                    }
                    OutlinedButton(
                        onClick = { PermissionHelper.openAccessibilitySettings(context) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.health_status_rebind), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Text(
                text = stringResource(
                    if (config.appPauseMode == io.github.omeryol.akisgesture.gesture.AppPauseMode.BLACKLIST) {
                        R.string.app_pause_mode_blacklist_hint
                    } else {
                        R.string.app_pause_mode_whitelist_hint
                    }
                ),
                modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )

            if (config.appPauseMode == io.github.omeryol.akisgesture.gesture.AppPauseMode.WHITELIST) {
                PauseWarningCard(
                    title = stringResource(R.string.app_pause_mode_whitelist_warning_title),
                    description = stringResource(R.string.app_pause_mode_whitelist_warning_desc),
                )
            }

            Spacer(Modifier.height(10.dp))
            DiagnosticsSettingsEntry()

            Spacer(Modifier.height(10.dp))

            // 2. Arka Plan & Pil Koruması Kartı
            val isBatteryIgnored = remember(context) { PermissionHelper.isBatteryOptimizationIgnored(context) }
            AkisGlassCard(accentTint = if (isBatteryIgnored) Color(0xFF00B0FF) else Color(0xFFFF9100)) {
                AkisSectionHeader(
                    title = stringResource(R.string.battery_card_title),
                    subtitle = stringResource(R.string.battery_card_subtitle),
                    icon = Icons.Filled.Speed
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBatteryIgnored) stringResource(R.string.battery_unrestricted) else stringResource(R.string.battery_restricted),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isBatteryIgnored) Color(0xFF00E676) else Color(0xFFFF9100)
                    )
                    if (!isBatteryIgnored) {
                        OutlinedButton(
                            onClick = { PermissionHelper.requestIgnoreBatteryOptimization(context) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.battery_open_settings), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // 3. Son Kullanılanlardan Gizle ve Kapanmayı Önleme Kartı
            AkisGlassCard(accentTint = Color(0xFF00E5FF)) {
                AkisSectionHeader(
                    title = stringResource(R.string.recents_lock_title),
                    subtitle = stringResource(R.string.recents_lock_subtitle),
                    icon = Icons.Filled.Security
                )
                Spacer(Modifier.height(6.dp))

                AkisSwitchRow(
                    title = stringResource(R.string.hide_from_recents),
                    subtitle = stringResource(R.string.hide_from_recents_subtitle),
                    checked = config.hideFromRecents,
                    onCheckedChange = viewModel::setHideFromRecents
                )

                Spacer(Modifier.height(10.dp))
                AkisSwitchRow(
                    title = stringResource(R.string.automation_apps_title),
                    subtitle = stringResource(R.string.automation_apps_subtitle),
                    checked = config.automationAppsEnabled,
                    onCheckedChange = viewModel::setAutomationAppsEnabled,
                )

                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00E5FF).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.recents_lock_guide_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.recents_lock_guide_text),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurface
                    )
                }
            }

            // 3. Ayrıcalıklı Otomatik İyileştirme Kartı (Yalnızca Root yetkisi VARSA görünür!)
            if (rootAccess == RootAccessState.AVAILABLE) {
                Spacer(Modifier.height(10.dp))
                AkisGlassCard(accentTint = Color(0xFFAA00FF)) {
                    AkisSectionHeader(
                        title = stringResource(R.string.privileged_card_title),
                        subtitle = stringResource(R.string.privileged_card_subtitle),
                        icon = Icons.Filled.Security
                    )
                    Spacer(Modifier.height(8.dp))

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
                            text = stringResource(R.string.root_available),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Watchdog Enable/Disable Switch
                    AkisSwitchRow(
                        title = stringResource(R.string.root_watchdog_title),
                        subtitle = stringResource(R.string.root_watchdog_subtitle),
                        checked = config.rootWatchdogEnabled,
                        onCheckedChange = viewModel::setRootWatchdogEnabled
                    )

                    if (config.rootWatchdogEnabled) {
                        Spacer(Modifier.height(10.dp))

                        val intervalVal = config.rootWatchdogIntervalMinutes
                        val intervalText = if (intervalVal >= 60) {
                            "${intervalVal / 60} saat ${if (intervalVal % 60 > 0) "${intervalVal % 60} dk" else ""}".trim()
                        } else {
                            "$intervalVal dk"
                        }

                        AkisSliderRow(
                            title = stringResource(R.string.root_watchdog_interval),
                            valueText = intervalText,
                            value = intervalVal.toFloat(),
                            valueRange = 5f..120f,
                            onValueChange = { viewModel.setRootWatchdogInterval(it.toInt()) }
                        )

                        Spacer(Modifier.height(8.dp))

                        // Color-coded Battery Impact Indicator & Written Warning
                        val (impactTitle, impactDesc, impactColor) = when {
                            intervalVal <= 10 -> Triple(
                                stringResource(R.string.battery_impact_high),
                                stringResource(R.string.battery_impact_high_desc),
                                Color(0xFFFF1744)
                            )
                            intervalVal <= 30 -> Triple(
                                stringResource(R.string.battery_impact_moderate),
                                stringResource(R.string.battery_impact_moderate_desc),
                                Color(0xFFFF9100)
                            )
                            else -> Triple(
                                stringResource(R.string.battery_impact_low),
                                stringResource(R.string.battery_impact_low_desc),
                                Color(0xFF00E676)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(impactColor.copy(alpha = 0.12f))
                                .border(1.dp, impactColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = impactTitle,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = impactColor
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = impactDesc,
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Detailed Scope Notice Box (What Root DOES vs DOES NOT do)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(scheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.root_scope_title),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.root_scope_does),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.root_scope_does_not),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // 4. Yedekleme ve Geri Yükleme Kartı
            AkisGlassCard(accentTint = Color(0xFFFFAB00)) {
                AkisSectionHeader(
                    title = stringResource(R.string.backup_section),
                    subtitle = stringResource(R.string.backup_section_subtitle),
                    icon = Icons.Filled.Save
                )
                Spacer(Modifier.height(8.dp))
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
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (updateCheckState) {
                            UpdateCheckState.CURRENT -> Color(0xFF00C853).copy(alpha = 0.16f)
                            UpdateCheckState.FAILED -> scheme.error.copy(alpha = 0.16f)
                            is UpdateCheckState.AVAILABLE -> Color(0xFFFFAB00).copy(alpha = 0.20f)
                            is UpdateCheckState.DEV_BUILD -> Color(0xFFE040FB).copy(alpha = 0.20f)
                            else -> Color(0xFF00B0FF).copy(alpha = 0.15f)
                        }
                    )
                    .clickable(enabled = updateCheckState is UpdateCheckState.AVAILABLE) {
                        showReleaseDialog = true
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = when (updateCheckState) {
                            is UpdateCheckState.DEV_BUILD -> stringResource(R.string.update_check_dev_build)
                            else -> stringResource(R.string.update_check_title)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (updateCheckState) {
                            is UpdateCheckState.DEV_BUILD -> Color(0xFFE040FB)
                            else -> scheme.onSurface
                        },
                    )
                    Text(
                        text = when (val state = updateCheckState) {
                            UpdateCheckState.IDLE -> stringResource(R.string.update_check_idle)
                            UpdateCheckState.CHECKING -> stringResource(R.string.update_check_checking)
                            UpdateCheckState.CURRENT -> stringResource(R.string.update_check_current, versionName)
                            is UpdateCheckState.AVAILABLE -> stringResource(R.string.update_check_available, state.release.version)
                            is UpdateCheckState.DEV_BUILD -> stringResource(R.string.update_check_dev_build_desc, versionName, state.releaseVersion)
                            UpdateCheckState.FAILED -> stringResource(R.string.update_check_failed)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (updateCheckState) {
                            UpdateCheckState.CURRENT -> Color(0xFF00E676)
                            UpdateCheckState.FAILED -> scheme.error
                            is UpdateCheckState.AVAILABLE -> Color(0xFFFFC107)
                            is UpdateCheckState.DEV_BUILD -> Color(0xFFE040FB)
                            else -> scheme.onSurfaceVariant
                        },
                    )
                }
                androidx.compose.material3.Button(
                    onClick = {
                        updateCheckState = UpdateCheckState.CHECKING
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                runCatching { GithubReleaseChecker.fetchLatestRelease() }
                            }
                            lastCheckedAt = System.currentTimeMillis()
                            updatePreferences.edit().putLong("last_checked_at", lastCheckedAt).apply()
                            updateCheckState = result.fold(
                                onSuccess = { release ->
                                    val comp = GithubReleaseChecker.compareVersions(versionName, release.version)
                                    when {
                                        comp > 0 -> UpdateCheckState.AVAILABLE(release).also { showReleaseDialog = true }
                                        comp < 0 -> UpdateCheckState.DEV_BUILD(release.version)
                                        else -> UpdateCheckState.CURRENT
                                    }
                                },
                                onFailure = { UpdateCheckState.FAILED },
                            )
                        }
                    },
                    enabled = updateCheckState != UpdateCheckState.CHECKING,
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00B0FF),
                        contentColor = Color.White
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.update_check_action), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Sürüm Geçmişi (Changelog Button) - Styled Card Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF7C4DFF).copy(alpha = 0.16f))
                    .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .clickable { showVersionHistoryDialog = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.version_history_button),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.version_history_subtitle),
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF7C4DFF))
                }
            }

            if (showReleaseDialog) {
                val release = (updateCheckState as? UpdateCheckState.AVAILABLE)?.release
                if (release != null) {
                    AlertDialog(
                        onDismissRequest = { showReleaseDialog = false },
                        containerColor = scheme.surface,
                        titleContentColor = scheme.onSurface,
                        textContentColor = scheme.onSurfaceVariant,
                        title = {
                            Column {
                                Text(
                                    stringResource(R.string.update_dialog_title, release.version),
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    stringResource(R.string.update_dialog_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFFC107),
                                )
                            }
                        },
                        text = {
                            val isTurkishLocale = (AppCompatDelegate.getApplicationLocales()[0]
                                ?: context.resources.configuration.locales[0]).language == "tr"
                            val cleanNotes = androidx.compose.runtime.remember(release.notes, isTurkishLocale) {
                                GithubReleaseChecker.extractCleanReleaseNotes(release.notes, isTurkish = isTurkishLocale)
                            }
                            Column(Modifier.verticalScroll(rememberScrollState())) {
                                Text(cleanNotes.ifBlank { stringResource(R.string.update_dialog_no_notes) }, style = MaterialTheme.typography.bodySmall)
                                updateDownloadError?.let { Text(it, color = scheme.error, style = MaterialTheme.typography.labelSmall) }
                            }
                        },

                        confirmButton = {
                            TextButton(
                                onClick = {
                                    updateDownloading = true
                                    updateDownloadError = null
                                    scope.launch {
                                        withContext(Dispatchers.IO) { runCatching { VerifiedApkDownloader.download(context, release) } }
                                            .onSuccess { apk ->
                                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
                                                context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "application/vnd.android.package-archive")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                })
                                                showReleaseDialog = false
                                            }
                                            .onFailure { updateDownloadError = it.message ?: "APK verification failed" }
                                        updateDownloading = false
                                    }
                                },
                                enabled = !updateDownloading,
                            ) {
                                Text(if (updateDownloading) stringResource(R.string.update_check_checking) else stringResource(R.string.update_dialog_download))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showReleaseDialog = false }) {
                                Text(stringResource(R.string.close))
                            }
                        },
                    )
                }
            }

            if (showVersionHistoryDialog) {
                val isTurkishLocale = (AppCompatDelegate.getApplicationLocales()[0]
                    ?: context.resources.configuration.locales[0]).language == "tr"
                AlertDialog(
                    onDismissRequest = { showVersionHistoryDialog = false },
                    containerColor = scheme.surface,
                    titleContentColor = scheme.onSurface,
                    textContentColor = scheme.onSurfaceVariant,
                    title = {
                        Text(
                            text = stringResource(R.string.version_history_title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            io.github.omeryol.akisgesture.util.VersionHistoryProvider.HISTORY.forEach { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(scheme.surfaceVariant.copy(alpha = 0.35f))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "v${item.version} (${item.date})",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFAB00)
                                        )
                                        if (item.isCurrent) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF00C853).copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isTurkishLocale) "Mevcut Sürüm" else "Current",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF00E676),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    val notes = if (isTurkishLocale) item.changesTr else item.changesEn
                                    notes.forEach { note ->
                                        Text(
                                            text = "• $note",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = scheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showVersionHistoryDialog = false }) {
                            Text(stringResource(R.string.close))
                        }
                    }
                )
            }

            Spacer(Modifier.height(14.dp))
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
                    "" to "🌐 " + stringResource(R.string.language_system),
                    "tr" to "🇹🇷 " + stringResource(R.string.language_turkish),
                    "en" to "🇬🇧 " + stringResource(R.string.language_english),
                ).forEach { (tag, label) ->
                    val currentLocales = AppCompatDelegate.getApplicationLocales()
                    val isSelected = if (tag.isEmpty()) currentLocales.isEmpty else currentLocales.toLanguageTags().contains(tag)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF00E676).copy(alpha = 0.25f) else scheme.surfaceVariant.copy(alpha = 0.35f))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) Color(0xFF00E676) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF00E676) else scheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = scheme.outlineVariant.copy(alpha = 0.45f),
            )

            Text(
                text = stringResource(R.string.about_support),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(10.dp))

            // Akış Gesture Main GitHub Repository Card Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF24292E))
                    .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/omeryol/AkisGesture")),
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🐱", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.open_project_repo),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "omeryol / AkisGesture",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB0BEC5)
                            )
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color(0xFF7C4DFF))
                }
            }

            Spacer(Modifier.height(8.dp))

            // OpenSwipe Upstream Secondary Pill Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFAB00).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFFFFAB00).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ARCJ137442/OpenSwipe")),
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Style, contentDescription = null, tint = Color(0xFFFFAB00), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.open_upstream),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "ARCJ137442 / OpenSwipe",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color(0xFFFFAB00), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            TelegramLinkCard(
                title = stringResource(R.string.telegram_group_title),
                subtitle = stringResource(R.string.telegram_group_subtitle),
                url = "https://t.me/+ZRMewoFvaIdhM2I0",
                accent = Color(0xFF229ED9),
            )
            Spacer(Modifier.height(8.dp))
            TelegramLinkCard(
                title = stringResource(R.string.telegram_channel_title),
                subtitle = stringResource(R.string.telegram_channel_subtitle),
                url = "https://t.me/+ZTbxUGG-ynowOWE0",
                accent = Color(0xFF1677B8),
            )
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))

            // ── ROOT REHBER KARTI (Yalnızca Root Yetkisi Olan Cihazlarda Görünür) ──
            if (rootAccess == RootAccessState.AVAILABLE) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF6D00).copy(alpha = 0.08f))
                        .border(1.dp, Color(0xFFFF6D00).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.about_root_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6D00)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00C853).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.root_available),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // UYARI: Root tavsiye edilmez
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFD50000).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFD50000).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.root_warning_title),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.root_warning_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurface
                        )
                    }

                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))

                    Text(
                        text = stringResource(R.string.about_root_not_required_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676)
                    )
                    Text(
                        text = stringResource(R.string.about_root_not_required_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurface
                    )

                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))

                    Text(
                        text = stringResource(R.string.about_root_features_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.about_root_features_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))

                    Text(
                        text = stringResource(R.string.about_root_privacy_desc),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onSurface
                    )
                }
                Spacer(Modifier.height(8.dp))
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
            title = {
                Text(
                    stringResource(
                        if (config.appPauseMode == io.github.omeryol.akisgesture.gesture.AppPauseMode.BLACKLIST) {
                            R.string.paused_apps_title
                        } else {
                            R.string.paused_apps_run_title
                        }
                    )
                )
            },
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
private fun PauseWarningCard(title: String, description: String) {
    AkisGlassCard(
        modifier = Modifier.padding(top = 4.dp),
        accentTint = Color(0xFFFF6D00),
        containerColor = Color(0xFFFF6D00).copy(alpha = 0.08f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = Color(0xFFFF6D00),
                modifier = Modifier.size(22.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    color = Color(0xFFFF6D00),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun TelegramLinkCard(title: String, subtitle: String, url: String, accent: Color) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.48f), RoundedCornerShape(12.dp))
            .clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✈", style = MaterialTheme.typography.titleLarge, color = accent)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, maxLines = 1)
            }
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }

    }
}

private sealed interface UpdateCheckState {
    data object IDLE : UpdateCheckState
    data object CHECKING : UpdateCheckState
    data object CURRENT : UpdateCheckState
    data class AVAILABLE(val release: GithubRelease) : UpdateCheckState
    data class DEV_BUILD(val releaseVersion: String) : UpdateCheckState
    data object FAILED : UpdateCheckState
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
