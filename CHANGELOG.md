# Değişiklik Günlüğü

Bu projedeki önemli değişiklikler bu dosyada tutulur.

## [1.1.10] - 2026-08-01

### 🔴 3D Yuvarlak Cam Rozet & Eşleşen 3D Dairesel Yükselti Gölgesi (3D Glass Orb)

- **3D Yuvarlak Cam Rozet (`3D Glass Orb Badge`):** İkon konteyneri, pürüzsüz 3 boyutlu yuvarlak küre rozet formuna getirildi.
- **Uyumlu 3D Dairesel Yükselti Gölgesi:** Rozet ile tam uyumlu 3D yuvarlak taban gölgesi (`canvas.drawCircle shadow`) ve üst-sol **3D ışık yansıması merceği (`specular refraction lens`)** entegre edildi.

## [1.1.9] - 2026-08-01

### 🟧 3D Adaptif Cam Squircle İkon Rozeti (Squircle Badge Container)

- **Sert Daire Rozet Kaldırıldı:** Kare/dikdörtgen Emoji simgeleriyle çakışan rijit daire konteyneri silindi.
- **3D Cam Squircle (`3D Adaptive Squircle`):** İkon ve Emoji geometrisine kusursuz uyum sağlayan yumuşak yumurtlatılmış kavisli **3D Cam Squircle (`drawRoundRect` ile 14-16dp kavis)** ve gölgelendirme konteyneri entegre edildi.

## [1.1.8] - 2026-08-01

### 💎 Belirgin 3D Derinlik Efektleri & Canlı Emoji Simgeleri

- **Belirgin 3D Derinlik & Çift Katmanlı Z-Gölgesi:** 3D Z-yükselti gölgesi (`drop shadow opacity & offset`) daha belirginleştirildi. Şekiller ekrandan bağımsız ve kabartmalı görünür.
- **Yüksek Kontrastlı 3D Optik & Küresel Işık Merceği:** Kopan cıva damlası, okyanus dalgası ve plazma kürelerine belirgin 3D ambient kararma (`ambient occlusion`) ve üst-sol parlak **3D mercek nodu (`specular highlight lens`)** kazandırıldı.
- **Canlı Emoji Simgeleri Geri Yüklendi:** Tüm jest eylem simgeleri ve geri bildirim ikonları ifadeli ve canlı Emoji formatına (`🏠 📱 🔒 📸 ⚡ 🔔 ⚙️ 🔊 ☀️ 🌙 🔦 🚀`) dönüştürüldü.

## [1.1.7] - 2026-08-01

### 🔮 3D Hacimsel Uzamsal Optik & Z-Eksen Derinliği (3D Spatial Engine)

- **3D Hacimsel Derinlik & Gölge (3D Z-Elevation Drop Shadow):** Tüm doğa animasyonlarına arka plandaki duvar kağıdı/uygulama üzerine düşen **3D Z-yükselti gölgesi (`drop shadow`)** eklendi.
- **3D Küresel Optik & Ambient Occlusion (`MERCURY_TEARDROP 3D`):** Kopan cıva damlasına alt 3D ortam kararması (`ambient occlusion`), 3D küresel derinlik gradyanı ve üst-sol **3D ışık kırılma merceği (`3D specular optics node`)** kazandırıldı.
- **3D Gelgit Yükseltisi (`OCEAN_WAVE 3D`):** Okyanus dalgasına 3D hacimsel derinlik katmanı ve öne doğru Protrude olan 3D kavis hissi eklendi.
- **3D Parçacık Derinlik Paralaksı (`PLASMA_FIRE 3D` & `SOLAR_CORONA 3D`):** Volkanik kor parçacıklarına ve güneş rüzgarlarına 3D Z-paralaks derinliği kazandırıldı (kameraya yakın parçacıklar daha büyük ve parlak, uzaktakiler derinlikte süzülüyor).

## [1.1.6] - 2026-08-01

### 🌊 Masterclass Doğa Simülasyonları & Kopan Sıvı Damlası Fiziği

- **Kopya Animasyonlar Temizlendi:** Tüm çakışan ve kopya isimler kaldırıldı; geriye 6 benzersiz, yüksek derinlikli doğa simülasyonu bırakıldı:
  - **💧 Kopan Sıvı Damlası (`MERCURY_TEARDROP`):** Parmağı çektikçe okyanustan uzayan, boynu daralıp **yüzey gerilimiyle koparak serbestçe parmağın önünde yüzen dairesel cıva damlası** fiziği.
  - **🌊 Okyanus Dalgası (`OCEAN_WAVE`):** Çift katmanlı kırılmalı gelgit dalgası ve köpüksü dalga tepeleri.
  - **⚡ Elektrik Fırtınası & Şimşek (`ELECTRIC_STORM`):** Yüksek voltajlı flaş aurası, 8 fraktal çatallanma arkı ve eklem iyonlaşma kıvılcımları.
  - **☀️ Güneş Koronası (`SOLAR_CORONA`):** 16 dönen güneş ışını, 4 arklı manyetik güneş ilmiği (`solar prominence loops`) ve mercek parlaması.
  - **💨 Atmosferik Sis & Buhar (`ATMOSPHERIC_MIST`):** Beğenilen 8 bulutsu sis kümesi ve orbital solunum aurası.
  - **🔥 Plazma Ateş (`PLASMA_FIRE`):** Titreşen alev dili ve yükselen volkanik kor parçacıkları.

## [1.1.5] - 2026-08-01

### 🌋 Derin Doğa Simülasyonu Mimarisi & Parçacık Fiziği (Deep Organic Engine Rewrite)

