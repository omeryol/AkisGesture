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

### Bilinen durumlar

- İlk temiz derleme henüz doğrulanmadı.
- Uygulama kimliği ve ekrandaki OpenSwipe markası henüz değiştirilmedi.
- Root/APatch yardımcısı henüz eklenmedi.
- Mevcut FNG kurulumu için otomatik kaldırma veya veri taşıma yapılmıyor.

## [0.1.0-foundation] - 2026-07-30

### Eklendi

- MIT lisanslı OpenSwipe kaynak geçmişi başlangıç tabanı olarak alındı.
