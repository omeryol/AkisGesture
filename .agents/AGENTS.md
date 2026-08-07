# Akış Gesture - Project Guidelines & Release Standard

## Keystore & Signing Security
- Keystore credentials (passwords, alias) MUST NEVER be hardcoded into `app/build.gradle.kts` or any file tracked by Git.
- Keystore credentials must be stored in `keystore.properties` (which is included in `.gitignore`) and loaded dynamically at build time.

## Git & Version Control Standards
- **Yerel Commit Mantığı**:
  - Yapılan her değişiklikte ve anlamlı geliştirme adımında mutlaka yerel Git commit'i (`git commit`) alınmalıdır.
- **GitHub Commit Cleanliness & Squash**:
  - Geçici test adımları, ara düzenlemeler veya gereksiz Git detayları GitHub uzak deposuna (remote repository) aktarılmamalıdır.
  - GitHub'a aktarım yapılmadan önce commit geçmişi temiz tutulmalı, gereksiz ara commit'ler birleştirilmeli (squash/clean history) ve GitHub'a derli toplu, net commit'ler ile yansıtılmalıdır.

## Git Remote Isolation (Kritik Güvenlik Kuralı)
Bu projede iki remote tanımlıdır ve aralarındaki fark kesinlikle korunmalıdır:

| Remote | URL | İzin Verilen İşlem |
|--------|-----|-------------------|
| `origin` | `https://github.com/omeryol/AkisGesture.git` | ✅ Push, fetch, release — **tek yetkili hedef** |
| `upstream` | `https://github.com/ARCJ137442/OpenSwipe.git` | ⛔ Sadece `fetch` (orijinal kaynaktan güncelleme çekmek için) |

- **`upstream`'e asla `git push` yapılmamalıdır.** OpenSwipe bağımsız bir üçüncü taraf projesidir.
- **`git push` her zaman yalnızca `origin`'e** (`git push origin main --tags`) yapılmalıdır.
- **`gh release create` komutunda her zaman `--repo omeryol/AkisGesture` bayrağı belirtilmelidir.**
  - Bu bayrak olmadan `gh` aracı yanlış repo (OpenSwipe) ile çakışan tag hatası verebilir.
  - Doğru kullanım: `gh release create vX.Y.Z <apk> --title "..." --notes-file "..." --repo omeryol/AkisGesture`


## GitHub Release Specification Standard
All GitHub Releases for `AkisGesture` MUST follow this exact structure:

1. **Release Title Standard**:
   `Akış Gesture vX.Y.Z` (e.g., `Akış Gesture v1.2.0`)

2. **Release Notes Body Standard**:
   ```markdown
   ## 🇹🇷 Türkçe

   [Türkçe detaylı yenilikler ve geliştirme özeti]

   ---

   ## 🇬🇧 English

   [English detailed feature highlights & changelog summary]

   ---

   ## 📦 İndirme / Downloads
   - **İmzalı APK (Signed APK)**: `AkisGesture-vX.Y.Z.apk`
   ```

3. **Versioning & Tagging**:
   - Always update `versionCode` (always +1) and `versionName` (`vX.Y.Z` SemVer) in `app/build.gradle.kts`.
   - Update `CHANGELOG.md`, `README.md` (Turkish), `README-en.md` (English), and `VersionHistoryProvider.kt` (in-app history).
   - Create signed APK (`gradlew assembleRelease`) and upload asset using `gh release create`.

## MIUI Türkiye Forum Release Update Standard
Her yeni sürüm yayınlandığında MIUI Türkiye forum konusu (`https://forum.miuiturkiye.net/konu/akis-gesture-...`) aşağıdaki adımlarla güncellenmelidir:

1. **Konu Başlığı Güncelleme Standardı**:
   - Format: `🌀 Akış Gesture vX.Y.Z - Android İçin Özelleştirilebilir Kenar Hareketleri [Açık Kaynak] 🎨`