- **Eski 2D Çizim Mantığı Tamamen Silindi:** Basit ve sığ görsel kalıplar temizlendi. Yerine prosedürel parçacık ve sıvı fiziği simülasyonları yazıldı:
  - **🌊 Okyanus Sıvı & Metaball Fiziği (`OCEAN_LIQUID`):** 12 dinamik sıvı verteksinden oluşan sinüs dalgası ve yüzeyden ayrılıp yalpalayan 5 dinamik metaball damlası.
  - **🔥 Plazma Ateş & Volkanik Kıvılcım Fiziği (`PLASMA_FIRE`):** Titreşen alev dili path'i, 3 kademeli plazma ısı gradyanı ve yukarı doğru süzülen 8 kor parçacığı.
  - **⚡ Elektrik Fırtınası & Fraktal Şimşek Fiziği (`ELECTRIC_STORM`):** Her karede prosedürel olarak üretilen 6 fraktal çatallanmalı elektrik arkı ve plazma iyonlaşma aurası.
  - **💨 Atmosferik Sis & Buhar Fiziği (`ATMOSPHERIC_MIST`):** Yörüngesel sis küme parçacıkları içeren derin bulutsu yoğuşma aurası.
  - **☀️ Güneş Koronası & Yıldız Işını Fiziği (`SOLAR_CORONA`):** 16 dönen güneş ışını, güneş korona aurası ve beyaz plazma mercek odağı.

## [1.1.4] - 2026-08-01

### 🌿 Doğal Varlıklar Animasyon Felsefesi & Sonsuz Renk Barları

- **Doğal Varlıklar Animasyon Motoru (Natural Elements Engine):** Animasyon felsefesi doğanın 8 temel unsurundan ilham alınarak sıfırdan yazıldı:
  - **🌊 Su (Water):** Akıcı nehir Bezier dalgası, köpük vurgusu ve su damlası serpintisi.
  - **🔥 Ateş (Fire):** Titreşen alev dili path'i, kehribar-kırmızı radyal ısı gradyanı ve yükselen kıvılcım parçacıkları.
  - **💨 Buhar (Steam):** Sıcak sis kümeleri ve yumuşak saydam yoğuşma daireleri.
  - **☁️ Bulut (Cloud):** Yumuşak kümülüs bulut katmanları ve hava hissi.
  - **⚡ Şimşek (Lightning):** Yüksek voltajlı çatallanan elektrik arkları ve flaş parlaması.
  - **🍃 Rüzgar (Wind):** Aerodinamik hava akımı çizgileri ve rüzgar kıvrımları.
  - **🌧️ Yağmur (Rain):** Suya düşen damla gibi genişleyen halka dalgaları ve micro-splash serpintileri.
  - **☀️ Güneş (Sun):** Dönen 8 güneş ışını, altın korona aurası ve yoğun ışık merceği.
- **Sonsuz HSV Renk Seçim Barı (`AkisInfiniteColorPicker`):** Sabit palet butonları yerine 16.7 milyon renk olanağı sunan 0°-360° Hue barı, Doygunluk ve Parlaklık slider'ları ve canlı HEX gösterimi sunuldu.

## [1.1.3] - 2026-08-01

### 🎨 Çift Renkli Renk Uzayı, Sabitlenen L-Swipe Geri Bildirimi & Görsel Yenilenme

- **Çift Renkli (Dual-Color) Tasarım & Renk Uzayı Düzeltmesi:** Manuel renk paleti seçiminin uygulamaya duyarlı otomatik renk tarafından ezilmesi sorunu giderildi. Hızlı Çekme için **Birincil Hareket Rengi** ve Çekip Bekletme için **İkincil Hareket Rengi** ayrıldı. Çekip bekletme eşiği aşıldığında renk yumuşak biçimde ikincil renge geçiş yapar.
- **Sabitlenen L-Swipe Görsel Geri Bildirimi (Anchored Lock-On):** L-Swipe hareketi başladığı anda kenardaki animasyon parmakla dikey kaymak yerine parmağın büküldüğü ilk dönüş noktasında (`bendStartY`) **kilitlenip sabit kalır** ve anında parlak **Neon Yeşil L-Swipe Vurgu Rengine** dönüşür.
- **1. Eylem (Geri) Parmak Bırakma Titreşimi:** `handleGestureResult` eylem yürütücüsüne haptik tetikleyici eklendi. Geri hareketi tamamlanıp parmak kaldırıldığı anda belirgin haptik darbe ile eylem onaylanır.
- **Görsel Geometri & Simge Etkileşimi Yenileme:** `BezierStretchRenderer` kütle merkezi hesabı (`0.52x`), akıcı Bezier konturları, yaylı simge büyümesi (spring-pop `1.18x` / `1.32x`) ve rozet halkası (badge ring) ile yenilendi.

## [1.1.2] - 2026-08-01

### 🏗️ Mimari Modülerleştirme, Haptik Motoru Sıfırdan Yazımı & Temizlik

- **`HapticHelper.kt` Sıfırdan Yazıldı:** Çapraz bağımlılıklar ve yama kodları tamamen temizlendi. Donanım titreşimi (OneShot + legacy darbe) ve ses motoru (`AudioManager.FX_KEY_CLICK` + `ToneGenerator`) iki bağımsız kanala ayrıldı. Titreşim süresi (%100'de 160ms'ye kadar) ve genliği slider'a tam duyarlı ölçekleniyor. Ayarlar ekranında slider sürüklendiğinde canlı haptik önizleme eklendi.
- **Eylem Yürütücüler Modülerleştirildi (`com.openswipe.action.handler`):** Monolitik `ActionDispatcher` 4 ayrı domain handler'a bölündü:
  - `NavigationActionHandler`: Geri, Ana Ekran, Son Uygulamalar, Önceki/Sonraki Uygulama.
  - `SystemActionHandler`: Ekran Kilidi, Ekran Görüntüsü, Güç Menüsü, Paneller, Parlaklık.
  - `MediaActionHandler`: Oynat/Duraklat, İleri/Geri parça, Ses seviyesi ve Sessiz.
  - `HardwareAndAppHandler`: Fener, Uygulama/Kısayol Başlatıcı, Ekran Yönü zorlama, Özel tuş kodları.
