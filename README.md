# Akış Gesture

[![Toplam İndirme](https://img.shields.io/github/downloads/omeryol/AkisGesture/total?color=blue&label=Toplam%20İndirme)](https://github.com/omeryol/AkisGesture/releases)
[![Son Sürüm](https://img.shields.io/github/v/release/omeryol/AkisGesture?color=green&label=Son%20Sürüm)](https://github.com/omeryol/AkisGesture/releases)
[![Telegram Grubu](https://img.shields.io/badge/Telegram-Destek%20Grubu-2CA5E0?logo=telegram)](https://t.me/+ZRMewoFvaIdhM2I0)
[![Telegram Kanalı](https://img.shields.io/badge/Telegram-Kanal-2CA5E0?logo=telegram)](https://t.me/+ZTbxUGG-ynowOWE0)

**Türkçe** | [English](README-en.md)

Akış Gesture, Android ve özellikle HyperOS cihazlarda sol, sağ ve alt kenar
hareketlerini özelleştiren açık kaynaklı bir erişilebilirlik uygulamasıdır.
Proje MIT lisanslı [OpenSwipe](https://github.com/ARCJ137442/OpenSwipe)
tabanından başlamıştır; kaynak atfı ve lisans bildirimleri korunur. Uygulamanın
paket adı `io.github.omeryol.akisgesture`dir.

> [!TIP]
> **Önemli Tavsiye:** Alt kenardan uygulama değiştirme, erişilebilirlik servisinin kendini onarma güvenilirliği ve kural/profil bütünlüğünde önemli düzeltmeler içerdiği için **v1.9.3** (en güncel sürüm) kullanmanız tavsiye edilir.

## v1.9.3 öne çıkanlar

- 🤏 **Alt kenardan yatay uygulama değiştirme düzeltildi:** Düz/yatay kaydırmalar artık doğru tanınıp uygulanıyor; önceden sessizce iptal oluyordu.
- 🛡️ **Erişilebilirlik servisi daha güvenilir:** Sağlık kontrolü artık kenar algılama pencerelerinin gerçekten ekrana eklenip eklenmediğini doğruluyor; otomatik onarım art arda başarısız olduğunda artık gerçekten yavaşlıyor.
- 🧩 **Kural ve profil bütünlüğü:** Uygulama profili kuralları artık alakasız bir ayar değişikliğiyle ezilmiyor; aynı bölgeye ikinci kural eklerken artık doğru şekilde güncelleniyor; kısayol/tuş etiketleri yeniden başlatma ve yedek geri yüklemede korunuyor.
- 🔁 **Otomasyon komutları daha sağlam:** Ardışık aç/kapat tetiklemeleri artık çakışmıyor.
- 🧹 **Bellek sızıntısı ve ANR riski giderildi:** Shizuku durum dinleyicisi düzgün temizleniyor; hızlı ayarlar kutucuğu artık ana iş parçacığını bloklamıyor.

İmzalı APK: [Akış Gesture v1.9.3](https://github.com/omeryol/AkisGesture/releases/download/v1.9.3/app-release.apk)
SHA-256: `06AF0D8F565AC557071EA6EEB90F5C5A9CE535420D4357A760B96CB07E0ADB00`

## v1.9.2 öne çıkanlar

- 🌀 **Menüler artık yay şeklinde:** Halka menüsü ve son uygulamalar düz bir sıra yerine yumuşak bir yay üzerinde dizilir. Ortadaki baloncuk en ileride durur, kenarlara doğru hafifçe geriler; görünüm daha doğal ve dengeli.
- 🩹 **Baloncuklar artık üst üste binmiyor:** Boyutu büyütüp aralığı kıssanız bile baloncuklar birbirine değmez. Aralarında her zaman en az 1 dp boşluk kalır ve aralıklar menü büyüdükçe kendini otomatik ayarlar.
- ⚖️ **Simetri ve kenar mesafesi:** Menü parmağınızın olduğu yere göre ortalanır ve ekran kenarına çarpmadan bir bütün olarak kayar; kenara çok yaklaşmaz.
- 🔐 **Otomasyon izni gerçekten kapatıyor:** İzin kapalıyken MacroDroid, Tasker gibi uygulamalar Akış Gesture'ı göremez ve çalıştıramaz. Açıkken komutlar sorunsuz çalışır.
- 🎨 **Yeni uygulama simgesi:** Üç kenar hareketini ortada buluşturan yeni simge; telefonunuz temalı simgeleri kullanıyorsa uygulama da ona uyum sağlar.

İmzalı APK: [Akış Gesture v1.9.2](https://github.com/omeryol/AkisGesture/releases/download/v1.9.2/app-release.apk)
SHA-256: `6C72BE7BD324FCA26D3C28C0E3855D2758B26BF6703475098D4C8F25FC655808`

## v1.9.1 öne çıkanlar

- 🎯 **Kenar Seçimi Hafızası:** Hareket listesinde kural eklerken veya düzenlerken seçili kenarın kaybolması engellendi; üzerinde çalışılan kenar (Sol/Alt/Sağ) korunur.
- 🛡️ **Sade ve Dinamik Koruma Sekmesi:** İzinler kartı sadeleştirildi, aktif izinler için modern durum anahtarları eklendi, eksik izinler belirgin uyarı kartlarıyla donatıldı.
- ⚡ **Kademeli Root Paneli:** Sistem onarımları "1. Kademe: Otomatik Onarım" ve "2. Kademe: Periyodik Nöbetçi" olarak ayrıldı; açıklamalar katlanabilir kılavuz notlarına taşındı.
- 🔒 **Cihaz İçi Güvenlik ve İzolasyon:** Cihazdaki diğer uygulamaların izinsiz müdahalesi ve yetkisiz intent tetiklemeleri engellendi; bileşenler, otomasyon izinleri ve veri koruma kuralları sıkılaştırıldı.
- 🚀 **Kararlılık ve Dayanıklılık:** Arka plan erişilebilirlik servisi donma takibi, çökme koruması ve sistem bazlı reaktif iyileşme mekanizmaları güçlendirildi.

İmzalı APK: [Akış Gesture v1.9.1](https://github.com/omeryol/AkisGesture/releases/download/v1.9.1/app-release.apk)
SHA-256: `01F73171350BD9CB40E13B99509B4430609429B5D3A13F4AAFA82B0688516151`

## v1.9.0 öne çıkanlar

- 🎨 **Organik Kenar Animasyonları:** Aurora Akışı korunarak 14 kenar animasyonu akışkanlar mekaniği ve doğal ışık geçirgenliğiyle baştan tasarlandı.
- 📊 **Canlı Ana Sayfa ve Akıllı Teşhis Paneli:** Canlı nabız göstergeli Hero durum kartı, dinamik kenar rozetleri ve HyperOS optimizasyon rehberli tek dokunuşlu teşhis paneli eklendi.
- 📐 **19.5:9 Modern Telefon Göstergeleri:** Hareket önizlemeleri modern telefon oranlarına güncellendi; vektörel neon tetik alanı göstergesi eklendi.
- 🎯 **Alt Kenar ve Kural Sıralaması:** Bölümler UUID yerine koordinat sırasıyla deterministik dizilir; Sol/Orta/Sağ konum etiketleriyle senkronize gösterilir.
- 🎨 **Kenar Temalı Kart Çerçeveleri:** Bölüm kartları ve ayar panelleri ait oldukları kenarın canlı tema renklerini kullanır.

İmzalı APK: [Akış Gesture v1.9.0](https://github.com/omeryol/AkisGesture/releases/download/v1.9.0/app-release.apk)
SHA-256: `379DF5B76B8555AADB09933CE7B06CE45A2A82DEFBF3AD0EDF7081F277F96706`

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
- Halka menüsü ve son uygulamalar şeridi doğal bir yay üzerinde, simetrik ve üst
  üste binmeyen yerleşimle çizilir; boyut büyütüldüğünde aralıklar otomatik uyum sağlar
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

Akış hizmetini başka uygulamalardan yönetmek için aşağıdaki intentler
kullanılabilir:

```text
io.github.omeryol.akisgesture.action.START
io.github.omeryol.akisgesture.action.STOP
io.github.omeryol.akisgesture.action.TOGGLE
```

Aynı adlar hem broadcast hem de activity olarak çözülür; otomasyon aracınızdaki
"Intent gönder" adımının hedef türü Broadcast veya Activity olabilir. En güvenilir
kullanım, paket adını (`io.github.omeryol.akisgesture`) açıkça belirtmektir:
Android, uygulama arka planda değilken örtük (implicit) broadcastleri düşürebilir.

MacroDroid ve Tasker, Akış Gesture'ı bir Locale/Tasker eklentisi olarak listeler
(eylem: Hareketleri başlat / Hareketleri durdur / Durumu değiştir). Eklenti
yeniden düzenlendiğinde mevcut seçim işaretli gelir.

Otomasyon uygulaması entegrasyonu, eklenti ve Hızlı Ayarlar kutucuğu aynı güvenli
denetim yolunu kullanır. Bu komutlar yalnızca Akış Gesture hizmetini etkiler.

Güvenlik anahtarı: Ayarlar ekranındaki **Otomasyon uygulamalarına izin ver**
anahtarı kapalıyken dışa açık tüm giriş noktaları (eklenti, broadcast receiver ve
Başlat/Durdur/Durum değiştir activity'leri) sistem düzeyinde devre dışı bırakılır.
Bu durumda otomasyon uygulamaları eklentiyi listesinde görmez ve intentler
çözülemez. Anahtar okunamazsa güvenli varsayılan "kapalı"dır.

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