2. **Post #2 (Sürüm Geçmişi / Update History) Standart Tasarım Kuralı**:
   - Post #2 her zaman standart ve uniform bir BBCode şablonuna sahip olmalıdır.
   - Her yeni sürüm çıktığında, en güncel sürüm bloğu Post #2'nin **en üstüne** eklenmeli ve altına `[HR][/HR]` ayırıcı çizgi konulmalıdır.
   - **Standart BBCode Şablonu**:
     ```bbcode
     [B][SIZE=4]🌀 vX.Y.Z (DD.MM.YYYY)[/SIZE][/B]
     • [B]⚡/🐛 [Kategori/Başlık][/B]: [Açıklama ve detaylar]
     • [B]🔒/🎨 [Kategori/Başlık][/B]: [Açıklama ve detaylar]
     • [B]🎯/✨ [Kategori/Başlık][/B]: [Açıklama ve detaylar]

     [HR][/HR]
     ```
   - **Tasarım Kuralları**:
     - Başlık her zaman `[B][SIZE=4]🌀 vX.Y.Z (Tarih)[/SIZE][/B]` formatında olmalıdır.
     - Maddeler `• [B]Emoji Başlık[/B]: Açıklama` şeklinde hizalı ve temiz yazılmalıdır.
     - Sürümler arası her zaman `[HR][/HR]` yatay çizgi ile ayrılmalıdır.
     - Rasgele font boyutları, karmaşık renkler veya tutarsız girintiler kesinlikle kullanılmamalıdır.

## Versioning & Release Checklist Standard (SemVer & In-App Sync)
- **Semantic Versioning Standard (`versionName`)**:
  - `X.Y.Z` format must be strictly enforced:
    - **MAJOR (X)**: Radical architecture changes or breaking redesigns.
    - **MINOR (Y)**: Backwards-compatible new features, new edge gestures, major settings cards.
    - **PATCH (Z)**: Backwards-compatible bug fixes, vibration/haptic fixes, minor UI tweaks.
- **Android Version Code (`versionCode`)**:
  - `versionCode` MUST ALWAYS be incremented by +1 for every single published build (e.g., 46 ➡️ 47). It must never be decremented or kept unchanged.
- **Mandatory Synchronized Files Checklist**:
  - `app/build.gradle.kts`: Increment `versionCode` (+1) and update `versionName`.
  - `CHANGELOG.md`: Add new `## [X.Y.Z] - YYYY-MM-DD` section with detailed Turkish and English bullet points.
  - `README.md` (TR) & `README-en.md` (EN): Update version release highlights section.
  - `VersionHistoryProvider.kt`: Append `VersionHistoryItem` entry for the in-app version history dialog.
  - **Clean Release Notes Requirement**: Update dialog MUST render clean, filtered release highlights extracted via `GithubReleaseChecker.extractCleanReleaseNotes` (never raw full README text).

## In-App Update & Pre-Release Testing Protocol
- **Güncelleme Butonu Test Standardı**:
  - Test aşamasında kullanıcı GitHub Release yükleme onayı verdiğinde, yeni sürüm yayınlanmadan önce ADB bağlantısı olan cihaza test amaçlı **geçici olarak daha düşük sürüm numarasına** (önceki veya düşük `versionCode`/`versionName`) sahip bir APK derlenip yüklenmelidir.
  - Bu işlem, yeni sürüm GitHub'a yüklendiğinde kullanıcının cihazdaki uygulama içi güncelleme (In-App Update) kontrolünü ve güncelleme butonunu gerçek ortamda test edebilmesini sağlar.
  - **Önemli**: Test yüklemesi sonrasında git ortamındaki `app/build.gradle.kts` ve release APK'sı hedeflenen güncel yüksek sürüm numarasıyla kalmalıdır.
- **ADB Yükleme Parametreleri**:
  - Test aşamasındaki tüm ADB APK yüklemeleri **sadece 0 kullanıcısına** ve **uygulama verilerini silmeden (reinstall/upgrade)** yapılmalıdır (`adb install -r --user 0 <apk_yolu>`).
  - Başka hiçbir kullanıcı profiline yükleme yapılmamalıdır.