- **Hareket Algılama Modülleri (`com.openswipe.gesture.detector`):**
  - `LSwipeDetector.kt`: 2-fazlı L-swipe vektör fiziği ve içe çekilme durum takibi bağımsız modüle dönüştürüldü.
  - `DirectionValidator.kt`: Sürükleme yönü ve açı toleransı doğrulaması modülleştirildi.
  - `EdgeGestureDetector.kt`: Tüm alt modülleri koordine eden temiz ve sürdürülebilir bir Durum Makinesi olarak sıfırdan refaktör edildi.
- **Canvas Simge Uyumsuzluğu Giderildi:** Canvas `drawText` ile çizilen emoji karakterleri (`🏠📸🔄`), tüm Android cihazlarda ve farklı sistem yazı tiplerinde sorunsuz render olan standart Unicode geometrik karakterlerle (`⌂⊡⇄`) yenilendi.

## [1.1.1] - 2026-08-01

### 🔧 Hareket Algılama Güvenilirlik Düzeltmesi

- **L-Swipe yanlış pozitif algılama giderildi:** `resolveGestureResult` içindeki
  `rawDy > 40f` tabanlı L-swipe sınıflandırması kaldırıldı. Normal geri hareketi
  sırasında parmağın doğal dikey sapması (40+ piksel) bu eşiği aşarak hareketi
  yanlışlıkla `SWIPE_UP_L` / `SWIPE_DOWN_L` olarak sınıflandırıyordu. Bu gesture
  tipi için kural tanımlı olmadığından eylem tetiklenmiyordu. L-swipe algılama
  artık yalnızca `handleUp` içindeki 2-fazlı dedektör tarafından yapılıyor;
  kasıtlı içeri→dikey hareket gerektirdiğinden yanlış pozitif üretmiyor.
- **Yön toleransı minimum alt sınırı:** `directionToleranceDegrees` değerine
  minimum 40° alt sınır eklendi. Önceki durumda 35.12° açı farkı 35.0° toleransla
  sadece 0.12° farkla reddediliyordu; artık tolerans 40°'nin altına düşemiyor.

## [1.1.0] - 2026-07-31

### 🚀 Geri Hareketi, L-Swipe İptal & Ana Sayfa Yönlendirme Düzeltmeleri

- **Geri Hareketi Takılma Düzeltmesi:** `InternalNavigationBus.requestBack()`, `MainActivity` aktif olmadığında veya dinleyici olmadığında `false` dönecek şekilde düzeltildi. Böylece dış uygulamalarda ve Akış içinde sistem `GLOBAL_ACTION_BACK` anında çalışır, geri hareketi takılması tamamen giderildi.
- **L-Swipe ve İptal Mantığı:** `EdgeGestureDetector` içerisindeki `state == CANCELLED` kontrolünün hareketi çalıştırması önlendi. Parmak kenara geri getirildiğinde (`< %40` eşik) `inwardArmed = false` yapılır ve hareket 0x0 tepkiyle iptal edilir.
- **Ana Sayfa Navigasyonu:** Durum kartı, Kenar Haritası kartı ve "Düzenle →" metni doğrudan Kurallar/İzinler ekranlarına yönlendirir.
- **Ana Sayfa Sekme Yönlendirmesi:** Ana sayfadaki "Sol & Sağ" kartı doğrudan `Sol Kenar` sekmesini, "Alt Kenar" kartı ise doğrudan `Alt Kenar` sekmesini açacak şekilde güncellendi.
- **Haptik Titreşim & Tıklama Sesi Düzeltmesi:** Titreşim çağrıları doğrudan donanım motoruna iletildi, sesler `STREAM_MUSIC` kanalına aktarıldı. Sistem dokunma sesleri kapalı olsa bile Akış içindeki sesler tam şiddetle çalışır.
- **L-Swipe 1.0x Vektör Oranı Kurallandırması:** L-Swipe'ın kabul edilmesi için dikey sürükleme mesafesinin yatay içeriye sürükleme mesafesinin en az **1.0 katı** (`turnDy >= turnDx * 1.0f`) olması zorunlu kılındı.
- **Ayar Kartları & Kavram Açıklamaları:** Ayarlar ekranında **Tetik**, **Hassasiyet (Sönümleme)**, **Eşik Mesafesi** ve **L-Swipe Bükülme Eşiği** için açıklayıcı yardım alt metinleri ve ayarlanabilir kaydırıcılar eklendi.
- **Sürüm:** Uygulama sürümü `1.1.0` (versionCode `2`) olarak güncellendi ve ana sayfaya eklendi.

- **L-Swipe Hassasiyet & Açı Kontrolü (`lSwipeThresholdDp`):**
  - İstem dışı L-Swipe tetiklenmelerini önlemek için dikey bükülme eşiği yükseltildi (`~85px / 30dp+`).
  - Çapraz kaydırmaları ayırt etmek için dikey vektör açı denetimi eklendi (`abs(turnDy) > turnDx * 1.1f`).
  - `hasLActionAt` kural kontrolü eklendi; ilgili bölgede L kuralı tanımlı değilse L-Swipe durum makinesi hiç devreye girmiyor.
- **Kademeli Geri Dönüş ve Esnek İptal (Multi-tier Hysteresis):**
  - **%65 Threshold Reversion:** Parmak bekletme (Hold / İkincil Eylem) modundayken %65 eşik seviyesinin altına çekildiğinde Hold kilidi çözülüyor ve otomatik olarak **Birincil Eyleme (Quick Swipe)** geri dönülüyor.
  - **%35 Threshold Cancel:** Parmak %35 eşiğin altına çekildiğinde tüm hareket güvenle iptal ediliyor.
