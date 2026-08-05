# 🎯 Bu Noktadan Sonra — Proje Hedefleri

**Yazan:** GitHub Copilot (DeepSeek V4 Pro)
**Tarih:** 2026-07-30
**Bağlam:** Bu dosya, projenin `2026-07-30` tarihli tam yedeği alındıktan sonra,
mevcut durumdan ileriye dönük olarak neyin amaçlandığını belgelemek için yazılmıştır.
Yedek dosyasına (`AkisGesture_backup_2026-07-30/`) dokunulmamıştır.

---

## Mevcut Durumda Ne Var?

Akış Gesture şu anda **günlük kullanıma hazır**, %57 FNG eşdeğerliğine sahip bir
Android kenar hareket uygulamasıdır:

- Sol, sağ ve alt kenarda hızlı çekme + çekip bekletme çalışıyor
- 24 farklı eylem (geri, ana ekran, son uygulamalar, medya, asistan vb.)
- 9 hazır kullanım şablonu (iOS tarzı, Android klasik, tek el, medya kontrolü vb.)
- Görsel Bézier eğrisi + 3 seviye haptic geri bildirim
- Uygulama bazlı duraklatma ve kural profilleri
- HyperOS/Android 15'te dayanıklılık (KeepAlive, BootReceiver, 1x1px overlay)
- Root komutları (uygulama zorla kapatma, hızlı uygulama geçişi, fener)
- JSON yedekleme, MacroDroid eklentisi, Quick Settings kutucuğu

## Neler Eksik?

`FNG_KARSILASTIRMA_RAPORU.md` dosyasında ayrıntılı olarak listelenen 18 eksik:

### Öncelikli (teknik borç değil, doğrudan kullanıcı deneyimi)
1. **Kenar başına ayrı hassasiyet** — Şu an tüm kenarlar aynı `dampingFactor` ile çalışıyor
2. **Eşit olmayan / özel sayıda bölge** — Sadece 3 eşit bölge var, FNG istenen sayıda bölge sunuyor
3. **"Eşikte çalıştır" modu** — Bekletme eylemi şu an sadece parmak kalkınca tetikleniyor
4. **Yan kenar dikey ofset** — Tek el kullanımı kolaylaştıran konumlandırma

### Orta vadeli (eylem seti)
5. Menü tuşu, döndürme kontrolleri, Xiaomi tek el modu
6. Sesli arama, uygulama kısayolu, özel tuş kodu gönderme
7. Gezinme çubuğu göster/gizle (root ile)

### Uzun vadeli (politika katmanı)
8. Tam ekran, araç modu, uygulama yükleyici, izin ekranlarında duraklatma
9. Klavye açıkken yan alanları taşıma
10. Bézier görsel geri bildirim tamamlama

## Bundan Sonra Ne Yapılacak?

Bu proje, **FNG'nin kullanıcıya sunduğu tüm işlevleri**, FNG'nin kodunu kopyalamadan,
özgün ve test edilebilir bir mimariyle karşılamayı hedeflemektedir.

### Yakın vadeli adımlar:
1. **GestureConfig**'e per-edge `sensitivity` (dampingFactor) ve `verticalOffset` alanları ekle
2. **SectionRange** modelini genişleterek kullanıcı tanımlı bölge sayısı desteği getir
3. **GestureEngine**'e "fire on threshold" modu ekle (şu an sadece "fire on release")
4. **EdgeGestureDetector**'a yön sapma kontrolü ve histerezis ekle
5. Eksik eylemleri **ActionNode** sealed class'ına ve **ActionDispatcher**'a ekle

### Mimari kısıtlar (değişmeyecek):
- AccessibilityService tabanlı, root gerektirmez (root opsiyonel katman)
- Sistem uygulamasına dönüştürülmez
- Sürekli polling veya agresif keep-alive yapılmaz
- Her değişiklik CHANGELOG ve test ile gelir