## APK Build & Install Standards
- **Debug vs Release Ayrımı**:
  - Geliştirme ve test süreçlerinde her zaman `assembleDebug` ile derlenen **debug APK** kullanılmalıdır.
  - GitHub Release öncesinde ve kullanıcıya teslim edilecek her APK'da mutlaka `assembleRelease` ile derlenen **imzalı release APK** kullanılmalıdır.
  - Debug APK hiçbir zaman GitHub Release asset'i olarak yüklenmemelidir.

## Code Verification Standard
- **Derleme Doğrulama Zorunluluğu**:
  - Birden fazla dosyayı etkileyen her kod değişikliğinden sonra `./gradlew test` çalıştırılmalı ve `BUILD SUCCESSFUL` çıktısı alınmalıdır.
  - Test başarısız olursa yerel Git commit alınmamalı, hata önce giderilmelidir.

## String Resources Bilingual Sync Rule
- **İkili String Güncelleme Zorunluluğu**:
  - Yeni bir UI string kaynağı eklendiğinde **hem** `app/src/main/res/values/strings.xml` (EN) **hem de** `app/src/main/res/values-tr/strings.xml` (TR) eş zamanlı güncellenmelidir.
  - Sadece birini güncellemek kabul edilemez; eksik çeviri bırakılmamalıdır.

## New Feature Development Template
- Yeni bir özellik geliştirilirken aşağıdaki sıra takip edilmelidir:
  1. `GestureConfig.kt` → Yeni alan ve DataStore anahtarı eklenir.
  2. `OpenSwipeApp.kt` → DataStore'dan okuma ve güncelleme fonksiyonu eklenir.
  3. `HomeViewModel.kt` → ViewModel setter fonksiyonu eklenir.
  4. `SettingsScreen.kt` → UI bileşeni (switch, card vb.) eklenir.
  5. `values/strings.xml` + `values-tr/strings.xml` → Çift dilli string kaynakları güncellenir.
  6. `./gradlew test` → Derleme ve birim testleri doğrulanır.
  7. `adb install -r --user 0 <apk>` → Cihaza yüklenerek manuel test yapılır.
  8. Yerel Git commit alınır.