- **Root Gerektirmeyen Anlık Uygulama Geçişi (`SwitchLastApp`):**
  - `ActionDispatcher.kt`, hafızadaki `previousForegroundPackage()` bilgisini kullanarak **Root gereksinimi olmadan ve sıfır gecikmeyle** önceki uygulamaya geçiyor.
  - Hata toast mesajları kaldırıldı; geçiş başarısız olursa sistem `GLOBAL_ACTION_RECENTS` görünümüne yumuşak yedekleme yapıyor.

### 🚀 L-Şeklinde Çekme (L-Swipe) — 2-Fazlı Mimari Yeniden Yazımı

> **Kök Neden Teşhisi:** L-hareketinin hiç tetiklenmemesinin iki temel nedeni tespit
> edildi ve kökten çözüldü.

- **Kök Neden 1 — Durum İptal Çakışması:** Parmak içeriye girdikten sonra L
  biçiminde yukarı/aşağı büküldüğünde yatay mesafe (`dx`) doğal olarak azalıyordu.
  `GestureCancelPolicy`, bu azalmayı "geri çekilme" sanarak durumu `CANCELLED`'e
  alıyordu. Parmak ekrandan kalktığında `state == DETECTED` kontrolü geçemediği
  için hiçbir eylem çalışmıyordu.
- **Kök Neden 2 — Bölge Hesabında Bitiş Koordinatı Hatası:** Parmağın kalktığı son
  `event.rawY` koordinatı kural eşleştirmesinde bölge oranı (`sectionRatio`) için
  kaynak olarak kullanılıyordu. L-Up hareketi sırasında parmak ekranın farklı bir
  bölgesine kayabildiğinden kullanıcının kuraldaki bölgeyle eşleşme sağlanamıyor ve
  `null` dönüyordu.

#### Uygulanan Çözümler

- **`maxInwardPx` Takibi:** Her `ACTION_MOVE` olayında içe ilerleme kaydediliyor.
  `maxInwardPx >= threshold` olduğu anda `inwardArmed = true` kilitlenir; bundan
  sonra yatay koordinat azalsa dahi `GestureCancelPolicy` devreye giremez.
- **`bendStartY` — Dönüş Noktası:** İçeri giriş eşiği aşıldığı andaki `event.rawY`
  değeri kaydedilerek dikey dönüş miktarı (`turnDy = event.rawY − bendStartY`)
  doğru geometri üzerinden hesaplanıyor.
- **2-Fazlı Sınıflandırma:** `inwardArmed && turnDy <= -35f → SWIPE_UP_L`,
  `turnDy >= 35f → SWIPE_DOWN_L`. L-tipi belirlendi mi `handleUp`'ta öncelikli
  olarak değerlendiriliyor; normal `QUICK_SWIPE` çözümlemesi atlanıyor.
- **`initialTouchCoord()` ile Başlangıç Bölge Hesabı:** Bölge oranı (`sectionRatio`)
  artık parmağın kenara ilk temas ettiği `downY` / `downX` koordinatından hesaplanıyor.
  Alt bölgeden başlayan L-Up hareketi ekranın en üstünde bitse bile Alt Bölge kuralı
  doğru bulunuyor.
- **`handleUp` Öncelik Sırası:** `lGestureType != null` ise eylem normal swipe
  değerlendirmesinden önce anında tetikleniyor.
- **`reset()` Temizliği:** `maxInwardPx`, `inwardArmed`, `bendStartY` ve `lGestureType`
  her dokunma başında sıfırlanıyor.

### 🧹 Kod Temizliği ve Deprecated Uyarı Giderimi

- **`service/GestureCommandReceiver.kt` silindi:** `receiver/GestureCommandReceiver`
  aktif ve Manifest'e kayıtlı olan sürümdür. `service/` paketindeki klon hiçbir
  bileşen tarafından kullanılmıyor ve Manifest'e kayıtlı değildi; ölü kod olarak
  kaldırıldı.
- **`EdgeGestureDetector.kt` — Deprecated Alan Geçişi (13 kullanım):**
  `config.dampingFactor` → `edgeDamping` (her kenar için `config.dampingFor(edge)`)
  ve `config.minSwipeThresholdPx` → `swipeThresholdPx` yerel property'leri eklendi.
  Tüm deprecated çağrılar bu iki alias üzerinden gerçekleştiriliyor.
- **`GestureEngine.kt` — Deprecated Alan Geçişi:**
  - `edgeTriggerWidthDp` karşılaştırması → `leftTriggerWidthDp != rightTriggerWidthDp`
  - `dampingFactor/minSwipeThresholdPx` diff kontrolü → `leftDamping/rightDamping/
    bottomDamping` ve `leftSwipeThresholdDp/rightSwipeThresholdDp/bottomSwipeThresholdDp`
    ile yeniden yazıldı.
  - `view.peakThreshold = currentConfig.minSwipeThresholdPx` satırı
    `@Suppress("DEPRECATION")` ile belgelenmiş istisna olarak işaretlendi
    (FeedbackView görsel bant genişliği için edge-agnostik global değer gerektirir).
- **`OpenSwipeApp.kt` — `KEY_EDGE_TRIGGER_WIDTH` kaldırıldı:**
  `updateEdgeTriggerWidth(dp)` artık deprecated `KEY_EDGE_TRIGGER_WIDTH` yerine
  `KEY_LEFT_TRIGGER_WIDTH` ve `KEY_RIGHT_TRIGGER_WIDTH` anahtarlarını doğrudan
  yazıyor.
- **`RuleLabels.kt` — Çift `when` Dalı Hatası:** `LockScreen`, `Screenshot` ve
  `SplitScreen` örnekleri `actionIcon` when-bloğunda iki kez yer alıyordu. Derleyici
  uyarısı veren yinelenen dallar temizlendi.
