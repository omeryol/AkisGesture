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

Temel sol, sağ ve alt kenar hareketleri gerçek cihazda çalışmaktadır. Türkçe
arayüz, erişilebilirlik hizmeti, dokunsal geri bildirim ve parmağı izleyen
kenar animasyonu geliştirme sürümünde aktiftir. Uzun hareketler ve uygulama
profilleri tamamlanana kadar sürüm cihaz üzerinde denenerek geliştirilmektedir.

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

## Yol haritası

Ayrıntılı işlev eşliği hedefi: [FNG_PARITY_TARGET.md](FNG_PARITY_TARGET.md)

- [x] Temel projeyi temiz biçimde derle
- [x] Akış kimliği, adı ve görsel sistemini ayır
- [x] Akıcı kenar animasyonu ve dokunsal eşik geri bildirimi ekle
- [ ] Hareket motoru için birim testleri ekle
- [ ] Gerçek cihazda kenar gecikmesi ve yanlış tetikleme ölçümü yap
- [ ] Kısa, uzun ve bekletmeli hareketleri tek durum makinesinde birleştir
- [ ] Uygulamaya özel profilleri ekle
- [ ] HyperOS sağlık durumunu olay tabanlı izle
- [ ] Ayrı ve isteğe bağlı root/APatch yardımcısını geliştir
- [x] FNG kalıntılarını Swift Backup yedeğine dokunmadan temizle

## Lisans

Projenin OpenSwipe kaynaklı bölümleri MIT lisansı altındadır. Yeni kodların
lisans durumu değiştirilmedikçe aynı lisans uygulanır. Ayrıntılar `LICENSE`
dosyasındadır.
