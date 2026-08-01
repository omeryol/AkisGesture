package io.github.omeryol.akisgesture.ui.screen

import android.graphics.Color as AndroidColor
import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Swipe
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.omeryol.akisgesture.AkisGestureApp
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

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

    val context = LocalContext.current
    val app = context.applicationContext as AkisGestureApp
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme

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
                    } ?: error("Dosya açılamadı")
                }
            }.onSuccess {
                Toast.makeText(context, "Yedek kaydedildi", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, it.message ?: "Yedek kaydedilemedi", Toast.LENGTH_LONG).show()
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
                    } ?: error("Dosya açılamadı")
                }
                pendingImportJson = json
            }.onFailure {
                Toast.makeText(context, it.message ?: "Yedek yüklenemedi", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AkisGlassCard(accentTint = if (serviceState == GestureAccessibilityService.ServiceState.CONNECTED) {
            Color(0xFF00C853)
        } else {
            Color(0xFFFF9100)
        }) {
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
                                Color(0xFFFFB300)
                            }
                        )
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (serviceState == GestureAccessibilityService.ServiceState.CONNECTED) {
                            "Hareket hizmeti hazır"
                        } else {
                            "Hareket hizmeti bağlı değil"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                    Text(
                        text = if (serviceState == GestureAccessibilityService.ServiceState.CONNECTED) {
                            "Kenar hareketleri bu kullanıcı için etkin."
                        } else {
                            "Kenar hareketlerini etkinleştirmek için izin verin."
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
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("İzin ver", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // ── 1. KENAR HASSASİYETİ VE HAREKET FİZİĞİ ──
        AkisGlassCard(accentTint = Color(0xFF3D5AFE)) {
            AkisSectionHeader(
                title = "Kenar ve Hareket Fiziği",
                subtitle = "Tetik alanları, hassasiyet ve bekleme ayarları",
                icon = Icons.Filled.Swipe
            )
            Spacer(Modifier.height(10.dp))

            // Edge Selection Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(Edge.LEFT to "Sol Kenar", Edge.RIGHT to "Sağ Kenar", Edge.BOTTOM to "Alt Kenar").forEach { (edge, label) ->
                    val selected = selectedEdge == edge
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) scheme.primary else scheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { selectedEdge = edge }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) scheme.onPrimary else scheme.onSurface
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
                title = "Tetik Kalınlığı",
                valueText = "${currentWidth.roundToInt()} dp",
                value = currentWidth,
                valueRange = 8f..60f,
                onValueChange = { viewModel.setEdgeTriggerSize(selectedEdge, it) }
            )
            Text(
                text = "Tetik: Kenardaki dokunma alanının kaplama kalınlığı (genişliği).",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            AkisSliderRow(
                title = "Hassasiyet (Sönümleme)",
                valueText = "%.1fx".format(currentDamping),
                value = currentDamping,
                valueRange = 0.5f..4.0f,
                onValueChange = { viewModel.setEdgeDamping(selectedEdge, it) }
            )
            Text(
                text = "Hassasiyet: Sürükleme direnci. Düşük sönümleme = hassas tepki; Yüksek = daha fazla sürükleme gerektirir.",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            AkisSliderRow(
                title = "Eşik Mesafesi",
                valueText = "${currentThreshold.roundToInt()} dp",
                value = currentThreshold,
                valueRange = 8f..40f,
                onValueChange = { viewModel.setEdgeSwipeThreshold(selectedEdge, it) }
            )
            Text(
                text = "Eşik: Hızlı çekmenin tetiklenmesi için kat edilmesi gereken minimum kaydırma mesafesi.",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            AkisSliderRow(
                title = "L-Swipe Bükülme Eşiği",
                valueText = "${config.lSwipeThresholdDp.roundToInt()} dp",
                value = config.lSwipeThresholdDp,
                valueRange = 15f..60f,
                onValueChange = { viewModel.setLSwipeThreshold(it) }
            )
            Text(
                text = "L-Eşiği: L hareketinin algılanması için gereken dikey bükülme mesafesi (Dikey kaydırma, yatay kaydırmanın en az 1 katı olmalıdır).",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            HorizontalDivider(Modifier.padding(vertical = 6.dp), color = scheme.outlineVariant.copy(alpha = 0.3f))

            AkisSliderRow(
                title = "Çekip Bekletme Süresi",
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
                    text = "Bekletme Çalışma Modu",
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
                                .background(if (active) scheme.primaryContainer else Color.Transparent)
                                .clickable { viewModel.setHoldFireMode(mode) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) scheme.onPrimaryContainer else scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ── 2. GÖRSEL VE DOKUNSAL GERİ BİLDİRİM ──
        AkisGlassCard(accentTint = Color(0xFF00E676)) {
            AkisSectionHeader(
                title = "Görsel ve Dokunsal Geri Bildirim",
                subtitle = "Animasyon stili, renk uzayı, saydamlık ve dokunsal titreşim",
                icon = Icons.Filled.Palette
            )
            Spacer(Modifier.height(10.dp))

            // Animation Style Selector Grid (All 8 animations)
            Text(
                text = "Animasyon Stili",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            val anims = FeedbackAnimation.entries.filter { it != FeedbackAnimation.NONE }
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
                                .background(if (selected) scheme.primaryContainer else scheme.surfaceVariant.copy(alpha = 0.35f))
                                .border(
                                    width = if (selected) 1.5.dp else 0.dp,
                                    color = if (selected) scheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setFeedbackAnimation(anim) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = anim.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
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

            // 1. Birincil Hareket Rengi (Hızlı Çekme) - Infinite Custom Color Picker
            AkisInfiniteColorPicker(
                title = "Birincil Hareket Rengi (Hızlı Çekme)",
                currentColorArgb = config.feedbackColorArgb,
                onColorChanged = viewModel::setFeedbackColor
            )

            Spacer(Modifier.height(8.dp))

            // 2. İkincil Hareket Rengi (Çekip Bekletme) - Infinite Custom Color Picker
            AkisInfiniteColorPicker(
                title = "İkincil Hareket Rengi (Çekip Bekletme)",
                currentColorArgb = config.secondaryColorArgb,
                onColorChanged = viewModel::setSecondaryColor
            )

            Spacer(Modifier.height(8.dp))

            AkisSwitchRow(
                title = "Uygulamaya Duyarlı Otomatik Renk",
                subtitle = "Öndeki uygulamanın baskın simge rengini otomatik kullan",
                checked = config.useAppAdaptiveColor,
                onCheckedChange = viewModel::setUseAppAdaptiveColor
            )

            Spacer(Modifier.height(8.dp))

            AkisSliderRow(
                title = "Görünürlük (Saydamlık)",
                valueText = "%${(config.feedbackOpacity * 100).roundToInt()}",
                value = config.feedbackOpacity,
                valueRange = 0.1f..1.0f,
                onValueChange = viewModel::setFeedbackOpacity
            )

            AkisSliderRow(
                title = "Animasyon Hızı",
                valueText = "%.1fx".format(config.animationSpeed),
                value = config.animationSpeed,
                valueRange = 0.5f..2.0f,
                onValueChange = viewModel::setAnimationSpeed
            )

            AkisSliderRow(
                title = "Animasyon Boyutu",
                valueText = "%.1fx".format(config.animationSize),
                value = config.animationSize,
                valueRange = 0.5f..2.0f,
                onValueChange = viewModel::setAnimationSize
            )

            HorizontalDivider(Modifier.padding(vertical = 6.dp), color = scheme.outlineVariant.copy(alpha = 0.3f))

            AkisSliderRow(
                title = "Titreşim Şiddeti",
                valueText = "%${(config.hapticIntensity * 100).roundToInt()}",
                value = config.hapticIntensity,
                valueRange = 0.0f..1.0f,
                onValueChange = viewModel::setHapticIntensity
            )

            AkisSwitchRow(
                title = "Tıklama Sesi",
                subtitle = "Hareket tetiklendiğinde kısa ton sesi çal",
                checked = config.hapticSoundEnabled,
                onCheckedChange = viewModel::setHapticSoundEnabled
            )
        }

        // ── 3. ÇALIŞMA VE DURAKLATMA KURALLARI ──
        AkisGlassCard(accentTint = Color(0xFFFF9100)) {
            AkisSectionHeader(
                title = "Çalışmayacağı Yerler",
                subtitle = "Özel durum ve uygulamalarda hareketleri otomatik kapat",
                icon = Icons.Filled.Security
            )
            Spacer(Modifier.height(6.dp))

            AkisSwitchRow(
                title = "Kilit Ekranı",
                subtitle = "Cihaz kilitliyken hareketleri duraklat",
                checked = config.pauseOnLockScreen,
                onCheckedChange = viewModel::setPauseOnLockScreen
            )

            AkisSwitchRow(
                title = "Klavye Açıkken",
                subtitle = "Metin girerken kenar dokunuşlarını yoksay",
                checked = config.pauseWhenKeyboardVisible,
                onCheckedChange = viewModel::setPauseWhenKeyboardVisible
            )

            AkisSwitchRow(
                title = "Yatay Ekran",
                subtitle = "Ekran yatay konumdayken hareketleri duraklat",
                checked = config.pauseInLandscape,
                onCheckedChange = viewModel::setPauseInLandscape
            )

            AkisSwitchRow(
                title = "Tam Ekran Moda Geçince",
                subtitle = "Video ve immersive tam ekran uygulamalarda kapat",
                checked = config.pauseOnFullScreen,
                onCheckedChange = viewModel::setPauseOnFullScreen
            )

            AkisSwitchRow(
                title = "İzin ve Güvenlik Ekranları",
                subtitle = "APK yükleme ve izin pencerelerinde kapat",
                checked = config.pauseOnPermissionScreen,
                onCheckedChange = viewModel::setPauseOnPermissionScreen
            )

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
                        text = "Uygulama Engelleri",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = if (pausedPackages.isEmpty()) "Hiçbir uygulamada duraklatılmıyor" else "${pausedPackages.size} uygulamada kapalı",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Filled.ChevronRight, null, tint = scheme.onSurfaceVariant)
            }
        }

        // ── 4. YEDEKLEME VE SİSTEM ──
        AkisGlassCard(accentTint = Color(0xFFD500F9)) {
            AkisSectionHeader(
                title = "Yedekleme ve Sistem",
                subtitle = "Kurallar, uygulama profilleri, ayarlar ve servis tercihi",
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
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Yedekle", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { importBackup.launch(arrayOf("application/json", "text/plain")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Restore, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Yükle", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Root Durumu",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = when (rootAccess) {
                        RootAccessState.CHECKING -> "Denetleniyor..."
                        RootAccessState.AVAILABLE -> "Root Var (Aktif)"
                        RootAccessState.UNAVAILABLE -> "Root Yok"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (rootAccess == RootAccessState.AVAILABLE) Color(0xFF00E676) else scheme.onSurfaceVariant
                )
            }
        }
    }

    // Backup Confirmation Dialog
    pendingImportJson?.let { json ->
        AlertDialog(
            onDismissRequest = { pendingImportJson = null },
            title = { Text("Yedeği yüklemek istiyor musunuz?") },
            text = { Text("Mevcut tüm hareket kuralları ve ayarlar seçtiğiniz yedekle değiştirilecek.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingImportJson = null
                    scope.launch {
                        runCatching { SettingsBackupManager.import(app, json) }
                            .onSuccess { Toast.makeText(context, "Yedek yüklendi", Toast.LENGTH_SHORT).show() }
                            .onFailure { Toast.makeText(context, it.message ?: "Hata", Toast.LENGTH_LONG).show() }
                    }
                }) { Text("Yükle", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportJson = null }) { Text("Vazgeç") }
            }
        )
    }

    // App Pause Selection Dialog
    if (showAppPicker) {
        AlertDialog(
            onDismissRequest = { showAppPicker = false },
            title = { Text("Hareketlerin Duraklatılacağı Uygulamalar") },
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
                TextButton(onClick = { showAppPicker = false }) { Text("Tamam", fontWeight = FontWeight.Bold) }
            }
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
    val hsv = remember(currentColorArgb) {
        FloatArray(3).also { AndroidColor.colorToHSV(currentColorArgb, it) }
    }
    val hexCode = String.format("#%06X", 0xFFFFFF and currentColorArgb)
    val scheme = MaterialTheme.colorScheme

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
                        .background(Color(currentColorArgb))
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
                        text = "HEX: $hexCode | Hue: ${hsv[0].toInt()}°",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "Renk Seçici Barı",
                    tint = scheme.onSurface
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                // Hue Slider (0 - 360)
                Text(
                    text = "Renk Tonu (Hue Bar): ${hsv[0].toInt()}°",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Slider(
                    value = hsv[0],
                    onValueChange = { newHue ->
                        val newColor = AndroidColor.HSVToColor(floatArrayOf(newHue, hsv[1].coerceAtLeast(0.1f), hsv[2].coerceAtLeast(0.1f)))
                        onColorChanged(newColor)
                    },
                    valueRange = 0f..360f
                )

                // Saturation Slider (0.0 - 1.0)
                Text(
                    text = "Doygunluk: %${(hsv[1] * 100).toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurface
                )
                Slider(
                    value = hsv[1],
                    onValueChange = { newSat ->
                        val newColor = AndroidColor.HSVToColor(floatArrayOf(hsv[0], newSat, hsv[2]))
                        onColorChanged(newColor)
                    },
                    valueRange = 0f..1f
                )

                // Brightness / Value Slider (0.0 - 1.0)
                Text(
                    text = "Parlaklık: %${(hsv[2] * 100).toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurface
                )
                Slider(
                    value = hsv[2],
                    onValueChange = { newVal ->
                        val newColor = AndroidColor.HSVToColor(floatArrayOf(hsv[0], hsv[1], newVal))
                        onColorChanged(newColor)
                    },
                    valueRange = 0f..1f
                )
            }
        }
    }
}
