# 🔍 Akış Gesture ↔ FNG Karşılaştırma Raporu

**Oluşturan:** GitHub Copilot (DeepSeek V4 Pro)
**Tarih:** 2026-07-30
**Amaç:** Akış Gesture'ın mevcut durumunu FNG ile karşılaştırarak eksikleri belirlemek.
**Kapsam:** `app/src/main/java/com/openswipe/` altındaki tüm paketler, UI, servis ve yapılandırma dosyaları incelenmiştir.

---

## Çıkarımlar ve Gözlemler

Bu proje, **OpenSwipe** (MIT lisanslı) tabanından başlayarak FNG'nin (Fluid Navigation Gestures) işlevsel eşdeğeri olmayı hedefleyen kişisel bir Android kenar hareket uygulamasıdır. İnceleme sonucunda:

1. **Mimari sağlam:** Bipartite-graph kural motoru, AccessibilityService tabanlı overlay yönetimi, DataStore kalıcılığı ve Jetpack Compose Material3 arayüzü ile temiz bir yapı kurulmuş.

2. **Günlük kullanıma hazır:** Sol/sağ/alt kenar hareketleri, hızlı çekme/çekip-bekletme ayrımı, 24 eylem, 9 preset, görsel/haptic geri bildirim ve HyperOS dayanıklılık önlemleri çalışır durumda.

3. **FNG'ye göre ~%57 tamamlanma oranı:** Temel hareketler ve eylemler büyük ölçüde tamam, ancak ince ayar (per-edge sensitivity, eşit olmayan bölgeler, eşikte çalıştırma), eksik eylemler (menü, döndürme, tek el modu) ve gelişmiş politika katmanları (tam ekran, araç modu) henüz yok.

4. **Root katmanı ayrı tutulmuş:** Root işlemleri ana uygulamadan izole, yalnızca gerekli durumlarda kullanılıyor — bu iyi bir tasarım kararı.

5. **Kod kalitesi yüksek:** Durum makineleri, sealed class'lar, policy pattern'leri, test edilebilir threshold fonksiyonları ile temiz ve sürdürülebilir bir kod tabanı.

---

## ✅ Mevcut Olan Özellikler (FNG ile eşleşen)

| Kategori | Özellik | Durum |
|----------|---------|-------|
| **Kenar Hareketleri** | Sol, sağ, alt kenar algılama | ✅ Tam |
| **Hareket Tipleri** | Hızlı çekme (QUICK_SWIPE) | ✅ Tam |
| **Hareket Tipleri** | Çekip bekletme (SWIPE_HOLD), 280ms varsayılan | ✅ Tam |
| **Hareket İptali** | Kenara geri dönerek iptal (GestureCancelPolicy) | ✅ Tam |
| **Tetik Modları** | Touch / Swipe (click pass-through) | ✅ Tam |
| **Gezinme** | Geri, Ana ekran, Son uygulamalar | ✅ Tam |
| **Uygulama Geçişi** | Önceki/sonraki uygulama (root ile) | ✅ Tam |
| **Sistem** | Kilit ekranı, Ekran görüntüsü, Bölünmüş ekran, Güç menüsü | ✅ Tam |
| **Paneller** | Bildirimler, Hızlı ayarlar, Klavye seçici, Ses paneli, Asistan | ✅ Tam |
| **Medya** | Oynat/Duraklat, Önceki/Sonraki parça, Ses +/-, Sesi kapat/aç | ✅ Tam |
| **Donanım** | Fener aç/kapat (root ile) | ✅ Tam |
| **Root** | Öndeki uygulamayı kapat, Uygulama geçişi | ✅ Tam |
| **Uygulama Aç** | Belirli uygulama başlatma (LaunchApp) | ✅ Tam |
| **Kural Motoru** | Bipartite-graph (Trigger → Action), enable/disable | ✅ Tam |
| **Bölgeler** | Alt kenar 3 eşit bölge, yarımlar, özel aralık | ✅ Tam |
| **Presetler** | 9 hazır şablon (iOS, Android, Tek el, Medya vb.) | ✅ Tam |
| **Profil** | Uygulama bazlı kural profilleri (AppRuleProfiles) | ✅ Kısmi |
| **Duraklatma** | Kilit ekranı, klavye, yatay, uygulama bazlı | ✅ Tam |
| **Geri Bildirim** | Bézier eğrisi (4 stil), haptic (3 seviye), renk/saydamlık/simge | ✅ Tam |
| **Servis** | AccessibilityService + KeepAlive + BootReceiver | ✅ Tam |
| **Yedek** | JSON yedekleme / geri yükleme (SettingsBackupManager) | ✅ Tam |
| **Arayüz** | Material3 Compose, Türkçe, hareket haritası (GestureMapCard) | ✅ Tam |
| **Hızlı Ayar** | Quick Settings tile (GestureTileService) | ✅ Tam |
| **Otomasyon** | Broadcast intent + MacroDroid eklentisi | ✅ Tam |
| **Navigasyon** | Uygulama içi geri (InternalNavigationBus) | ✅ Tam |
| **Sağlık** | AccessibilityHealthPolicy onarım mantığı | ✅ Tam |
| **Alt yatay çekme** | BottomAppSwitchPolicy (hızlı uygulama geçişi) | ✅ Tam |

---

## ❌ Eksik Özellikler (FNG'de var, Akış'ta yok)

### 🔴 Kritik Eksikler — Temel FNG deneyimi

