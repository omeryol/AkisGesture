# Akış Gesture

Akış Gesture, Android ve özellikle HyperOS cihazlarda doğal kenar hareketleri
sunmak için geliştirilen kişisel, açık kaynaklı bir navigasyon uygulamasıdır.

Proje, MIT lisanslı
[OpenSwipe](https://github.com/ARCJ137442/OpenSwipe) tabanından başlamıştır.
OpenSwipe telif ve lisans bildirimi `LICENSE` dosyasında korunur.

## Hedef

- Sol, sağ ve alt kenarda gecikmesiz hareket algılama
- Kısa kaydırma, uzun kaydırma ve kaydırıp bekletme
- Geri, ana ekran, son uygulamalar ve kullanıcı eylemleri
- Uygulamaya ve ekran yönüne göre farklı profiller
- Kullanıcıyı teknik ayrıntılarla yormayan sade Türkçe arayüz
- HyperOS tarafından durdurulduğunda güvenli toparlanma
- Root/APatch desteğini ana uygulamadan ayrılmış yardımcı katmanda tutma

## Mimari ilkeler

1. Hareket motorunun tek bir doğruluk kaynağı vardır.
2. Root, normal Android yolları başarısız olduğunda kullanılan yardımcıdır.
3. Uygulama sistem uygulamasına dönüştürülmez.
4. Island ve diğer çalışma profilleri kendiliğinden hedeflenmez.
5. Sürekli süreç öldürme, görünür uygulama açma veya sık aralıklı sorgulama yapılmaz.
6. Her davranış değişikliği test ve CHANGELOG kaydıyla birlikte gelir.

## Mevcut durum

İlk aşama OpenSwipe tabanının korunması ve doğrulanmasıdır. Uygulama henüz
günlük kullanım için hazır değildir ve mevcut FNG kurulumu kaldırılmamalıdır.

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

## Yol haritası

- [ ] Temel projeyi temiz biçimde derle
- [ ] Akış kimliği, adı ve görsel sistemini ayır
- [ ] Hareket motoru için birim testleri ekle
- [ ] Gerçek cihazda kenar gecikmesi ve yanlış tetikleme ölçümü yap
- [ ] Kısa, uzun ve bekletmeli hareketleri tek durum makinesinde birleştir
- [ ] Uygulamaya özel profilleri ekle
- [ ] HyperOS sağlık durumunu olay tabanlı izle
- [ ] Ayrı ve isteğe bağlı root/APatch yardımcısını geliştir
- [ ] FNG ile yan yana kullanım ve güvenli geçiş testi yap

## Lisans

Projenin OpenSwipe kaynaklı bölümleri MIT lisansı altındadır. Yeni kodların
lisans durumu değiştirilmedikçe aynı lisans uygulanır. Ayrıntılar `LICENSE`
dosyasındadır.