- **`ActionVisuals.kt` — AutoMirrored Icon Geçişi:**
  `Icons.Filled.ArrowBack` → `Icons.AutoMirrored.Filled.ArrowBack`
  `Icons.Filled.VolumeUp/Down/Off` → `Icons.AutoMirrored.Filled.*`
  Karşılıklı yansıtma (RTL) desteği ve Compose Material3 uyumluluğu sağlandı.
- **Sonuç:** `BUILD SUCCESSFUL` — sıfır `w:` derleyici uyarısı.


- **Siyah Renk Uzayı:** Renk uzayı palet seçicisine Siyah (`#000000`) seçeneği eklendi.
- **100% Dokunsal Titreşim (Haptics):** `HapticHelper` içerisine `VibrationAttributes.USAGE_TOUCH` / `USAGE_ASSISTANCE_SONIFICATION` ve `FLAG_IGNORE_GLOBAL_SETTING` eklendi; Android 10-15 pencerelerinden titreşim tam hassasiyetle tetiklendi.
- **8 Animasyon Modu Çizimi:** `BezierStretchRenderer` içerisine tüm 8 animasyon modu (`FLUID`, `NEON_PULSE`, `CYBER_HEX`, `ORB_GLOW`, `TEARDROP`, `BUBBLE`, `MINIMAL_PADDLE`, `ICON_ONLY`) tam matematiksel formüllerle aktarıldı.
- **Sönümlemeli Simge Ölçekleme ve 0.36x Takip:** Simgenin parmağın altına sıçramasını önleyen `0.36x` sönümlemeli takip fiziği ve pürüzsüz saydamlık (`alpha`) sönümlenmesi eklendi.

### 🌟 Yeni L-Şeklinde Çekme ve Kenar Sürükleyici Hazırlıkları
- Kenardan içeri çekip yukarı/aşağı kaydırma (L-Swipe) ve Kenar Parlaklık/Ses kaydırıcısı geliştirmeleri planlandı.

### Kenar Başına Tetik Kalınlığı
- Sol, sağ ve alt kenar için ayrı tetik genişliği ayarı
  (`leftTriggerWidthDp`, `rightTriggerWidthDp`, `bottomTriggerHeightDp`)
- Ayarlar > Hareket altında her kenar için kalınlık slider'ı

### Tam Ekran ve İzin Ekranı Duraklatma
- **Tam ekran**: Video ve oyunlarda kenar hareketlerini otomatik kapatma
  (varsayılan: açık)
- **İzin ve güvenlik ekranları**: APK yükleme, izin verme ve güvenlik
  ekranlarında hareketleri kapatma (varsayılan: açık)
- MIUI/HyperOS paket yükleyici ve izin denetleyicisi de tanınıyor

### Diğer
- GestureMapCard canlı prova artık kenar başına hassasiyet kullanıyor
- Deprecation uyarıları temizlendi
- Titreşim artık şiddet ayarına duyarlı (%0–100), sıfırlanınca tamamen kapanıyor
- İsteğe bağlı tıklama sesi: hareket tetiklendiğinde kısa ton sesi
- Eylem simgeleri: hareket sırasında eşleşen eyleme özel Unicode simge
  gösteriliyor (← ⌂ ⊞ 🔊 vb.) — ayrı simge kategorisi gereksiz
- Animasyon hızı ve boyutu ayarlanabilir (0.5x–2.0x)
- Ayarlar > Görünüm yeniden düzenlendi: animasyon, titreşim, ses ve renk
  bir arada
- Eylem seçicideki simgeler yeni eylemler için güncellendi

### Arayüz Tasarım Güncellemesi
- **Tema yenilendi:** Dinamik renk desteği (Android 12+ Material You),
  modern renk paleti, daha yumuşak köşe yarıçapları (6–28 dp)
- **Ana ekran kompaktlaştı:** Hantal kartlar kaldırıldı, renkli metrik
  kutucukları eklendi, durum bildirimi daha zarif
- **Glass-effect kartlar:** `AkisGlassCard` komponenti ile tüm ekranlarda
  tutarlı cam görünümü — HomeScreen, RuleListScreen, GestureMapCard
- **Eylem ikonları renkli:** Her eylem kategorisine özel renk (gezinme=mavi,
  sistem=kırmızı, panel=yeşil, medya=sarı, root=kırmızı)
- **Hareket haritası:** Telefon silüeti içinde renkli bölge overlay,
  yarım telefon görseli (`EdgeZoneVisual`) ile kenar/bölge gösterimi
- **Animasyonlar:** 8 farklı geri bildirim animasyonu (Akış, Neon, Altıgen,
  Küre, Damla, Baloncuk, Kart, Sade simge)
- **Slider'lar:** Tema renklerine uyumlu thumb ve track renkleri
- Karanlık tema: Zengin kontrastlı koyu mavi-mor palet

### Yeni Eylemler (FNG eşliği)
- **Menü** — `KEYCODE_MENU` tuş kodu gönderir
- **Otomatik döndürme aç/kapat** — `ACCELEROMETER_ROTATION` değiştirir
- **Dikey / Yatay yön** — Geçerli uygulamanın yönünü zorlar
- **Tek el modu** — Xiaomi/MIUI/HyperOS one-hand mode tetikler
- **Sesli arama** — `ACTION_VOICE_SEARCH_HANDS_FREE` intenti
- **Sesli asistan** — `ACTION_VOICE_COMMAND` (fallback: sistem asistanı)
- **Uygulama kısayolu** — `ShortcutManager` ile kısayol başlatma
- **Özel tuş kodu** — Kullanıcının seçtiği herhangi `KeyEvent`
- **Gezinme çubuğu göster/gizle** — Root ile `policy_control` değiştirir

### Kenar Başına Hassasiyet
- Her kenar için ayrı damping (`leftDamping`, `rightDamping`, `bottomDamping`)
  ve eşik mesafesi (`leftSwipeThresholdDp`, ...) ayarı eklendi
