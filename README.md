# Akış Gesture

[![Toplam İndirme](https://img.shields.io/github/downloads/omeryol/AkisGesture/total?color=blue&label=Toplam%20İndirme)](https://github.com/omeryol/AkisGesture/releases)
[![Son Sürüm](https://img.shields.io/github/v/release/omeryol/AkisGesture?color=green&label=Son%20Sürüm)](https://github.com/omeryol/AkisGesture/releases)

**Türkçe** | [English](README-en.md)

Akış Gesture, Android ve özellikle HyperOS cihazlarda sol, sağ ve alt kenar
hareketlerini özelleştiren açık kaynaklı bir erişilebilirlik uygulamasıdır.
Proje MIT lisanslı [OpenSwipe](https://github.com/ARCJ137442/OpenSwipe)
tabanından başlamıştır; kaynak atfı ve lisans bildirimleri korunur. Uygulamanın
paket adı `io.github.omeryol.akisgesture`dir.

## v1.8.1 öne çıkanlar

- 🌍 **8 Yeni Dil Desteği:** Rusça (`ru`), Lehçe (`pl`), Almanca (`de`), Fransızca (`fr`), İtalyanca (`it`), Vietnamca (`vi`), Geleneksel Çince (`zh-rTW`) ve Farsça (`fa`) eklendi.
- 🌐 **22 Dilli Yerel Deneyim:** Ayarlar menüsüne 22 dili içeren kaydırılabilir seçim diyaloğu entegre edildi; Android 13+ uygulama bazlı dil tercihleri güncellendi.
- 📱 **Tam Anahtar Paritesi:** 607 çeviri anahtarının tamamında ve Android sistem standartlarında %100 uyumluluk sağlandı.

İmzalı APK: [Akış Gesture v1.8.1](https://github.com/omeryol/AkisGesture/releases/download/v1.8.1/app-release.apk)
SHA-256: `33AA782A574E36298F2536643679026C582B89CFA522B74632723C34E45602B5`

## v1.8.0 öne çıkanlar

- 📱 **Son Kullanılan Uygulamalar Dock Menüsü:** Kenardan çekip bekleterek son kullanılan uygulamalar arasında hızlı geçiş yapabilme; Halka Menüsü ile karşılıklı akıllı kilitleme.
- 🎛️ **Tam Özelleştirilebilir Menü Ayarları:** Uygulama sayısı (2-6), kenar mesafesi, ikon boyutu, aralık ve yay kavisi için bağımsız kaydırıcılar.
- ⏱️ **Açılma Bekleme Süresi Ayarı:** Menünün açılma süresini milisaniye hassasiyetinde (150 ms – 1000 ms) ayarlayabilme.
- ⚡ **120 FPS Ultra Akıcı Animasyonlar & Sıfır Gecikme:** Sistem çağrıları ve ikon yüklemeleri önbelleğe alınarak sıfır takılma sağlandı; Android 14 BAL optimizasyonu ile anında başlatma.
- 🎯 **Kesin Parmak Üzeri Seçim:** Daraltılmış seçim alanı sayesinde yalnızca parmak doğrudan hedef halkanın üzerine geldiğinde seçim gerçekleşir.

İmzalı APK: [Akış Gesture v1.8.0](https://github.com/omeryol/AkisGesture/releases/download/v1.8.0/app-release.apk)

SHA-256: `DCA0D61A36700F73A7702ED5899138196276F51E3412031DD9120D2750ABF37C`

## v1.7.3 öne çıkanlar

- 🛡️ **Bekçi ve Erişilebilirlik Koruması:** 5 saniye ile 120 dakika arasında yapılandırılabilir bekçi, ekran açılışında anında sağlık kontrolü ve manuel durdurma sonrası otomatik yeniden etkinleştirme.
- ⚡ **Hızlı Ayarlar:** Kutucuk aktif/pasif durumu daha güvenilir güncellenir; gecikmeli doğrulama ve bekçiye anlık durum sinyali eklendi.
- 🔔 **Ön Plan Bildirimi:** Koruma sekmesine bildirim görünürlüğü seçeneği ve bildirim kapatıldığında gösterilen koruma uyarısı eklendi.
- 📚 **Dinamik Sürüm Geçmişi:** Hakkında ekranı GitHub Releases üzerinden güncellenir; önbellek ve APK içi yedek liste çevrimdışı kullanımı korur.

İmzalı APK: [Akış Gesture v1.7.3](https://github.com/omeryol/AkisGesture/releases/download/v1.7.3/app-release.apk)

SHA-256: `A140FD22051873EBC56AE9EC062FF7C519D54DBBBE315868A1B625A1BFD107B`

## v1.7.2 öne çıkanlar

- 🧭 **Dinamik Kenar Bölümleri:** Aynı kenardaki hareketler otomatik olarak çakışmadan bölümlere ayrılır.
- 📱 **Daha Anlaşılır Harita:** Bölüm etiketleri, tetik alanları ve atanan eylemler senkronize gösterilir.
- 🛡️ **Root ve Koruma:** Root bilgileri ayrı sekmede, izin durumları doğrudan işlem seçenekleriyle sunulur.
- 🌍 **Yeni Diller:** Amharca, Bengalce, İspanyolca, Japonca, Korece, Portekizce, Quechua ve Svahili.
- 🎨 **Arayüz İyileştirmeleri:** Kart, buton, ikon ve hareket ekleme yerleşimleri sadeleştirildi.

## v1.7.0 öne çıkanlar

- 🎨 **5 Yeni İkon Paket ve Renk Modları:** Fluent, Pixelart, Ionicons, Lucide, Radix paketleri ile Cyber Neon ve Accent renk seçenekleri.
- 🌊 **3D Fizik ve Sıvı Animasyonları:** Blinn-Phong ışıklandırmalı surface tension, su kabarcığı ve kor yataklı ateş fiziği overhaul'u.
- 📱 **Sadeleştirilmiş Arayüz:** Görünüm sekmesinde 3 genişletilebilir bölüm içeren tek kart ve cihaz geometrisine tam oturan kenar haritası.
- ⚙️ **Geri Bildirim Esnekliği:** Animasyon kapatma seçeneği (`FeedbackAnimation.NONE`) ve gelişmiş L-jest göstergeleri.

## v1.6.1 öne çıkanlar

- Genel kullanıcı deneyimi ve arayüz iyileştirmeleri.
- Kenar bazlı halka açma/kapatma ve yenilenen halka eylem kartları.
- Kara liste ve beyaz liste durum göstergelerinde iyileştirmeler.
- Ana ekran Akış durumu kartında daha net renkler ve kontroller.

## v1.6.0 öne çıkanlar

- Her kenar için üç eylemli halka menüsü ve simgeler.
- Halka boyutu, aralığı, uzaklığı ve yarım halka eğriliği canlı ayarlanabilir.
- Ayar sırasında cihaz ekranında gerçek halka overlay'i görünür.
- Ana sayfada izin yönlendirmesi ve tanılama akışı bulunur.

## v1.5.1 öne çıkanlar

- Ana sayfa, telefon haritası, Ayarlar ve Hareketler'deki kenar sırası artık
  tutarlı: Sol → Alt → Sağ.
- Otomasyon uygulamaları açık kullanıcı izni olmadan hizmeti yönetemez; izin
  verildiğinde yalnızca Başlat, Durdur ve Aç/Kapat komutları kullanılabilir.
- Root kapsamı netleştirildi: temel hareketler root istemez ve uygulama sistem
  gezinme çubuğunu yönetmez.
- Telegram grup ve kanal bağlantıları eklendi; uygulama ikonu yenilendi.

## Özellikler

- Hızlı çekme, çekip bekletme ve iki yönlü L-hareketi
- Uygulama, ekran yönü ve sistem durumuna göre duraklatma
- Ayarlanabilir kenar alanı, eşik ve hassasiyet
- 15 ayrı görsel geri bildirim stili, haptik ve isteğe bağlı ses
- Uygulama başlatma, gezinme, medya, sistem ve korumalı root eylemleri
- JSON ile kural ve ayar yedekleme/geri yükleme
- Türkçe ve İngilizce arayüz

## İzinler ve güvenlik

- **Erişilebilirlik hizmeti:** hareketleri algılar ve seçilen eylemleri çalıştırır.
- **Titreşim:** hareket geri bildirimi sağlar.
- **Kamera:** yalnızca fener eylemi için kullanılır; fotoğraf/video çekilmez.
- **Bildirim ve ön plan hizmeti:** hizmet durumunu görünür ve dayanıklı tutar.
- **Açılışta çalışma/pil optimizasyonu istisnası:** kullanıcı etkinleştirirse
  yeniden başlatma sonrasında hizmeti sürdürmeye yardımcı olur.

Sistem gezinmesini ayrıca kapatmak uygulamanın kapsamı dışındadır ve cihazı
geçici olarak kullanılamaz hale getirebilir. Böyle bir değişiklikten önce JSON
yedeği alın, geri dönüş yolunuzu doğrulayın ve önce tek bir hareketi test edin.

## Kurulum

Gereksinimler: JDK 21 ve Android SDK 35.

```powershell
.\gradlew.bat assembleDebug
adb install --user 0 -r app\build\outputs\apk\debug\app-debug.apk
```

İmzalı sürüm APK'ları yalnızca [GitHub Releases](https://github.com/omeryol/AkisGesture/releases)
bölümünde yayınlanır. Yayınlarda sürüm etiketi ve SHA-256 özeti bulunur.

## Otomasyon

Akış hizmetini başka uygulamalardan yönetmek için aşağıdaki broadcast intentleri
kullanılabilir:

```text
io.github.omeryol.akisgesture.action.START
io.github.omeryol.akisgesture.action.STOP
io.github.omeryol.akisgesture.action.TOGGLE
```

Otomasyon uygulaması entegrasyonu ve Hızlı Ayarlar kutucuğu aynı güvenli denetim yolunu
kullanır. Bu komutlar yalnızca Akış Gesture hizmetini etkiler.

## Durum ve yol haritası

Temel sol, sağ ve alt kenar hareketleri HyperOS/Android 15 üzerinde
doğrulanmıştır. Sıradaki odak alanları, gerçek cihazlarda gecikme/yanlış
tetikleme ölçümü ve daha kapsamlı uygulama profili akışlarıdır.

## Katkı ve lisans

Akış Gesture bağımsız bir hobi projesidir; cihaz uyumluluğu veya bireysel destek
garantisi verilmez. Tekrarlanabilir hata raporları ve katkılar memnuniyetle
karşılanır. Projenin OpenSwipe kaynaklı bölümleri ve Akış Gesture katkıları MIT
lisansı altındadır; ayrıntılar için [LICENSE](LICENSE) dosyasına bakın.

## Topluluk

- [Telegram Grubu](https://t.me/+ZRMewoFvaIdhM2I0) — destek, geri bildirim ve cihaz deneyimleri
- [Telegram Kanalı](https://t.me/+ZTbxUGG-ynowOWE0) — sürüm duyuruları ve geliştirme haberleri
