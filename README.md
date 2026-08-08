# Akış Gesture

[![Toplam İndirme](https://img.shields.io/github/downloads/omeryol/AkisGesture/total?color=blue&label=Toplam%20İndirme)](https://github.com/omeryol/AkisGesture/releases)
[![Son Sürüm](https://img.shields.io/github/v/release/omeryol/AkisGesture?color=green&label=Son%20Sürüm)](https://github.com/omeryol/AkisGesture/releases)

**Türkçe** | [English](README-en.md)

Akış Gesture, Android ve özellikle HyperOS cihazlarda sol, sağ ve alt kenar
hareketlerini özelleştiren açık kaynaklı bir erişilebilirlik uygulamasıdır.
Proje MIT lisanslı [OpenSwipe](https://github.com/ARCJ137442/OpenSwipe)
tabanından başlamıştır; kaynak atfı ve lisans bildirimleri korunur. Uygulamanın
paket adı `io.github.omeryol.akisgesture`dir.

## v1.4.0 öne çıkanlar

- Ana sayfaya kenar hareketlerini görsel olarak düzenlemek için 3B telefon
  haritası eklendi.
- Sol ve sağ tetik alanları haritadan yeniden boyutlandırılabilir ve dikeyde
  taşınabilir; iki kenarın canlı ölçüleri birlikte gösterilir.
- Hareketler, eylem ekleme ve ayarlar ekranlarındaki çeviri/tutarlılık
  sorunları ile görsel geri bildirimler iyileştirildi.

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

MacroDroid entegrasyonu ve Hızlı Ayarlar kutucuğu aynı güvenli denetim yolunu
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
