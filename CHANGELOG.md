# Değişiklik Günlüğü

Bu projedeki önemli değişiklikler bu dosyada tutulur.

## [Unreleased]

### Eklendi

- Proje adı ve hedefleri Akış Gesture olarak belirlendi.
- Türkçe README ve ilk ürün yol haritası oluşturuldu.
- Sürdürülebilir geliştirme kuralları eklendi.
- Kaynak proje OpenSwipe, Git üzerinde `upstream` olarak ayrıldı.
- Kaynak projedeki makineye özel ve geçersiz Gradle proxy ayarı kaldırıldı.
- Proje ve kullanıcıya görünen uygulama adı Akış Gesture olarak değiştirildi.
- Eski manifest paket bildirimi kaldırılarak Android Gradle Plugin uyarısı giderildi.
- Uygulama kimliği kaynak projeden ayrılarak `com.omer.akisgesture` yapıldı.
- Erişilebilirlik bağlanırken ana iş parçacığını kilitleyen senkron DataStore
  okuması kaldırıldı; güvenli varsayılanlar anında, kayıtlı kurallar asenkron
  yükleniyor.
- Android 14 ve üzeri için hareket hizmetinin gerekli `specialUse` ön plan
  hizmeti izni ve açıklaması eklendi.
- Uygulamanın görünen metinleri sade Türkçe anlatımla yenilendi.
- Sol, sağ ve alt kenar hareketlerine parmağı izleyen akıcı dalga animasyonu eklendi.
- Hareket eşiği renk değişimi ve üç aşamalı dokunsal geri bildirimle görünür hâle getirildi.
- Parmak bırakıldığında görselin sertçe kaybolması yerine yumuşak geri çekilmesi sağlandı.
- Görsel geri bildirim dokunmayı engellemeyen ayrı bir erişilebilirlik katmanına bağlandı.
- Island iş profiline yanlışlıkla kurulum yapılmaması için cihaz kurulum komutu
  yalnızca kişisel kullanıcıyı (`--user 0`) hedefleyecek şekilde belgelendi.

### Bilinen durumlar

- Uzun kaydırma ve kaydırıp bekletme hareketleri henüz tamamlanmadı.
- Root/APatch yardımcısı henüz eklenmedi.
- Uygulama profilleri henüz eklenmedi.

## [0.1.0-foundation] - 2026-07-30

### Eklendi

- MIT lisanslı OpenSwipe kaynak geçmişi başlangıç tabanı olarak alındı.
