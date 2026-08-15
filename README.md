# Akış Gesture

[![Toplam İndirme](https://img.shields.io/github/downloads/omeryol/AkisGesture/total?color=blue&label=Toplam%20İndirme)](https://github.com/omeryol/AkisGesture/releases)
[![Son Sürüm](https://img.shields.io/github/v/release/omeryol/AkisGesture?color=green&label=Son%20Sürüm)](https://github.com/omeryol/AkisGesture/releases)

**Türkçe** | [English](README-en.md)

Akış Gesture, Android ve özellikle HyperOS cihazlarda sol, sağ ve alt kenar
hareketlerini özelleştiren açık kaynaklı bir erişilebilirlik uygulamasıdır.
Proje MIT lisanslı [OpenSwipe](https://github.com/ARCJ137442/OpenSwipe)
tabanından başlamıştır; kaynak atfı ve lisans bildirimleri korunur. Uygulamanın
paket adı `io.github.omeryol.akisgesture`dir.

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