- Sol ve sağ kenar için dikey konum aralığı (`leftVerticalStart/End`,
  `rightVerticalStart/End`) — tek el kullanımını kolaylaştırır
- Ayarlar > Hareket bölümünde her kenar için bağımsız slider'lar

### Bekletme Eylemi Geliştirmeleri
- **Eşikte çalıştır** modu: Bekletme eylemi artık parmak kalkmadan,
  eşik geçilir geçilmez tetiklenebilir (`HoldFireMode.ON_THRESHOLD`)
- **Parmak kalkınca çalıştır** varsayılan mod korundu (`HoldFireMode.ON_RELEASE`)
- Seçenek Ayarlar > Hareket altında

### Yön Doğruluğu ve Histerezis
- Yan kenarlarda yön sapma kontrolü: beklenen yönden `35°` (varsayılan)
  sapmadan fazlası reddedilir
- Histerezis oranı (`0.25` varsayılan): eşik geçildikten sonra parmak
  küçük geri hareketlerinde durum düşmez
- `GestureCancelPolicy` artık yapılandırılabilir histerezis kullanıyor

### Görsel Arayüz
- Ayarlar > Hareket: Her kenar için hassasiyet ve eşik slider'ları
- Bekletme modu seçici (Eşikte / Parmak kalkınca)
- Eylem kategorilerine "Döndürme" ve "Sistem Arayüzü" eklendi

- HyperOS koruması artık yalnızca erişilebilirlik ayarındaki kaydı değil,
  hizmetin gerçekten bağlı olup olmadığını da kontrol ediyor. Ayar açık fakat
  bağlantı düşmüşse yalnızca Akış bileşeni olay sonrasında yeniden bağlanıyor.
- Ekran açma, kilit açma, cihaz açılışı ve uygulama güncellemesi kontrollerine
  kısa bağlanma bekleme süresi ve 30 saniyelik yeniden deneme sınırı eklendi;
  saniyelik sorgu veya sürekli kapat-aç döngüsü kullanılmıyor.
- Kullanıcı kutucuk, intent veya etkinlik üzerinden Akış'ı durdurduğunda ön plan
  koruma hizmeti de kapanıyor ve olay kontrolleri kullanıcı kararını geri almıyor.
- Eylem seçimine telefondaki başlatılabilir uygulamaları listeleyen ve arayan
  `Uygulama aç` bölümü eklendi. Uygulama adı doğrudan genel eylem aramasında da
  bulunabiliyor ve seçim yedeklere paket adıyla kaydediliyor.
- Seçilen uygulamalar eylem seçicisinde, kural tablosunda ve ayrıntı ekranında
  genel bir simge yerine kendi gerçek uygulama simgesiyle gösteriliyor.
- Artık kullanılmayan eski eylem modeli ve içindeki uygulanmamış klavye seçici
  yolu kaldırıldı; hareket motoru tek `ActionNode` yürütme hattını kullanıyor.
- Alan adlarındaki teknik `üçte bir` ifadeleri kaldırıldı. Dikey kenarlarda
  `Üst / Orta / Alt bölüm`, alt kenarda `Sol / Orta / Sağ bölüm` dili
  kullanılıyor; özel alanlar da anlaşılır yüzde aralığıyla gösteriliyor.
- Yeni kural oluşturma akışı üç adımdan iki adıma indirildi. Kenar ve alan aynı
  ekranda, hızlı ve bekletmeli hareketler ise tek özet ekranında ayarlanıyor.
- Yeni kural ekranındaki uzun açılır eylem listesi kaldırıldı. Mevcut kurallarla
  aynı aranabilir eylem seçici kullanılıyor; seçilen eylem tek dokunuşla
  değiştirilebiliyor veya kaldırılabiliyor.
- Hareket tablosunda boş `Hızlı` veya `Beklet` alanına dokununca ara pencere
  kaldırıldı; ilgili eylem artık doğrudan atanabiliyor.
- Eylem seçimine arama alanı ve sık kullanılan eylemler bölümü eklendi. Geri,
  ana ekran, son uygulamalar, uygulama geçişi ve temel paneller tek dokunuşla
  seçilebiliyor.
- Alt kenardaki uygulama geçişi için parmağı izleyen yön kapsülü eklendi.
  Sağ/sol yön oku hareket sırasında görünür; eşik tamamlandığında kapsül çerçeve
  ve dokunsal geri bildirimle hazır durumunu bildirir, işlem yine bırakınca çalışır.
- Yatay uygulama geçişini dikey alt-kenar hareketinden ayıran saf yön/eşik
  politikası ve birim testleri eklendi.
- Eylem listesinde bulunmasına rağmen çalışmayan `Fener` işlemi gerçek kamera
  fener denetimine bağlandı. Gerekli kamera izni yalnız kişisel profilde root
  üzerinden veriliyor; erişilebilirlik hizmeti izin verilmeden önce kameraya
  erişmeye çalışmıyor.
- Ana ekran; hizmet durumu, etkin hareketler, kullanılan kenarlar ve koruma
  bilgisini tek bakışta gösteren kompakt bir gösterge paneline dönüştürüldü.
- Yedek yükleme işlemine, mevcut ayarların değiştirileceğini açıkça gösteren
  son onay adımı eklendi.
- Ayarlar ekranında artık kullanılmayan eski geniş kart bileşenleri temizlendi.
- Ayarlar ekranına tüm hareket kurallarını, görünümü ve uygulama engellerini
  JSON dosyasına yedekleme ve geri yükleme eklendi.
- MacroDroid için başlat, durdur ve durum değiştir işlemleri hem anlaşılır
  uygulama etkinlikleri hem de Locale/Tasker uyumlu eklenti olarak eklendi.
- Erişilebilirlik hizmetinin kısa ve tam bileşen adlarının çift kayıt
  oluşturması önlendi.