### Yedek referansı:
`c:\Users\Omer\Desktop\AkisGesture_backup_2026-07-30\`
— Bu yedek, raporun oluşturulduğu andaki temiz durumu saklar.
Geri dönüş gerekirse bu yedekten geri yüklenebilir.

---

## ✅ 2026-07-30 Oturumu — Tamamlananlar

Bu oturumda aşağıdaki değişiklikler **build başarılı** şekilde tamamlandı:

### 1. Kenar Başına Hassasiyet (`GestureConfig`)
- `leftDamping`, `rightDamping`, `bottomDamping` — her kenar için ayrı damping
- `leftSwipeThresholdDp`, `rightSwipeThresholdDp`, `bottomSwipeThresholdDp` — ayrı eşik
- `leftVerticalStart/End`, `rightVerticalStart/End` — yan kenar dikey konum aralığı
- `directionToleranceDegrees` (varsayılan 35°) — yön sapma toleransı
- `hysteresisRatio` (varsayılan 0.25) — eşik düşme histerezisi
- `HoldFireMode` enum: `ON_RELEASE` / `ON_THRESHOLD`
- Tümü DataStore'a kalıcı kaydediliyor

### 2. 10 Yeni Eylem
| Eylem | ID | Gereksinim |
|-------|-----|-----------|
| Menü | `menu` | — |
| Otomatik döndürme | `toggle_auto_rotate` | — |
| Dikey yön | `force_portrait` | — |
| Yatay yön | `force_landscape` | — |
| Tek el modu | `xiaomi_one_hand` | Xiaomi cihaz |
| Sesli arama | `voice_search` | — |
| Sesli asistan | `voice_assistant` | API 21+ |
| Uygulama kısayolu | `app_shortcut:*` | API 25+ |
| Özel tuş kodu | `keycode:*` | — |
| Gezinme çubuğu | `toggle_nav_bar` | Root |

### 3. EdgeGestureDetector İyileştirmeleri
- Yön sapma kontrolü (atan2 + açı karşılaştırması)
- `holdFireMode` desteği — `ON_THRESHOLD` modunda eşik geçilince anında tetikleme
- Histerezis oranı yapılandırılabilir (önceden sabit 0.72 idi)

### 4. GestureEngine — Per-Edge Kullanımı
- `addEdgeOverlay` dikey ofset uyguluyor (yan kenarlar için)
- `createDetector` her kenar için ayrı damping ve eşik değeri kullanıyor

### 5. UI Güncellemeleri
- Ayarlar > Hareket: Her kenar için hassasiyet + eşik slider'ları
- Bekletme modu seçici (açılır menü)
- Eylem kategorileri güncellendi (Döndürme, Sistem Arayüzü eklendi)

### Dosya Değişiklikleri
| Dosya | Değişiklik |
|-------|-----------|
| `GestureConfig.kt` | Per-edge alanlar + HoldFireMode + helper metodlar |
| `OpenSwipeApp.kt` | Yeni DataStore anahtarları + update metodları |
| `ActionNode.kt` | 10 yeni eylem (Menu, ToggleAutoRotate, ForcePortrait, ForceLandscape, XiaomiOneHandMode, VoiceSearch, VoiceAssistant, AppShortcut, SendKeyCode, ToggleNavBar) |
| `ActionDispatcher.kt` | Yeni eylem implementasyonları |
| `RootCommandExecutor.kt` | `toggleNavBar()` eklendi |
| `ActionCategories.kt` | Yeni kategoriler (Döndürme, Sistem Arayüzü) |
| `EdgeGestureDetector.kt` | Yön sapması + holdFireMode + histerezis |
| `GestureEngine.kt` | Per-edge config + dikey ofset |
| `GestureCancelPolicy.kt` | Yapılandırılabilir histerezis |
| `HomeViewModel.kt` | Yeni config metodları |
| `SettingsScreen.kt` | Per-edge ayar UI + holdFireMode seçici |
| `CHANGELOG.md` | Tüm değişiklikler kaydedildi |

### Kalanlar (sonraki oturum)
- [ ] F-Droid listeleme
- [ ] Araç modu duraklatma
- [ ] Klavye ile yan alan taşıma
- [ ] Eşit olmayan / özel sayıda bölge desteği (UI)
- [ ] Bézier görsel geri bildirim (OpenSwipe #3)

---

## ✅ 2026-07-30 Oturumu #3 — Tamamlananlar

### 1. Titreşim Şiddeti + Ses Ayarı
- `hapticIntensity` (0f–1f, varsayılan 1f) — titreşim gücü
- `hapticSoundEnabled` — hareket tetiklendiğinde kısa tıklama sesi
- `HapticHelper` yeniden yazıldı: `intensity` ve `soundEnabled` global state
- `ToneGenerator` ile debounce'lu tıklama sesi
- Ayarlar > Görünüm altında slider ve switch

### 2. Eylem Simgeleri — FeedbackIcon Kaldırıldı
- `ActionSymbols` — her eylem için Unicode simge (← ⌂ ⊞ 🔊 🔦 vb.)
- `BezierStretchRenderer.drawGestureIcon`: önce `actionSymbol` kontrol edilir,
  boşsa eski `FeedbackIcon`'a düşer
- `GestureEngine.handleGestureProgress`: hareket sırasında eşleşen eylemi
  `activeRuleSet.match()` ile bulup `ActionSymbols.symbolFor()` ile simgeyi
  FeedbackView'e aktarır
- Ayarlar'daki "Hızlı çekme simgesi" / "Bekletme simgesi" seçicileri kaldırıldı
- `ActionVisuals.kt`: yeni eylemler için Material simgeler eklendi

### 3. Animasyon Esnekliği
- `animationSpeed` (0.5x–2.0x) — animasyon hız çarpanı
- `animationSize` (0.5x–2.0x) — baloncuk/kavis/simge boyut çarpanı
- `FeedbackView` ve `BezierStretchRenderer` bu değerleri uyguluyor
- Ayarlar > Görünüm altında slider'lar

### Dosya Değişiklikleri
| Dosya | Değişiklik |
|-------|-----------|
| `GestureConfig.kt` | hapticIntensity, hapticSoundEnabled, animationSpeed/Size, KEY_HAPTIC_ENABLED |
| `OpenSwipeApp.kt` | Yeni DataStore anahtarları + update metodları |
| `HapticHelper.kt` | intensity/soundEnabled global state + ToneGenerator |
| `FeedbackView.kt` | actionSymbol, animationSpeed, animationSize |
| `BezierStretchRenderer.kt` | actionSymbol, animSpeed/Size, drawGestureIcon güncellemesi |
| `ActionSymbols.kt` | **Yeni dosya** — eylem→Unicode simge eşlemesi |
| `GestureEngine.kt` | actionSymbol eşleme + haptic intensity/sound aktarımı |
| `ActionVisuals.kt` | Yeni eylemler için Material simgeler |
| `HomeViewModel.kt` | setHapticIntensity/Sound/Enabled, setAnimationSpeed/Size |
| `SettingsScreen.kt` | Görünüm yeniden düzenlendi, simge seçiciler kaldırıldı |
| `CHANGELOG.md` | Güncellendi |

---

## ✅ 2026-07-30 Oturumu #2 — Tamamlananlar

### 1. Per-Edge Tetik Kalınlığı
- `leftTriggerWidthDp`, `rightTriggerWidthDp`, `bottomTriggerHeightDp` — her kenar ayrı
- `GestureEngine.addEdgeOverlay` artık `triggerSizeDpFor(edge)` kullanıyor
- Ayarlar > Hareket: Her kenar için kalınlık slider'ı

### 2. Tam Ekran + İzin Ekranı Duraklatma
- `pauseOnFullScreen` (varsayılan: açık) — video/oyun tam ekranda kapat
- `pauseOnPermissionScreen` (varsayılan: açık) — APK yükleme/izin/güvenlik ekranlarında kapat
- `SystemPausePolicy` genişletildi
- MIUI/HyperOS paketleri de tanınıyor (`com.miui.packageinstaller`, `com.miui.securitycenter`)

### 3. GestureMapCard Temizliği
- Canlı prova artık `config.dampingFor(edge)` ve `config.swipeThresholdDpFor(edge)` kullanıyor
- `LocalDensity` ile dp→px dönüşümü

### Dosya Değişiklikleri
| Dosya | Değişiklik |
|-------|-----------|
| `GestureConfig.kt` | Per-edge trigger width + pauseOnFullScreen/PermissionScreen |
| `OpenSwipeApp.kt` | Yeni DataStore anahtarları + update metodları |
| `GestureEngine.kt` | Per-edge triggerSizeDpFor + fullScreen/permissionScreen params |
| `SystemPausePolicy.kt` | fullScreen + permissionScreen parametreleri |
| `GestureAccessibilityService.kt` | İzin ekranı tespiti (PERMISSION_PACKAGES) |
| `HomeViewModel.kt` | setEdgeTriggerSize + setPauseOnFullScreen/PermissionScreen |
| `SettingsScreen.kt` | Kalınlık slider'ları + Tam ekran/İzin ekranı switch'leri |
| `GestureMapCard.kt` | Per-edge config + deprecation temizliği |
| `CHANGELOG.md` | Güncellendi |

