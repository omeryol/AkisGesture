package io.github.omeryol.akisgesture.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import io.github.omeryol.akisgesture.ui.component.AkisFluidSwitch
import io.github.omeryol.akisgesture.ui.component.AkisGlassCard
import io.github.omeryol.akisgesture.ui.component.AkisSectionHeader
import io.github.omeryol.akisgesture.ui.component.InteractivePhoneMap
import io.github.omeryol.akisgesture.ui.viewmodel.HomeViewModel
import io.github.omeryol.akisgesture.util.PermissionHelper
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPermissions: () -> Unit,
    onNavigateToRules: (Edge) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val serviceState by GestureAccessibilityService.serviceState.collectAsState()
    val ruleSet by AkisGestureApp.getInstance().compiledRuleSet.collectAsState()
    val rules by viewModel.rules.collectAsState()
    val context = LocalContext.current
    val isConnected = serviceState == GestureAccessibilityService.ServiceState.CONNECTED
    val batteryOptimized = !PermissionHelper.isBatteryOptimizationIgnored(context)
    val totalRules = ruleSet.totalRuleCount()
    val activeEdges = Edge.entries.count(ruleSet::hasRulesFor)
    val scheme = MaterialTheme.colorScheme

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Pulse Animation for live service status
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    val gestureConfig by viewModel.configState.collectAsState()
    val masterEnabled = gestureConfig.masterEnabled
    val isMasterActive = isConnected && masterEnabled

    Scaffold(
        containerColor = scheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── 1. Live Master Service Status Card ──
            AkisGlassCard(
                onClick = {
                    if (!isConnected) {
                        onNavigateToPermissions()
                    } else {
                        viewModel.toggleMaster(!isMasterActive)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                accentTint = if (isMasterActive) scheme.primary else if (!isConnected) scheme.error else scheme.outline,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .scale(if (isMasterActive) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                if (isMasterActive) scheme.primary
                                else if (!isConnected) scheme.error
                                else scheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (!isConnected) Icons.Filled.Shield
                            else if (isMasterActive) Icons.Filled.Check
                            else Icons.Filled.Pause,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (!isConnected) stringResource(R.string.home_service_off)
                                else if (isMasterActive) stringResource(R.string.home_active)
                                else "Akış Durduruldu ⏸️",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface,
                            )
                            if (isMasterActive) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E676)),
                                )
                            }
                        }
                        Text(
                            text = if (!isConnected) {
                                stringResource(R.string.home_enable_accessibility)
                            } else if (isMasterActive) {
                                stringResource(R.string.home_summary, totalRules, activeEdges, BuildConfig.VERSION_NAME)
                            } else {
                                "Jest algılama durduruldu · Dokunarak başlatın"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }

                    AkisFluidSwitch(
                        checked = isMasterActive,
                        onCheckedChange = { checked ->
                            if (!isConnected) {
                                onNavigateToPermissions()
                            } else {
                                viewModel.toggleMaster(checked)
                            }
                        },
                    )
                }
            }

            // ── 2. Redesigned Interactive Phone Map ──
            AkisGlassCard(
                onClick = { onNavigateToRules(Edge.LEFT) },
                modifier = Modifier.fillMaxWidth(),
                accentTint = scheme.primary,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AkisSectionHeader(
                            title = stringResource(R.string.edge_map),
                            subtitle = stringResource(R.string.map_expanded_hint),
                            icon = Icons.Filled.Smartphone,
                        )
                        Text(
                            text = stringResource(R.string.edit_arrow),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary,
                        )
                    }
                    InteractivePhoneMap(
                        rules = rules,
                        onZoneClick = { zone -> onNavigateToRules(zone.edge) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                    )
                }
            }

            // ── 3. Real Usage Gesture Summary Chart Card ──
            AkisSummaryChartCard()

            // ── 4. Hazır Şablonlar Carousel (Preset Templates) ──
            AkisGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentTint = scheme.secondary,
            ) {
                AkisSectionHeader(
                    title = "✨ Hazır Jest Şablonları",
                    subtitle = "Tek tıkla zengin jest düzeni yükleyin",
                    icon = Icons.Filled.AutoAwesome,
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val presetList = listOf(
                        Triple("✨ Genel · Dengeli", "Dengeli günlük kullanım", Presets.DEFAULT),
                        Triple("📱 Tek Elle Kullanım", "Sağ kenarda Geri ve Bildirimler", Presets.ONE_HAND_RIGHT),
                        Triple("📐 Klasik Android", "Alt kenarda Geri, Ana Sayfa, Son", Presets.ANDROID_CLASSIC),
                        Triple("⚡ Gelişmiş · Çift Kenar", "Her iki kenarda hızlı jestler", Presets.DUAL_EDGE_ADVANCED),
                        Triple("🎵 Medya Kontrolü", "Ses ve parça değiştirme kısayolları", Presets.MEDIA_CONTROL),
                    )

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
                                OutlinedButton(
                                    onClick = {
                                        viewModel.applyPresetGraph(graph)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("'$title' şablonu yüklendi!")
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 4.dp),
                                ) {
                                    Text("Uygula", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // ── 5. Battery Optimization Notice ──
            if (batteryOptimized) {
                AkisGlassCard(
                    onClick = { PermissionHelper.requestIgnoreBatteryOptimization(context) },
                    modifier = Modifier.fillMaxWidth(),
                    accentTint = scheme.error,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("⚡", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.disable_battery_restriction),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.error,
                            )
                            Text(
                                text = stringResource(R.string.battery_restriction_description),
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
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
            title = "📊 Gerçek Kullanım ve Kenar Dağılım Grafiği",
            subtitle = if (totalCount > 0) "$totalCount kez canlı jest çalıştırıldı" else "Canlı sayım aktif · Jest yaptıkça sayaç güncellenir",
            icon = Icons.Filled.BarChart,
        )
        Spacer(Modifier.height(12.dp))

        // ── 1. Kenar Dağılım Çubuğu ──
        Text(
            text = "Kenar Dağılımı",
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
                        .background(Color(0xFF3D5AFE)),
                )
            }
            if (rightCount > 0 || totalCount == 0) {
                Box(
                    modifier = Modifier
                        .weight(rightWeight)
                        .fillMaxHeight()
                        .background(Color(0xFF00E676)),
                )
            }
            if (bottomCount > 0 || totalCount == 0) {
                Box(
                    modifier = Modifier
                        .weight(bottomWeight)
                        .fillMaxHeight()
                        .background(Color(0xFFFF9100)),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ChartLegendItem(color = Color(0xFF3D5AFE), label = "Sol Kenar", count = leftCount)
            ChartLegendItem(color = Color(0xFF00E676), label = "Sağ Kenar", count = rightCount)
            ChartLegendItem(color = Color(0xFFFF9100), label = "Alt Kenar", count = bottomCount)
        }

        Spacer(Modifier.height(14.dp))

        // ── 2. Tetikleyici Türü İlerleme Çubukları ──
        Text(
            text = "Hareket Türü Analizi",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GestureTypeBarRow(title = "⚡ Hızlı Çekme", count = quickCount, total = totalCount, color = Color(0xFF3D5AFE))
            GestureTypeBarRow(title = "⏱️ Çek ve Tut", count = holdCount, total = totalCount, color = Color(0xFFD500F9))
            GestureTypeBarRow(title = "↗️ L-Swipe", count = lCount, total = totalCount, color = Color(0xFF00E5FF))
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
            Text("$count jest", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
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