- Alt kenarda sağa veya sola sürükleyerek açık uygulamalar arasında iki yönde
  geçiş eklendi. Önceki/sonraki uygulama işlemleri artık kararlı bir uygulama
  sırası üzerinden çalışıyor.
- Kenar alanlarının yan genişlik ve alt yükseklik ölçekleri artık 0 dp'den başlıyor.

### Düzeltildi

- HyperOS/Android 15 üzerinde erişilebilirlik hizmeti bağlanırken hareket
  katmanlarının bazen sistem pencere anahtarı hazır olmadan oluşturulması
  giderildi. Sol, sağ ve alt hareket alanları artık hizmet hazır olduktan sonra
  güvenli biçimde başlatılıyor.
- Akış Gesture açıkken `Geri` hareketi doğrudan uygulamanın kendi sayfa
  geçmişinde çalışacak şekilde düzeltildi; ana sayfadayken normal çıkış
  davranışı korunur.
- Çekip bekletme hareketi yalnızca o alana ikincil eylem atanmışsa hazırlanıyor.
  Eşik dolduğunda yalnızca hazır duruma geçiyor; eylem parmak bırakılınca bir
  kez çalışıyor. Parmak kenara geri götürülürse hareket tamamen iptal ediliyor.
- `Önceki uygulama` eylemi güvenilmez çift son-uygulamalar çağrısı yerine gerçek
  uygulama geçmişini kullanacak şekilde düzeltildi.

### Eklendi

- Görsel tasarım AI konsepti temel alınarak Hareketler ekranı `Alt / Sol / Sağ`
  sekmelerine ayrılan kompakt tablo düzenine dönüştürüldü. Alan, hızlı eylem ve
  bekletme eylemi aynı satırda görülüyor; silme ve etkinlik seçenekleri üç nokta
  menüsüne taşındı.
- Telefon haritası ana kural listesinden çıkarılarak gerektiğinde açılan ikincil
  `Harita` görünümüne taşındı.
- Ayarlar ekranındaki büyük açılır kartlar kaldırıldı. Ana görünüm kısa bölüm
  başlıkları ve ince ayırıcılı satırlardan oluşuyor; kaydırma çubukları, renk ve
  simge ayrıntıları odaklanmış ayar pencerelerinde açılıyor.
- Ayarlara FNG benzeri `Çalışmayacağı yerler` menüsü eklendi. Hareketler kilit
  ekranında, klavye açıkken, yatay ekranda ve kullanıcının seçtiği uygulamalarda
  ayrı ayrı duraklatılabiliyor; koşul bittiğinde kendiliğinden geri geliyor.
- Sistem durumuna göre duraklatmanın yalnızca seçilen koşullarda çalıştığını
  doğrulayan birim testi eklendi.
- Hızlı Ayarlar'a Akış hareketlerini açıp kapatan ve gerçek hizmet durumunu
  gösteren `Akış` kutucuğu eklendi.
- MacroDroid ve diğer otomasyonların kullanabilmesi için yalnızca Akış'ın kendi
  hizmetini yöneten `START`, `STOP` ve `TOGGLE` yayın intentleri eklendi.
- HyperOS erişilebilirlik kaydını beklenmedik biçimde düşürdüğünde, ekran
  açılışı veya kullanıcı kilit açma olayında root üzerinden yalnızca Akış
  bileşenini geri ekleyen koruma eklendi. Kullanıcı kutucuktan veya `STOP`
  intentiyle kapattığında otomatik onarım yapılmıyor.
- Hareket kuralı kartları yatay sıkışan düğmeler yerine alan başlığı ve her
  harekete ait ayrı, simgeli işlem satırlarıyla yeniden tasarlandı.
- Eylem seçimleri emoji metinleri yerine her işlemin anlamını gösteren gerçek
  vektör simgelerle yenilendi. Gezinme, sistem, panel, medya, donanım ve root
  eylemlerinin her biri kendi simgesini kullanıyor.
- Uzun eylem seçenekleri kategori kategori açılan listelere, kural oluşturma
  ekranındaki eylem seçimi ise tek satırlık açılır alana dönüştürüldü.
- Ayarlar ekranı `Hareket hissi`, `Görünüm`, `Uygulama davranışı` ve `Gelişmiş`
  başlıklarında toplanan açılır bölümlere dönüştürüldü.
- Hareket alanı haritası varsayılan olarak kompakt özet gösteriyor; düzenleme
  veya canlı deneme gerektiğinde tek dokunuşla açılıyor.
- Açık ve koyu temanın yüzeyleri, köşe yapısı ve vurgu renkleri daha modern,
  sakin ve tutarlı bir görsel sistemle yenilendi.
- Telefon haritasına, gerçek hareket motoruyla aynı mesafe, sönümleme ve bekleme
  değerlerini kullanan canlı `Dene` görünümü eklendi.
- Hareket motoru için eşik hesabını tek yerde tutan ortak model ve birim testleri
  eklendi.
- Klavye seçici, ses paneli, sistem asistanı ve sesi kapat/aç eylemleri eklendi.
- Aynı kenar alanının hızlı çekme ve çekip bekletme eylemleri artık tek ekranda
  birlikte atanabiliyor; ikinci eylem için sihirbazdan çıkıp geri dönmek gerekmiyor.
- Kural listesi aynı alana ait iki hareketi tek kartta gösteriyor. Liste ve ayrıntı
  ekranları aynı kayıt durumunu kullandığı için yeni kurala dokununca görülen
  geçici `Kural bulunamadı` hatası giderildi.
- Kullanıcının seçtiği uygulamalar öne geldiğinde hareketleri olay tabanlı olarak
  duraklatan uygulama listesi eklendi. Uygulamadan çıkınca hareket alanları
  kendiliğinden geri gelir; sürekli sorgulama yapılmaz.
- Sol, sağ ve alt kenar alanları hazır bölümlerin yanında yüzde tabanlı başlangıç
  ve bitiş sınırlarıyla ayarlanabilir hâle getirildi. En küçük alan yüzde 10'dur.
