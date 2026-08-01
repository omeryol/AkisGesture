package io.github.omeryol.akisgesture.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.AkisGestureApp
import io.github.omeryol.akisgesture.overlay.Edge
import io.github.omeryol.akisgesture.service.GestureAccessibilityService
import io.github.omeryol.akisgesture.ui.component.AkisGlassCard
import io.github.omeryol.akisgesture.ui.component.InteractivePhoneMap
import io.github.omeryol.akisgesture.ui.viewmodel.HomeViewModel
import io.github.omeryol.akisgesture.util.PermissionHelper

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ── Service Status Pill Card ──
        AkisGlassCard(
            onClick = { if (isConnected) onNavigateToRules(Edge.LEFT) else onNavigateToPermissions() },
            modifier = Modifier.fillMaxWidth(),
            accentTint = if (isConnected) Color(0xFF00E676) else scheme.error,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isConnected) Color(0xFF00E676) else scheme.error,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Filled.Check else Icons.Filled.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (isConnected) "Akış Aktif" else "Servis Kapalı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = if (isConnected) "$totalRules Hareket · $activeEdges Aktif Kenar · v1.1.0" else "Erişilebilirlik iznini aktifleştirin",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── Realistic Phone Map with photo frame ──
        AkisGlassCard(
            onClick = { onNavigateToRules(Edge.LEFT) },
            modifier = Modifier.fillMaxWidth(),
            accentTint = scheme.primary,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📱 Kenar Haritası",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = "Düzenle →",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.primary,
                        modifier = Modifier.padding(2.dp)
                    )
                }
                InteractivePhoneMap(
                    rules = rules,
                    onZoneClick = { zone -> onNavigateToRules(zone.edge) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                )
            }
        }

        // ── Compact Quick Metric Tiles ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AkisGlassCard(
                onClick = { onNavigateToRules(Edge.LEFT) },
                modifier = Modifier.weight(1f),
                accentTint = Color(0xFF3D5AFE),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📐 Sol & Sağ", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${ruleSet.ruleCountFor(Edge.LEFT) + ruleSet.ruleCountFor(Edge.RIGHT)} Hareket",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            AkisGlassCard(
                onClick = { onNavigateToRules(Edge.BOTTOM) },
                modifier = Modifier.weight(1f),
                accentTint = Color(0xFFFF9100),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔽 Alt Kenar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${ruleSet.ruleCountFor(Edge.BOTTOM)} Hareket",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── Battery Optimization Notice ──
        if (batteryOptimized) {
            AkisGlassCard(
                onClick = { PermissionHelper.requestIgnoreBatteryOptimization(context) },
                modifier = Modifier.fillMaxWidth(),
                accentTint = scheme.error,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Pil Kısıtlamasını Kaldır",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.error
                        )
                        Text(
                            text = "Arka planda kesintisiz çalışması için pil optimizasyonunu kapatın",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
