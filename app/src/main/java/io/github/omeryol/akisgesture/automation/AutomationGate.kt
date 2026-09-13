package io.github.omeryol.akisgesture.automation

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import io.github.omeryol.akisgesture.gesture.GestureConfig
import io.github.omeryol.akisgesture.receiver.GestureCommandReceiver
import io.github.omeryol.akisgesture.service.MacroDroidPluginActivity
import io.github.omeryol.akisgesture.service.MacroDroidPluginReceiver
import io.github.omeryol.akisgesture.service.StartGestureActivity
import io.github.omeryol.akisgesture.service.StopGestureActivity
import io.github.omeryol.akisgesture.service.ToggleGestureActivity
import io.github.omeryol.akisgesture.settingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Dış otomasyon (MacroDroid, Tasker, Locale uyumlu araçlar) için tek yetkili
 * güvenlik kapısıdır. İki katman birlikte çalışır:
 *
 * 1. **Bileşen durumu:** Otomasyon anahtarı kapalıyken dışa açık tüm bileşenler
 *    `PackageManager` üzerinden devre dışı bırakılır. Otomasyon uygulaması
 *    eklentiyi listesinde görmez, broadcast ve activity intentleri çözülemez.
 * 2. **Çalışma anı kontrolü:** Her giriş noktası komutu işlemeden önce anahtarı
 *    doğrudan DataStore'dan okur. Böylece soğuk süreç başlangıcında `StateFlow`
 *    varsayılan değerinin (kapalı) yanlış karar üretmesi engellenir.
 *
 * Kapı **fail-closed** çalışır: okunamayan veya eksik değer "kapalı" sayılır.
 */
object AutomationGate {

    /**
     * Anahtar kapalıyken devre dışı bırakılan dışa açık bileşenler.
     * Manifest ile tutarlılığı birim testiyle doğrulanır.
     */
    val exportedComponentClasses: List<Class<*>> = listOf(
        MacroDroidPluginActivity::class.java,
        MacroDroidPluginReceiver::class.java,
        GestureCommandReceiver::class.java,
        StartGestureActivity::class.java,
        StopGestureActivity::class.java,
        ToggleGestureActivity::class.java,
    )

    private fun exportedComponents(context: Context): List<ComponentName> =
        exportedComponentClasses.map { ComponentName(context, it) }

    /**
     * Otomasyon anahtarının gerçek değeri. DataStore'dan okunur; eksik veya
     * bozuk kayıt "kapalı" kabul edilir.
     */
    suspend fun isEnabled(context: Context): Boolean =
        context.settingsDataStore.data.first()[GestureConfig.KEY_AUTOMATION_APPS_ENABLED] ?: false

    /**
     * Anahtar değerini engelleyerek okur.
     * Ana iş parçacığında çağrılmamalıdır; receiver ve activity komutları
     * arka plan iş parçacığında işlenir.
     */
    fun isEnabledBlocking(context: Context): Boolean = runBlocking { isEnabled(context) }

    /** Anahtar durumuna göre dış bileşenleri açar veya kapatır. */
    fun applyComponentState(context: Context, enabled: Boolean) {
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        val packageManager = context.packageManager
        for (component in exportedComponents(context)) {
            runCatching {
                packageManager.setComponentEnabledSetting(
                    component,
                    state,
                    PackageManager.DONT_KILL_APP,
                )
            }
        }
    }
}
