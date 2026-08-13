package io.github.omeryol.akisgesture.util

data class VersionHistoryItem(
    val version: String,
    val date: String,
    val isCurrent: Boolean = false,
    val changesTr: List<String>,
    val changesEn: List<String>,
)

object VersionHistoryProvider {
    val HISTORY = listOf(
        VersionHistoryItem(
            version = "1.7.0",
            date = "2026-08-13",
            isCurrent = true,
            changesTr = listOf(
                "🎨 İkon ve Renk Çeşitliliği: 5 yeni ikon ailesi (Fluent, Pixelart, Ionicons, Lucide, Radix) ile Cyber Neon ve Accent renk modları.",
                "🌊 3D Fizik ve Sıvı Animasyonları: Blinn-Phong ışıklandırmalı yüzey gerilimi, su kabarcığı ve kor yataklı ateş efektleri.",
                "📱 Sadeleştirilmiş Görünüm: Görünüm sekmesinde 3 genişletilebilir bölüm içeren birleşik kart tasarımı.",
                "⚙️ Gelişmiş Deneyim: Animasyon kapatma seçeneği (NONE) ve stabilize edilmiş L-jest göstergeleri.",
            ),
            changesEn = listOf(
                "🎨 Icon & Color Variety: 5 new icon families (Fluent, Pixelart, Ionicons, Lucide, Radix) with Cyber Neon and Accent color modes.",
                "🌊 3D Physics & Fluid Animations: Overhauled surface tension, water bubbles, dynamic vortex, and glowing flame animations.",
                "📱 Unified Appearance: Görünüm tab reorganized into 3 expandable sections with anchored edge map geometry.",
                "⚙️ Enhanced Usability: Added option to disable feedback animations (NONE) and stabilized L-gesture indicators.",
            )
        ),
        VersionHistoryItem(
            version = "1.6.1",
            date = "2026-08-11",
            isCurrent = false,
            changesTr = listOf(
                "Halka Menüsü: Her kenar için üç eylemli, simgeli ve ayarlanabilir halka grubu.",
                "Canlı Ayarlama: Boyut, aralık, uzaklık ve yarım halka eğriliği gerçek overlay üzerinde görülebilir.",
                "İzin ve Tanılama: Eksik izin etkileri ana sayfada açıklanır; tanılama APK'sı halka akışlarını izler.",
            ),
            changesEn = listOf(
                "Ring Menu: Three configurable, icon-based action rings for every edge.",
                "Live Tuning: Size, spacing, inset, and half-arc curvature are visible on the real overlay.",
                "Permissions and Diagnostics: Missing permission effects are explained on Home; diagnostic builds trace ring flows.",
            )
        ),
        VersionHistoryItem(
            version = "1.5.1",
            date = "2026-08-08",
            isCurrent = false,
            changesTr = listOf(
                "🧭 Tutarlı Kenar Düzeni: Ana sayfa, harita, Ayarlar ve Hareketler artık Sol → Alt → Sağ sırasını kullanır.",
                "🔒 Otomasyon İzni: Otomasyon uygulamaları yalnızca açık kullanıcı izniyle Başlat, Durdur veya Aç/Kapat komutunu kullanabilir.",
                "🛡️ Net Root Sınırı: Temel hareketler root istemez; uygulama sistem gezinme çubuğunu yönetmez.",
                "✈️ Topluluk ve İkon: Telegram bağlantıları eklendi, uygulama ikonu yenilendi.",
            ),
            changesEn = listOf(
                "🧭 Consistent Edge Order: Home, the map, Settings, and Gestures now use Left → Bottom → Right.",
                "🔒 Automation Consent: Automation apps can only Start, Stop, or Toggle the service after explicit user approval.",
                "🛡️ Clear Root Scope: Core gestures do not need root, and the app does not manage the system navigation bar.",
                "✈️ Community and Icon: Telegram links were added and the app icon was refreshed.",
            )
        ),
        VersionHistoryItem(
            version = "1.5.0",
            date = "2026-08-08",
            isCurrent = false,
            changesTr = listOf(
                "🔄 Güvenilir Güncelleme Merkezi: Sürüm notları seçili uygulama dilinde gösterilir.",
                "🔐 Doğrulanmış İndirme: APK yalnızca doğru release asset'i ve SHA-256 özeti doğrulandıktan sonra yükleyiciye aktarılır.",
                "🕒 Son Kontrol: Hakkında kartı son GitHub kontrol zamanını gösterir.",
            ),
            changesEn = listOf(
                "🔄 Reliable Update Center: Release notes follow the selected app language.",
                "🔐 Verified Download: The installer opens only after the correct release asset and SHA-256 digest are verified.",
                "🕒 Last Check: The About card shows the latest GitHub check time.",
            )
        ),
        VersionHistoryItem(
            version = "1.4.0",
            date = "2026-08-08",
            isCurrent = false,
            changesTr = listOf(
                "📱 3B Kenar Haritası: Sol ve sağ tetik alanlarını ana sayfadaki telefon haritasından boyutlandırın ve dikeyde taşıyın.",
                "📏 Canlı Ölçüler: İki kenarın uzunluğu ve başlangıç konumu birlikte gösterilerek eşit hizalama kolaylaştırıldı.",
                "✨ Akıcı Önizleme: Gerçek tetik alanı sürükleme boyunca anında güncellenir; ayar işlem sonunda kalıcı olarak kaydedilir.",
                "🌍 Arayüz Düzenlemeleri: Hareketler, eylem ekleme ve ayarlar ekranlarında çeviri ve görünüm tutarlılığı iyileştirildi.",
            ),
            changesEn = listOf(
                "📱 3D Edge Map: Resize and reposition left and right trigger areas directly from the Home screen phone map.",
                "📏 Live Measurements: Length and start position for both edges are shown together for easier matching.",
                "✨ Smooth Preview: The real trigger area updates live while dragging, then saves when editing finishes.",
                "🌍 Interface Refinements: Improved translation and visual consistency across Gestures, Add Action, and Settings.",
            )
        ),
        VersionHistoryItem(
            version = "1.3.7",
            date = "2026-08-07",
            isCurrent = false,
            changesTr = listOf(
                "🐛 Kural Ekleme Düzeltmesi: Kural ekleme diyaloğunda eylem seçici (ActionPicker) navigation tetiklediğinde diyalog durumunun sıfırlanması sorunu giderildi.",
                "⚡ Inline ActionPicker: Eylem seçici artık navigation olmadan tam ekran dialog olarak açılıyor; seçilen eylemler ve tüm diyalog durumu korunuyor.",
            ),
            changesEn = listOf(
                "🐛 Rule Creation Fix: Fixed critical bug where ActionPicker navigation caused AddRuleDialog state reset, preventing any rule from being added.",
                "⚡ Inline ActionPicker: Action picker now opens as a full-screen inline dialog without navigation, preserving all dialog state correctly.",
            )
        ),
        VersionHistoryItem(
            version = "1.3.6",
            date = "2026-08-07",
            isCurrent = false,
            changesTr = listOf(
                "⚡ Mimarisi Yenilenen Aksiyon & Kural Yönetimi: Aksiyon seçici (ActionPicker) ve jest kuralı oluşturma mimarisi PendingActionTarget durum yönetimi ile baştan sona yeniden yapılandırıldı.",
                "🔒 Kesintisiz Veri Kaydı: Aksiyon seçildiği an yerinde güncelleme yapılması sağlandı, arka planda kaydetmeyi engelleyen durumlar kaldırıldı.",
                "🎯 Boş Slot & Çakışma İyileştirmeleri: Boş alanlara eylem ekleme ve mükerrer kural çakışmaları tamamen çözüldü.",
            ),
            changesEn = listOf(
                "⚡ Refactored Action & Rule Architecture: Rebuilt action picker (ActionPicker) and gesture rule creation flow using PendingActionTarget state management.",
                "🔒 Atomic Data Persistence: Action selections now perform in-place updates instantly upon selection, eliminating silent save-blocking checks.",
                "🎯 Empty Slot & Conflict Handling: Completely resolved rule creation for empty slots and eliminated duplicate rule conflicts.",
            )
        ),
        VersionHistoryItem(
            version = "1.3.5",
            date = "2026-08-07",
            isCurrent = false,
            changesTr = listOf(
                "🎨 Hazır Renk Şablonları & Yumuşatılmış Renk Geçişleri: 7 adet 3'lü renk teması, esneme anı için güç eğrili yumuşak renk geçişleri ve akordiyon dikey renk seçici eklendi.",
                "🛡️ Root Bekçi (Watchdog) & Pil Uyarısı: Root cihazlar için 5dk-120dk slider, kademeli pil etkisi uyarısı ve kilit koruma kartı eklendi.",
                "✨ Uyumlu İkon Paketleri & İkon Boyutu Slider: 6 ikon paketi monokrom vektörlerle yenilendi, bağımsız ikon boyutu barı eklendi.",
                "🛠️ Test APK Uyarısı & Hakkında Redizaynı: Yüklü APK GitHub'dan daha güncelse 🛠️ Test sürümü uyarısı ve renkli buton kartları eklendi.",
            ),
            changesEn = listOf(
                "🎨 Color Presets & Smooth Color Transitions: Added 7 3-color palette presets, power-curve smoothed color transitions during gesture stretch, and collapsible color pickers.",
                "🛡️ Root Watchdog & Battery Warning: Added 5min-120min watchdog slider, battery usage warnings, and recents lock card.",
                "✨ Harmonized Icon Packs & Icon Size Slider: Updated all 6 icon packs with distinct symbols and added an independent icon size slider.",
                "🛠️ Dev Build Warning & About Tab Redesign: Added dev build notice when local version is ahead of GitHub and redesigned About tab buttons.",
            )
        ),
        VersionHistoryItem(
            version = "1.3.3",
            date = "2026-08-06",
            isCurrent = false,
            changesTr = listOf(
                "🔇 Haptik Geri Bildirim Düzeltmesi: Tetik bölgesine kazara dokunulduğunda oluşan sürekli titreşim sorunu çözüldü. Haptic geri bildirim artık sadece gerçek hareket algılandığında tetikleniyor.",
            ),
            changesEn = listOf(
                "🔇 Haptic Feedback Fix: Fixed continuous vibration issue when accidentally touching the trigger zone. Haptic feedback now only triggers when a real gesture is detected (armed state).",
            )
        ),
        VersionHistoryItem(
            version = "1.3.2",
            date = "2026-08-06",
            isCurrent = false,
            changesTr = listOf(
                "🔒 Son Kullanılanlar Koruması: 'Tümünü Kapat' butonunun uygulamayı kapatmasını engelleyen gizleme anahtarı ve Recents kilit 🔒 rehber kartı eklendi.",
                "🔑 Şeffaf Root Rehberi: Hakkında bölümüne root'un kesinlikle zorunlu olmadığını ve bu uygulamada yalnızca zorla kapatma ve özel komut için kullanıldığını açıklayan ayrıntılı bilgi kartı eklendi.",
                "📖 Proje GitHub Bağlantısı: Hakkında bölümüne Akış Gesture GitHub deposu linki eklendi.",
                "🕰️ Sürüm Geçmişi Dialogu: Geçmiş sürümlerdeki değişiklikleri listeleyen uygulama içi sürüm geçmişi butonu ve iletişim kutusu eklendi.",
            ),
            changesEn = listOf(
                "🔒 Recents Protection: Added a toggle to hide the app from Recents (preventing accidental 'Clear All' closure) and a padlock 🔒 guide card.",
                "🔑 Transparent Root Guide: Added a detailed info card in About clearly explaining root is NOT required and is only used for force-kill and custom shell commands.",
                "📖 Project GitHub Link: Added Akış Gesture GitHub repository link in the About section.",
                "🕰️ Version History Dialog: Added in-app version history button and dialog listing changes across past releases.",
            )
        ),
        VersionHistoryItem(
            version = "1.3.1",
            date = "2026-08-06",
            isCurrent = false,
            changesTr = listOf(
                "📳 Titreşim Kilitlenme Düzeltmesi: Tetik alanlarına kazara değildiğinde oluşan donanım kilitlenmesi giderildi; parmak ayrıldığında titreşim anında sonlandırılır.",
                "📷 Kamera & Çekim Duraklatması: Kamera vizörü açıkken veya video kaydı yapılırken hareketleri otomatik kapatma seçeneği eklendi.",
                "📞 Telefon Görüşmesi Duraklatması: Gelen aramalarda ve aktif telefon görüşmelerinde jestlerin kapanması sağlandı.",
                "⚪ Kara Liste & Beyaz Liste Modu: Uygulama istisnaları için Kara Liste (seçilenlerde duraklat) ve Beyaz Liste (yalnızca seçilenlerde çalış) seçenekleri entegre edildi.",
                "🏷️ İnteraktif Uygulama Rozetleri: Duraklatılan uygulamalar kart üzerinde doğrudan kaldırılabilir rozetler (chips) halinde gösterildi.",
                "📜 Sadeleştirilmiş Sürüm Notları: Güncelleme kontrol penceresinde tüm README yerine yalnızca ilgili sürümün yenilik özeti gösterilir.",
            ),
            changesEn = listOf(
                "📳 Haptic Vibration Fix: Resolved continuous vibration loop caused by hardware driver lockups on accidental edge touches.",
                "📷 Camera & Shooting Auto-Pause: Automatically pause gestures during active camera viewfinder or video recording.",
                "📞 Phone Call Auto-Pause: Disable gestures during incoming calls or active phone calls.",
                "⚪ Blacklist & Whitelist Mode: Integrated Blacklist (pause in selected) and Whitelist (run ONLY in selected) operating modes.",
                "🏷️ Interactive App Chips: Paused apps are displayed as removable chips directly inside the settings card.",
                "📜 Clean Release Notes: Update dialog extracts concise release highlights instead of rendering full README text.",
            )
        ),
        VersionHistoryItem(
            version = "1.3.0",
            date = "2026-08-06",
            isCurrent = false,
            changesTr = listOf(
                "↔️ Alt Kenar L-Çekme Hareketleri: Alt kenardan içeri çekip sağa veya sola dönerek L-Sağ ve L-Sol hareketleri eklendi.",
                "🖼️ Gerçek Duvar Kağıdı Haritası: İnteraktif telefon haritasında cihazın gerçek duvar kağıdı kullanılmaya başlandı.",
                "🎨 Renkli ve Bağımsız Ayarlar: Kenar anahtarları, görünüm kartları ve geri bildirim ayarları bağımsız renk kartlarına taşındı.",
                "💾 Tam JSON Yedekleme & Yükleme: Ayarlar, kurallar, profiller ve duraklatılan uygulamalar tek yedek dosyasında korundu.",
                "🔄 Güncelleme Kontrolü: GitHub Releases üzerinden manuel güncelleme ve APK indirme desteği sağlandı.",
            ),
            changesEn = listOf(
                "↔️ Bottom-edge L Gestures: Added L-right and L-left gestures by pulling inward and sliding right or left.",
                "🖼️ Real Wallpaper Map: Interactive phone map displays the device's actual wallpaper.",
                "🎨 Colorful Independent Settings: Edge toggles, appearance cards, and feedback controls work independently.",
                "💾 Complete JSON Backup & Restore: Settings, rules, app profiles, and paused apps preserved in one JSON backup.",
                "🔄 Update Check: Manual update check via GitHub Releases with direct in-app APK download.",
            )
        ),
        VersionHistoryItem(
            version = "1.2.0",
            date = "2026-08-04",
            isCurrent = false,
            changesTr = listOf(
                "📐 Sıfır Hayalet Boşluk: MainActivity üst başlık boşlukları kaldırıldı, durum çubuğu altına çekildi.",
                "📱 %55 Büyütülmüş Telefon Haritası: 360dp kart yüksekliği ve siberpunk OLED teması eklendi.",
                "📊 Canlı Kullanım Analizi & Kart Gizleme: Kenar sayaçları, grafik kartı ve ana sayfa kart göster/gizle anahtarları eklendi.",
            ),
            changesEn = listOf(
                "📐 Zero Header Waste: Removed redundant top padding in MainActivity layout.",
                "📱 55% Larger Phone Map: 360dp card height and cyberpunk OLED theme.",
                "📊 Live Usage Analytics & Card Visibility: Added edge usage counters, summary chart, and home card toggles.",
            )
        ),
        VersionHistoryItem(
            version = "1.1.0",
            date = "2026-08-01",
            isCurrent = false,
            changesTr = listOf(
                "⚡ Çek ve Tut Fiziği & L-Swipe Geliştirmeleri.",
                "🎨 Dinamik Uygulama Rengi (Adaptive App Color) Desteği.",
                "🌐 İlk Açık Kaynaklı Akış Gesture Sürümü.",
            ),
            changesEn = listOf(
                "⚡ Swipe and Hold Physics & L-Swipe Enhancements.",
                "🎨 Dynamic Adaptive App Color Support.",
                "🌐 Initial Open-Source Release of Akış Gesture.",
            )
        ),
    )
}
