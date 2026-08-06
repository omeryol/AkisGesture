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
            version = "1.3.2",
            date = "2026-08-06",
            isCurrent = true,
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