## Breaking Change (Kırıcı Değişiklik) Tanımı
- Aşağıdaki durumlar **MAJOR (X)** sürüm artışı gerektirir:
  - `DataStore` anahtar adlarının (`PreferencesKey`) yeniden adlandırılması veya silinmesi (kullanıcı verisi kaybına yol açar).
  - `GestureConfig.kt` veri yapısında geriye dönük uyumsuz değişiklik.
  - `GestureRule` veya `GestureRuleGraph` serileştirme şemasının kırılması.
  - Ana navigasyon mimarisinin (NavHost route'ları) köklü yeniden yapılanması.
- Aşağıdaki durumlar **MINOR (Y)** sürüm artışı gerektirir:
  - Geriye dönük uyumlu yeni özellik, yeni ayar kartı, yeni jest eylemi.
- Aşağıdaki durumlar **PATCH (Z)** sürüm artışı gerektirir:
  - Hata düzeltme, titreşim/haptic düzeltme, küçük UI tweaks, string düzeltmeleri.

## Plan Approval Threshold
- Aşağıdaki durumlarda kod değişikliğine başlamadan önce **özet plan kullanıcıya sunulmalı ve onay alınmalıdır**:
  - 3'ten fazla farklı dosyayı etkileyen özellik geliştirmeleri.
  - Mimari değişiklikler (`GestureConfig`, `OpenSwipeApp`, servis katmanı).
  - Sürüm yükseltme ve GitHub Release hazırlığı.
  - Kullanıcı verilerini etkileyebilecek (DataStore, kural serialize) değişiklikler.
- Tek dosyalık düzeltmeler ve küçük UI tweaks için plan onayı gerekmez, doğrudan uygulanır.

## Commit Message Format Convention
- Tüm yerel Git commit mesajları aşağıdaki `type(scope): description` formatına uymalıdır:
  - `feat(settings): ...` — Yeni özellik ekleme
  - `fix(gesture): ...` — Hata düzeltme
  - `docs(about): ...` — Dokümantasyon güncellemesi
  - `refactor(viewmodel): ...` — Kod yeniden yapılandırma (davranış değişikliği yok)
  - `chore(build): ...` — Build, bağımlılık, config değişikliği
  - `release: vX.Y.Z` — Sürüm yayınlama commit'i
- Commit mesajları açıklayıcı olmalı; `fix bug`, `update file` gibi belirsiz mesajlar kullanılmamalıdır.

## Agent Operations & Token Efficiency
- **Ekran Görüntüsü Kısıtlaması**:
  - Kullanıcının açık ve net bir talebi/isteği olmadığı sürece kesinlikle ekran görüntüsü (screenshot) alınmamalıdır.
- **Token & Bağlam Verimliliği**:
  - Token kullanımı ve çalışma adımları maksimum verimlilikle yürütülmelidir. Gereksiz dosya taramaları, lüzumsuz derin kod analizleri ve doğrudan amaç dışı incelemeler yapılmamalıdır.

## GitHub Push & Release Pre-Flight Mandatory Reminders & Testing Rule
- **GitHub Push Öncesi Değişiklik Özeti & Zorunlu Test Hatırlatması**:
  - GitHub uzak deposuna (`origin/main`) herhangi bir `git push` veya `gh release create` işlemi yapılmadan önce, ajan kullanıcıya **yapılan tüm değişikliklerin net özetini** ve **kritik kullanıcı akışlarının (eylem ekleme/silme, ayar kaydetme, jest tetikleme vb.) cihaz üzerinde bizzat test edilip edilmediğini** sormalı ve hatırlatmalıdır.
  - Ajan, test edilmemiş hiçbir kritik işlevi (özellikle jest kuralı ekleme/silme, DataStore yazma ve ana UI akışları) cihazda çalıştırmadan veya onay almadan release/push işlemine geçmemelidir.

## Strict Root Cause Analysis & Anti-Patchwork Development Rule
- **Kök Neden Analizi Zorunluluğu (Anti-Yamalı Bohça Kuralı)**:
  - Bir hata bildirildiğinde veya bir işlev çalışmadığında kesinlikle yüzeysel tahminlerle, deneme-yanılma yamalarıyla veya peş peşe ara derlemelerle müdahale edilmemelidir.
  - Ajan, kod yazmadan ve yeniden derlemeden önce verinin UI'dan ViewModel'e, servise ve saklama katmanına (DataStore) kadar olan **tüm uçtan uca akışını tam olarak izlemeli (trace) ve kök nedeni kesin olarak teşhis etmelidir**.
- **İzinsiz Kod Değişikliği Yasağı & Abartısız İletişim**:
  - Hatanın nedeni tam olarak kesinleşmeden veya birden fazla dosyayı etkileyen mantıksal müdahaleler yapılmadan önce kullanıcıya net durum bildirilmeli, abartılı/kendini öven ifadeler kesinlikle kullanılmamalıdır.
  - Ara deneme yamalarıyla ve aceleci varsayımlarla kullanıcı cihazı veya derleme süreci lüzumsuz meşgul edilmemelidir.
- **ADB Logcat Teşhis Zorunluluğu**:
  - Cihaz üzerinde çalışmama, UI etkileşiminin yanıt vermemesi veya davranış hatalarında kesinlikle tahmine dayalı yama yapılmamalıdır.
  - ADB bağlantısı mevcut olduğunda ilk iş olarak `adb logcat -d` veya uygun filtreli log çıktısı çekilmeli, canlı runtime logları ve hata izleri incelenerek teşhis koyulmalıdır.
