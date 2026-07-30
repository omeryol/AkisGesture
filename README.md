# Akış Gesture

Akış Gesture, Android ve özellikle HyperOS cihazlarda doğal kenar hareketleri
sunmak için geliştirilen kişisel, açık kaynaklı bir navigasyon uygulamasıdır.

Proje, MIT lisanslı
[OpenSwipe](https://github.com/ARCJ137442/OpenSwipe) tabanından başlamıştır.
OpenSwipe telif ve lisans bildirimi `LICENSE` dosyasında korunur.

## Hedef

- Sol, sağ ve alt kenarda gecikmesiz hareket algılama
- Hızlı çekme ve çekip bekletme için bağımsız eylemler
- Geri, ana ekran, son uygulamalar ve kullanıcı eylemleri
- Uygulamaya ve ekran yönüne göre farklı profiller
- Kullanıcıyı teknik ayrıntılarla yormayan sade Türkçe arayüz
- HyperOS tarafından durdurulduğunda güvenli toparlanma
- Root/APatch desteğini ana uygulamadan ayrılmış yardımcı katmanda tutma

Root ile öndeki uygulamayı kapatma eylemi yalnızca kişisel profili hedefler ve
kritik sistem uygulamalarını korur.

## Mimari ilkeler

1. Hareket motorunun tek bir doğruluk kaynağı vardır.
2. Root, normal Android yolları başarısız olduğunda kullanılan yardımcıdır.
3. Uygulama sistem uygulamasına dönüştürülmez.
4. Island ve diğer çalışma profilleri kendiliğinden hedeflenmez.
5. Sürekli süreç öldürme, görünür uygulama açma veya sık aralıklı sorgulama yapılmaz.
6. Her davranış değişikliği test ve CHANGELOG kaydıyla birlikte gelir.

## Mevcut durum

Sol, sağ ve alt kenar hareketleri HyperOS/Android 15 cihazda çalışmaktadır.
Hızlı çekme ve çekip bekletme aynı alanda bağımsız eylemler çalıştırır; bekletme
varsayılan olarak 280 ms'de hazır olur ve eylem parmak bırakılınca devreye
girer. Parmak kenara geri götürülerek hareket iptal edilebilir. Akış Gesture
içindeyken geri hareketi uygulamanın kendi sayfalarında
gezinir. Telefon biçimli kural haritası alanları taşıma, boyutlandırma, birlikte
eylem atama ve hareketi canlı deneme olanağı sunar. Arayüzde uzun seçenek
yığınları yerine açılır bölümler ve listeler; eylemlerde ise işleve özel
vektör simgeler kullanılır. `Çalışmayacağı yerler` menüsünden kilit ekranı,
klavye, yatay ekran ve uygulamaya özel duraklatma koşulları seçilebilir.
Hareketler ekranında kenarlar sekmelerle ayrılır; her alanın hızlı ve bekletme
eylemi aynı kompakt satırda düzenlenir. Ayrıntılı görünüm ayarları ana sayfayı
kalabalıklaştırmadan ayrı pencerelerde açılır.
Eylem seçicisinden telefondaki başlatılabilir uygulamalar ada göre aranabilir;
atanan uygulama kendi simgesiyle gösterilir ve paket kimliği yedek dosyasında
korunur.

## Derleme

Gereksinimler:

- JDK 21
- Android SDK 35

Windows:

```powershell
.\gradlew.bat assembleDebug
```

APK:

`app/build/outputs/apk/debug/app-debug.apk`

Kişisel profile kurulum:

```powershell
adb install --user 0 -r app\build\outputs\apk\debug\app-debug.apk
```

Genel `adb install -r` kullanılmaz; aktif bir Island iş profili varsa uygulamayı
o profile de kaydedebilir.

## Otomasyon intentleri

Akış'ın kendi erişilebilirlik hizmetini başlatmak, durdurmak veya durumunu
değiştirmek için:

```text
com.omer.akisgesture.action.START
com.omer.akisgesture.action.STOP
com.omer.akisgesture.action.TOGGLE
```

Intentler yayın (`Broadcast`) olarak `com.omer.akisgesture` paketine gönderilir.
Bu komutlar diğer erişilebilirlik hizmetlerini değiştirmez. Root varsa Hızlı
Ayarlar'daki `Akış` kutucuğu da aynı güvenli denetleyiciyi kullanır.

MacroDroid içinde iki kolay yol vardır:

- `Eylemler > Uygulamalar > Eklenti > Akış Gesture` yolundan başlat, durdur
  veya durum değiştir seçilir.
- `Uygulamayı başlat > Etkinlik seç` bölümünde `Akış · Hareketleri başlat`,
  `Akış · Hareketleri durdur` ve `Akış · Durumu değiştir` ayrı görünür.

Ayarlar ekranındaki `YEDEK` bölümünden kurallar ve uygulama ayarları tek JSON
dosyasına kaydedilip geri yüklenebilir.

## Yol haritası

Ayrıntılı işlev eşliği hedefi: [FNG_PARITY_TARGET.md](FNG_PARITY_TARGET.md)

- [x] Temel projeyi temiz biçimde derle
- [x] Akış kimliği, adı ve görsel sistemini ayır
- [x] Akıcı kenar animasyonu ve dokunsal eşik geri bildirimi ekle
- [x] Hızlı çekme ve çekip bekletme kural motoru için birim testleri ekle
- [ ] Gerçek cihazda kenar gecikmesi ve yanlış tetikleme ölçümü yap
- [x] Hızlı çekme ve çekip bekletmeyi tek durum makinesinde birleştir
- [ ] Uygulamaya özel profilleri ekle (uygulamaya göre duraklatma tamamlandı)
- [x] Tüm kenarlarda ayarlanabilir alan konumu ve uzunluğu ekle
- [x] Animasyon rengi, saydamlığı ve kenara geri dönerek iptal davranışını ekle
- [x] Beş animasyon biçimi ve iki harekete ayrı simge seçimi ekle
- [x] Kuralları telefon üzerinde gösteren dokunulabilir hareket alanı haritası ekle
- [x] Harita üzerinde alan taşıma ve uçlarından boyutlandırma ekle
- [x] Haritada gerçek eşiklerle canlı hızlı çekme/bekletme denemesi ekle
- [x] HyperOS erişilebilirlik katmanı başlatma yarışını gider
- [x] Uygulama içi geri ve güvenilir önceki uygulama davranışını ekle
- [x] HyperOS sağlık durumunu olay tabanlı izle
- [x] Korunan paketleri gözeten ayrı root/APatch eylem katmanını ekle
- [x] FNG kalıntılarını Swift Backup yedeğine dokunmadan temizle

## Lisans

Projenin OpenSwipe kaynaklı bölümleri MIT lisansı altındadır. Yeni kodların
lisans durumu değiştirilmedikçe aynı lisans uygulanır. Ayrıntılar `LICENSE`
dosyasındadır.