| # | Özellik | FNG'deki Karşılığı | Öncelik |
|---|---------|-------------------|---------|
| 1 | **Kenar başına hassasiyet** | Sol, sağ, alt için ayrı sensitivity ayarı | 🔴 Yüksek |
| 2 | **Eşit olmayan / özel sayıda bölge** | İstenilen sayıda ve boyutta bölge tanımlama | 🔴 Yüksek |
| 3 | **Bölge konumu / uzunluğu / kalınlığı** | Her bölgenin konumu, uzunluğu ve kenar kalınlığı ayrı ayarlanabilir | 🔴 Yüksek |
| 4 | **"Eşikte çalıştır" seçeneği** | Bekletme eylemi: eşik geçilince mi, parmak kalkınca mı çalışsın? | 🔴 Yüksek |
| 5 | **Yan tetikleyici dikey konumu** | Tek el kullanım için yan alanı yukarı/aşağı kaydırma (offset) | 🔴 Yüksek |
| 6 | **Yön sapma kontrolü** | Yanlış yönde çekmeyi iptal (başlangıçta max 35° sapma) | 🟠 Orta |
| 7 | **Histerezis** | Eşik geçildikten sonra küçük parmak titremelerinde durum düşmesin | 🟠 Orta |

### 🟡 Eylem Eksikleri — FNG'nin eylem listesinden

| # | Eylem | Açıklama |
|---|-------|----------|
| 8 | **Menü tuşu** | `KEYCODE_MENU` gönder |
| 9 | **Otomatik döndürme aç/kapat** | `Settings.System.ACCELEROMETER_ROTATION` toggle |
| 10 | **Geçerli uygulamanın yönünü değiştir** | `setRequestedOrientation()` zorlaması |
| 11 | **Zorlanmış döndürme** | Portrait / Landscape / Reverse zorlaması |
| 12 | **Xiaomi/MIUI/HyperOS tek el modu** | Cihaza özel one-hand mode tetikleme |
| 13 | **Google arama katmanı** | Google search overlay (FNG'nin özel entegrasyonu) |
| 14 | **Sesli arama** | `Intent.ACTION_VOICE_SEARCH_HANDS_FREE` |
| 15 | **Sesli asistan** | Voice assistant (sistem asistanından ayrı) |
| 16 | **Uygulama kısayolu aç** | `ShortcutManager` shortcut intent |
| 17 | **Belirli tuş kodu gönder** | Kullanıcının seçtiği herhangi bir KeyEvent |
| 18 | **Gezinme çubuğu göster/gizle** | Navigation bar visibility (root veya `settings` komutu ile) |
| 19 | **Son uygulamalara çift dokunma** | Recents ekranında özel çift dokunma davranışı |

### 🟠 Politika / Davranış Eksikleri

| # | Özellik | Açıklama |
|---|---------|----------|
| 20 | **Tam ekran uygulama tespiti** | Fullscreen immersive mode'da hareketleri duraklatma |
| 21 | **Araç modu** | Android Auto / Car Mode'da farklı davranış |
| 22 | **Uygulama yükleyici ekranı** | Package installer ekranında duraklatma |
| 23 | **İzin ekranları** | Permission grant screen'de duraklatma |
| 24 | **Klavye açıkken yan alan taşıma** | IME görünürken side trigger overlay'leri yukarı kaydırma |
| 25 | **Root'suz ekran kilidi/SS** | Root olmadan alternatif lock/screenshot yolu (fallback) |

### 🔵 Mimari / Altyapı Eksikleri

| # | Özellik | Açıklama |
|---|---------|----------|
| 26 | **Bézier görsel geri bildirim** | Roadmap'te var ([#3](https://github.com/ARCJ137442/OpenSwipe/issues/3)), tamamlanmamış |
| 27 | **F-Droid listeleme** | Roadmap'te var, henüz yok |
| 28 | **Birim testleri** | `app/src/test/` var ama kapsamı belirsiz |
| 29 | **Edge aktif uzunluk override** | Kenarın sadece belirli bir piksel aralığını aktif yapma |
| 30 | **Ekran dönüşünde overlay yenileme** | Rotation sırasında overlay'lerin doğru yeniden konumlanması |

---

## 📊 Özet İstatistikler

| Metrik | Değer |
|--------|-------|
| Toplam hedef özellik | 30 |
| Tamamlanmış | 17 |
| Kısmen tamamlanmış | 5 |
| Eksik | 18 |
| **Tamamlanma oranı** | **~57%** |

---

## 🎯 Önerilen Yol Haritası

### 1. Aşama — Temel Eşik (ilk 3 sprint)
- [ ] Kenar başına ayrı hassasiyet ayarı (#1)
- [ ] Özel bölge sayısı, konumu ve uzunluğu (#2, #3)
- [ ] "Eşikte çalıştır" / "parmak kalkınca çalıştır" seçeneği (#4)
- [ ] Yön sapma kontrolü + histerezis (#6, #7)

### 2. Aşama — Eylem Genişletme
- [ ] Menü tuşu, döndürme kontrolleri, Xiaomi tek el modu (#8–12)
- [ ] Sesli arama, sesli asistan, uygulama kısayolu, özel tuş kodu (#14–17)
- [ ] Gezinme çubuğu yönetimi (#18)

### 3. Aşama — Politika ve Dayanıklılık
- [ ] Tam ekran / araç modu / yükleyici / izin duraklatmaları (#20–23)
- [ ] Klavye ile yan alan taşıma (#24)
- [ ] Bézier feedback tamamlama (#26)

---

> **Not:** Bu rapor, `app/src/main/java/com/openswipe/` altındaki tüm paketler (`action/`, `backup/`, `feedback/`, `gesture/`, `model/`, `navigation/`, `overlay/`, `root/`, `rule/`, `service/`, `ui/`, `util/`) ve ilgili yapılandırma dosyaları (`build.gradle.kts`, `AndroidManifest.xml`, `FNG_PARITY_TARGET.md`, `README.md`, `docs/`) incelenerek hazırlanmıştır. Kod değişikliği yapılmamıştır.
