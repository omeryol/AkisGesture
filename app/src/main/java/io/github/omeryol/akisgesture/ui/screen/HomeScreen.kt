package io.github.omeryol.akisgesture.ui.screen

import android.util.Log
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.AkisGestureApp
import io.github.omeryol.akisgesture.BuildConfig
import io.github.omeryol.akisgesture.R
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.rule.Presets
import io.github.omeryol.akisgesture.service.GestureAccessibilityService
import io.github.omeryol.akisgesture.ui.component.AkisGlassCard
import io.github.omeryol.akisgesture.ui.component.AkisFluidSwitch
import io.github.omeryol.akisgesture.ui.component.AkisFlowGlyph
import io.github.omeryol.akisgesture.ui.component.AkisSectionHeader
import io.github.omeryol.akisgesture.ui.component.InteractivePhoneMap
import io.github.omeryol.akisgesture.ui.viewmodel.HomeViewModel
import io.github.omeryol.akisgesture.ui.theme.EdgeUi
import io.github.omeryol.akisgesture.ui.theme.StatusConnected
import io.github.omeryol.akisgesture.ui.theme.StatusDisconnected
import io.github.omeryol.akisgesture.util.PermissionHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPermissions: () -> Unit,
    onNavigateToRules: (Edge) -> Unit,
    modifier: Modifier = Modifier,
) {
    val serviceState by GestureAccessibilityService.serviceState.collectAsState()
    val app = AkisGestureApp.getInstance()
    val ruleSet by app.compiledRuleSet.collectAsState()
    val rules by app.activeRuleGraph.collectAsState()
    val context = LocalContext.current
    val isConnected = serviceState == GestureAccessibilityService.ServiceState.CONNECTED
    val batteryOptimized = !PermissionHelper.isBatteryOptimizationIgnored(context)
    var accessibilityGranted by remember { mutableStateOf(PermissionHelper.isAccessibilityServiceEnabled(context)) }
    var writeSettingsGranted by remember { mutableStateOf(PermissionHelper.canWriteSystemSettings(context)) }
    var batteryExemptionGranted by remember { mutableStateOf(PermissionHelper.isBatteryOptimizationIgnored(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        accessibilityGranted = PermissionHelper.isAccessibilityServiceEnabled(context)
        writeSettingsGranted = PermissionHelper.canWriteSystemSettings(context)
        batteryExemptionGranted = PermissionHelper.isBatteryOptimizationIgnored(context)
    }
    val gestureConfig by viewModel.configState.collectAsState()
    val ringActionCount = Edge.entries.sumOf { gestureConfig.ringActionsFor(it).size }
    val totalActions = ruleSet.totalRuleCount() + ringActionCount
    val activeEdges = Edge.entries.count { edge -> ruleSet.hasRulesFor(edge) || gestureConfig.hasRingActionsFor(edge) }
    val scheme = MaterialTheme.colorScheme

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val masterEnabled = gestureConfig.masterEnabled
    val isMasterActive = isConnected && masterEnabled

    val visibleState = remember {
        androidx.compose.animation.core.MutableTransitionState(false).apply { targetState = true }
    }

    Scaffold(
        containerColor = scheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        androidx.compose.animation.AnimatedVisibility(
            visibleState = visibleState,
            enter = androidx.compose.animation.fadeIn(tween(400)) + androidx.compose.animation.slideInVertically(
                initialOffsetY = { 80 },
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.78f,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                ),
            ),
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
            // ── 1. Live Master Service Status Hero Card ──
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onBackground,
                modifier = Modifier.padding(start = 2.dp),
            )

            val statusColor = when {
                !isConnected -> Color(0xFFFF3D00)
                isMasterActive -> Color(0xFF00E676)
                else -> Color(0xFFFFB300)
            }

            AkisGlassCard(
                onClick = if (!isConnected) {
                    { PermissionHelper.openAccessibilitySettings(context) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                accentTint = statusColor,
                containerColor = statusColor.copy(alpha = 0.08f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(statusColor),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = when {
                                        !isConnected -> Icons.Filled.Shield
                                        isMasterActive -> Icons.Filled.Check
                                        else -> Icons.Filled.Pause
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when {
                                        !isConnected -> stringResource(R.string.home_service_off)
                                        isMasterActive -> stringResource(R.string.home_active)
                                        else -> stringResource(R.string.stream_paused)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurface,
                                )
                                Spacer(Modifier.width(8.dp))
                                PulseBeaconDot(color = statusColor)
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = when {
                                    !isConnected -> stringResource(R.string.home_enable_accessibility)
                                    isMasterActive -> stringResource(R.string.home_summary, totalActions, activeEdges, BuildConfig.VERSION_NAME)
                                    else -> stringResource(R.string.stream_paused_hint)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }

                        if (isConnected) {
                            AkisFluidSwitch(
                                checked = isMasterActive,
                                activeColor = Color(0xFF00E676),
                                onCheckedChange = { checked ->
                                    viewModel.toggleMaster(checked)
                                },
                            )
                        }
                    }

                    // Live active edge chips when service is active
                    if (isMasterActive && activeEdges > 0) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (ruleSet.hasRulesFor(Edge.LEFT) || gestureConfig.hasRingActionsFor(Edge.LEFT)) {
                                EdgeIndicatorPill(name = stringResource(R.string.edge_left), color = EdgeUi.color(Edge.LEFT))
                            }
                            if (ruleSet.hasRulesFor(Edge.RIGHT) || gestureConfig.hasRingActionsFor(Edge.RIGHT)) {
                                EdgeIndicatorPill(name = stringResource(R.string.edge_right), color = EdgeUi.color(Edge.RIGHT))
                            }
                            if (ruleSet.hasRulesFor(Edge.BOTTOM) || gestureConfig.hasRingActionsFor(Edge.BOTTOM)) {
                                EdgeIndicatorPill(name = stringResource(R.string.edge_bottom), color = EdgeUi.color(Edge.BOTTOM))
                            }
                        }
                    }

                    // 1-Tap Action Button when Service is OFF
                    if (!isConnected) {
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { PermissionHelper.openAccessibilitySettings(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3D00)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            Icon(Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.open_permission_steps), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── 2. Unified Intelligent Diagnostic & Health Hub ──
            val allRecommendedPermissionsGranted = accessibilityGranted && writeSettingsGranted && batteryExemptionGranted
            if (!allRecommendedPermissionsGranted) {
                DiagnosticHealthCard(
                    accessibilityGranted = accessibilityGranted,
                    batteryExemptionGranted = batteryExemptionGranted,
                    writeSettingsGranted = writeSettingsGranted,
                    onOpenAccessibility = { PermissionHelper.openAccessibilitySettings(context) },
                    onOpenBattery = { PermissionHelper.requestIgnoreBatteryOptimization(context) },
                    onOpenWriteSettings = { PermissionHelper.openWriteSettings(context) },
                    onOpenFullGuide = onNavigateToPermissions,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ── 3. Redesigned Interactive Phone Map ──
            if (gestureConfig.showPhoneMap) {
                AkisGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentTint = scheme.primary,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AkisSectionHeader(
                            title = stringResource(R.string.edge_map),
                            flowGlyph = AkisFlowGlyph.EDGE_MAP,
                        )
                        InteractivePhoneMap(
                            rules = rules.rules,
                            onSideRangeChange = viewModel::setEdgeVerticalRange,
                            onSideRangePreview = { edge, start, end ->
                                GestureAccessibilityService.instance?.previewEdgeVerticalRange(edge, start, end)
                            },
                            onEdgeClick = onNavigateToRules,
                            modifier = Modifier.fillMaxWidth(),
                            iconPack = gestureConfig.actionIconPack,
                            config = gestureConfig,
                        )
                    }
                }
            }

            // ── 3. Real Usage Gesture Summary Chart Card ──
            if (gestureConfig.showSummaryChart) {
                AkisSummaryChartCard()
            }

            // ── 4. Hazır Şablonlar Carousel (Preset Templates) ──
            if (gestureConfig.showPresetsCard) {
                val presetNames = stringArrayResource(R.array.preset_names)
                val presetDescriptions = stringArrayResource(R.array.preset_descriptions)
                AkisGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentTint = scheme.secondary,
                ) {
                    AkisSectionHeader(
                        title = stringResource(R.string.show_presets_card),
                        subtitle = stringResource(R.string.show_presets_card_subtitle),
                        flowGlyph = AkisFlowGlyph.PRESETS,
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        val presetList = Presets.ALL.mapIndexed { index, (_, graph) ->
                            Triple(presetNames[index], presetDescriptions[index], graph)
                        }

                        presetList.forEach { (title, desc, graph) ->
                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(scheme.surfaceVariant.copy(alpha = 0.40f))
                                    .padding(12.dp),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = scheme.onSurfaceVariant,
                                        maxLines = 2,
                                        modifier = Modifier.height(32.dp),
                                    )
                                     Spacer(Modifier.height(8.dp))
                                     val appliedMessage = stringResource(R.string.template_applied, title)
                                     OutlinedButton(
                                        onClick = {
                                            viewModel.applyPresetGraph(graph)
                                            scope.launch {
                                                 snackbarHostState.showSnackbar(appliedMessage)
                                            }
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                    ) {
                                        Text(stringResource(R.string.apply), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
}

@Composable
private fun PulseBeaconDot(color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(modifier = modifier.size(14.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(7.dp * scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
private fun EdgeIndicatorPill(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun DiagnosticHealthCard(
    accessibilityGranted: Boolean,
    batteryExemptionGranted: Boolean,
    writeSettingsGranted: Boolean,
    onOpenAccessibility: () -> Unit,
    onOpenBattery: () -> Unit,
    onOpenWriteSettings: () -> Unit,
    onOpenFullGuide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val cardColor = when {
        !accessibilityGranted -> Color(0xFFFF3D00)
        !batteryExemptionGranted -> Color(0xFFFFB300)
        else -> scheme.tertiary
    }
    var showHyperOSTip by remember { mutableStateOf(false) }

    AkisGlassCard(
        modifier = modifier,
        accentTint = cardColor,
        containerColor = cardColor.copy(alpha = 0.05f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(cardColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (!accessibilityGranted) Icons.Filled.Shield else Icons.Filled.BatteryAlert,
                        contentDescription = null,
                        tint = cardColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.quick_start_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.quick_start_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }

            // 1. Accessibility Service Row
            DiagnosticItemRow(
                title = stringResource(R.string.accessibility_service),
                detail = if (accessibilityGranted) stringResource(R.string.permission_ready) else stringResource(R.string.quick_start_accessibility_missing),
                granted = accessibilityGranted,
                actionLabel = stringResource(R.string.open_permission_steps),
                onAction = onOpenAccessibility,
                isCritical = true,
            )

            // 2. Battery Exemption Row (Background Keep-Alive)
            DiagnosticItemRow(
                title = stringResource(R.string.battery_permission_title),
                detail = if (batteryExemptionGranted) stringResource(R.string.permission_ready) else stringResource(R.string.quick_start_battery_missing),
                granted = batteryExemptionGranted,
                actionLabel = stringResource(R.string.disable_battery_restriction),
                onAction = onOpenBattery,
                isCritical = false,
            )

            // 3. Write System Settings Row
            DiagnosticItemRow(
                title = stringResource(R.string.write_settings_permission_title),
                detail = if (writeSettingsGranted) stringResource(R.string.permission_ready) else stringResource(R.string.quick_start_write_settings_missing),
                granted = writeSettingsGranted,
                actionLabel = stringResource(R.string.open_permission_steps),
                onAction = onOpenWriteSettings,
                isCritical = false,
            )

            // 4. Background Termination Tip (Xiaomi / HyperOS / MIUI)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(scheme.surfaceVariant.copy(alpha = 0.35f))
                    .clickable { showHyperOSTip = !showHyperOSTip }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💡", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Arka planda kapanmayı önleme ipucu",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurface,
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    if (showHyperOSTip) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Xiaomi / HyperOS veya agresif pil yöneticisi bulunan cihazlarda servisin durdurulmaması için: Son Uygulamalar ekranında Akış Gesture kartını kilitleyin ve Otomatik Başlatma iznini verin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tüm İzin Adımlarını Gör ›",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary,
                            modifier = Modifier.clickable { onOpenFullGuide() },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItemRow(
    title: String,
    detail: String,
    granted: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    isCritical: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val statusColor = if (granted) Color(0xFF00E676) else if (isCritical) Color(0xFFFF3D00) else Color(0xFFFFB300)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(scheme.surfaceVariant.copy(alpha = 0.20f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (granted) Icons.Filled.Check else Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(14.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (granted) statusColor else scheme.onSurfaceVariant,
                )
            }
        }

        if (!granted) {
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp),
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AkisSummaryChartCard(
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val totalExecutions by io.github.omeryol.akisgesture.util.GestureTracker.totalExecutions.collectAsState()
    val edgeCounts by io.github.omeryol.akisgesture.util.GestureTracker.edgeCounts.collectAsState()
    val typeCounts by io.github.omeryol.akisgesture.util.GestureTracker.typeCounts.collectAsState()

    val leftCount = edgeCounts[Edge.LEFT] ?: 0
    val rightCount = edgeCounts[Edge.RIGHT] ?: 0
    val bottomCount = edgeCounts[Edge.BOTTOM] ?: 0

    val quickCount = typeCounts[GestureType.QUICK_SWIPE] ?: 0
    val holdCount = typeCounts[GestureType.SWIPE_HOLD] ?: 0
    val lCount = (typeCounts[GestureType.SWIPE_UP_L] ?: 0) + (typeCounts[GestureType.SWIPE_DOWN_L] ?: 0)

    val totalCount = totalExecutions

    val leftWeight = if (totalCount > 0) (leftCount.toFloat() / totalCount).coerceAtLeast(0.001f) else 0.33f
    val rightWeight = if (totalCount > 0) (rightCount.toFloat() / totalCount).coerceAtLeast(0.001f) else 0.33f
    val bottomWeight = if (totalCount > 0) (bottomCount.toFloat() / totalCount).coerceAtLeast(0.001f) else 0.33f

    AkisGlassCard(
        modifier = modifier.fillMaxWidth(),
        accentTint = scheme.tertiary,
    ) {
        AkisSectionHeader(
            title = stringResource(R.string.live_chart_title),
            subtitle = if (totalCount > 0) stringResource(R.string.live_chart_subtitle, totalCount) else stringResource(R.string.live_chart_empty),
            flowGlyph = AkisFlowGlyph.SUMMARY,
        )
        Spacer(Modifier.height(12.dp))

        // ── 1. Kenar Dağılım Çubuğu ──
        Text(
            text = stringResource(R.string.edge_distribution),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(scheme.surfaceVariant.copy(alpha = 0.4f)),
        ) {
            if (leftCount > 0 || totalCount == 0) {
                Box(
                    modifier = Modifier
                        .weight(leftWeight)
                        .fillMaxHeight()
                        .background(EdgeUi.color(Edge.LEFT)),
                )
            }
            if (bottomCount > 0 || totalCount == 0) {
                Box(
                    modifier = Modifier
                        .weight(bottomWeight)
                        .fillMaxHeight()
                        .background(EdgeUi.color(Edge.BOTTOM)),
                )
            }
            if (rightCount > 0 || totalCount == 0) {
                Box(
                    modifier = Modifier
                        .weight(rightWeight)
                        .fillMaxHeight()
                        .background(EdgeUi.color(Edge.RIGHT)),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ChartLegendItem(color = EdgeUi.color(Edge.LEFT), label = stringResource(R.string.edge_left), count = leftCount)
            ChartLegendItem(color = EdgeUi.color(Edge.BOTTOM), label = stringResource(R.string.edge_bottom), count = bottomCount)
            ChartLegendItem(color = EdgeUi.color(Edge.RIGHT), label = stringResource(R.string.edge_right), count = rightCount)
        }

        Spacer(Modifier.height(14.dp))

        // ── 2. Tetikleyici Türü İlerleme Çubukları ──
        Text(
            text = stringResource(R.string.gesture_type),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GestureTypeBarRow(title = "⚡ ${stringResource(R.string.gesture_quick)}", count = quickCount, total = totalCount, color = EdgeUi.color(Edge.LEFT))
            GestureTypeBarRow(title = "⏱️ ${stringResource(R.string.gesture_hold)}", count = holdCount, total = totalCount, color = EdgeUi.color(Edge.RIGHT))
            GestureTypeBarRow(title = "↗️ ${stringResource(R.string.gesture_l_up)}", count = lCount, total = totalCount, color = EdgeUi.color(Edge.BOTTOM))
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun GestureTypeBarRow(title: String, count: Int, total: Int, color: Color) {
    val scheme = MaterialTheme.colorScheme
    val fraction = if (total > 0) (count.toFloat() / total).coerceIn(0f, 1f) else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
            Text(stringResource(R.string.gesture_count_label, count), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(scheme.surfaceVariant.copy(alpha = 0.4f)),
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color),
                )
            }
        }
    }
}