- Hareket animasyonu için tüm renk tonlarını kapsayan renk tonu, canlılık ve
  parlaklık seçicisi ile yüzde 10–100 saydamlık ayarı eklendi.
- Başlangıç, genel kullanım, tek el, ileri seviye, üretkenlik, medya ve açıkça
  işaretlenmiş root kullanımını kapsayan dokuz hazır hareket düzeni eklendi.
- Akış, baloncuk, damla, sade simge ve kapalı animasyon biçimleri eklendi.
- Hızlı çekme ile çekip bekletme için birbirinden bağımsız görsel simge seçimi
  eklendi; ikinci eşik hazır olduğunda görsel ikinci simgeye geçiyor.
- Renk seçicinin parlaklık alt sınırı gerçek siyah için yüzde sıfıra indirildi;
  siyah, beyaz ve varsayılan mavi kısa yolları eklendi.
- Ayarlar ekranındaki boşluklar ve kart içleri daha kompakt hale getirildi.
- Hızlı çekme ve çekip bekletme simgelerine `<`, `>`, `<<` ve `>>`
  seçenekleri eklendi.
- Kurallar ekranına gerçek bölge oranlarını gösteren, renkli kenarlarına
  dokunularak doğrudan düzenlenebilen telefon biçimli hareket haritası eklendi.
- Haritadaki bir alana dokunulduğunda hızlı çekme ve çekip bekletme eylemleri
  artık aynı pencerede atanabiliyor; tek tek sayfalar arasında dönmek gerekmiyor.
- Haritadaki alanlar parmakla taşınabiliyor; başlangıç veya bitiş ucundan
  sürüklendiğinde güvenli asgari uzunluk korunarak uzatılıp kısaltılabiliyor.
- Eşiğe ulaşan hareket parmak yeniden kenara kadar geri götürüldüğünde iptal
  ediliyor; görsel başlangıç durumuna dönüyor ve parmak bırakıldığında eylem çalışmıyor.
- `Uygulama aç` eylemi artık tahmini ekran adı kullanmıyor; Android'in ilgili
  uygulama için bildirdiği gerçek açılış ekranını kullanıyor.
- Aynı tetikleme alanına ayrı `Hızlı çekme` ve `Çekip bekletme` eylemleri
  atanabilen iki aşamalı hareket modeli eklendi.
- Eski `SWIPE`, `SHORT_SWIPE` ve `LONG_SWIPE` kayıtları yeni modele kayıpsız
  taşınıyor.
- Çekip bekletme süresi Ayarlar ekranında `150–700 ms` arasında değiştirilebilir.
- Bekletme eşiği için sarı görsel durum, ayrı titreşim ve parmak titremesine
  dayanıklı geri dönüş toleransı eklendi.
- Root ile öndeki uygulamayı kişisel profilde zorla durdurma eylemi eklendi.
  Akış Gesture, Android sistemi, System UI, Ayarlar ve kullanılan launcher
  güvenlik nedeniyle korunur.
- Ayarlar ekranına APatch/root erişimini gerçek uygulama sürecinden sınayan
  `Root hazır` durum kartı eklendi.
- Hızlı çekme ve bekletme kurallarının aynı alanda çakışmadan derlendiğini
  doğrulayan birim testleri eklendi.
- FNG'nin hareket mantığı, eylemleri, root işlevleri, görsel seçenekleri ve
  sistem uyumluluğu incelenerek `FNG_PARITY_TARGET.md` hedef belgesi oluşturuldu.
- Proje adı ve hedefleri Akış Gesture olarak belirlendi.
- Türkçe README ve ilk ürün yol haritası oluşturuldu.
- Sürdürülebilir geliştirme kuralları eklendi.
- Kaynak proje OpenSwipe, Git üzerinde `upstream` olarak ayrıldı.
- Kaynak projedeki makineye özel ve geçersiz Gradle proxy ayarı kaldırıldı.
- Proje ve kullanıcıya görünen uygulama adı Akış Gesture olarak değiştirildi.
- Eski manifest paket bildirimi kaldırılarak Android Gradle Plugin uyarısı giderildi.
- Uygulama kimliği kaynak projeden ayrılarak `com.omer.akisgesture` yapıldı.
- Erişilebilirlik bağlanırken ana iş parçacığını kilitleyen senkron DataStore
  okuması kaldırıldı; güvenli varsayılanlar anında, kayıtlı kurallar asenkron
  yükleniyor.
- Android 14 ve üzeri için hareket hizmetinin gerekli `specialUse` ön plan
  hizmeti izni ve açıklaması eklendi.
- Uygulamanın görünen metinleri sade Türkçe anlatımla yenilendi.
- Sol, sağ ve alt kenar hareketlerine parmağı izleyen akıcı dalga animasyonu eklendi.
- Hareket eşiği renk değişimi ve üç aşamalı dokunsal geri bildirimle görünür hâle getirildi.
- Parmak bırakıldığında görselin sertçe kaybolması yerine yumuşak geri çekilmesi sağlandı.
- Görsel geri bildirim dokunmayı engellemeyen ayrı bir erişilebilirlik katmanına bağlandı.
- Island iş profiline yanlışlıkla kurulum yapılmaması için cihaz kurulum komutu
  yalnızca kişisel kullanıcıyı (`--user 0`) hedefleyecek şekilde belgelendi.

### Bilinen durumlar

- Uygulama profilleri, ayrıntılı alan geometrisi, alternatif animasyon biçimleri
  ve ayar yedekleme/geri yükleme henüz eklenmedi.
- Fener eylemi kamera izni gerektiren güvenli akış tamamlanana kadar devre dışıdır.

## [0.1.0-foundation] - 2026-07-30

### Eklendi

- MIT lisanslı OpenSwipe kaynak geçmişi başlangıç tabanı olarak alındı.
