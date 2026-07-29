# Değişiklik Günlüğü

Bu projedeki önemli değişiklikler bu dosyada tutulur.

## [Unreleased]

### Eklendi

- Klavye seçici, ses paneli, sistem asistanı ve sesi kapat/aç eylemleri eklendi.
- Aynı kenar alanının hızlı çekme ve çekip bekletme eylemleri artık tek ekranda
  birlikte atanabiliyor; ikinci eylem için sihirbazdan çıkıp geri dönmek gerekmiyor.
- Kural listesi aynı alana ait iki hareketi tek kartta gösteriyor. Liste ve ayrıntı
  ekranları aynı kayıt durumunu kullandığı için yeni kurala dokununca görülen
  geçici `Kural bulunamadı` hatası giderildi.
- Kullanıcının seçtiği uygulamalar öne geldiğinde hareketleri olay tabanlı olarak
  duraklatan uygulama listesi eklendi. Uygulamadan çıkınca hareket alanları
  kendiliğinden geri gelir; sürekli sorgulama yapılmaz.
- Sol, sağ ve alt kenar alanları hazır bölümlerin yanında yüzde tabanlı başlangıç
  ve bitiş sınırlarıyla ayarlanabilir hâle getirildi. En küçük alan yüzde 10'dur.
- Hareket animasyonu için tüm renk tonlarını kapsayan renk tonu, canlılık ve
  parlaklık seçicisi ile yüzde 10–100 saydamlık ayarı eklendi.
- Başlangıç, genel kullanım, tek el, ileri seviye, üretkenlik, medya ve açıkça
  işaretlenmiş root kullanımını kapsayan dokuz hazır hareket düzeni eklendi.
- Akış, baloncuk, damla, sade simge ve kapalı animasyon biçimleri eklendi.
- Hızlı çekme ile çekip bekletme için birbirinden bağımsız görsel simge seçimi
  eklendi; ikinci eşik hazır olduğunda görsel ikinci simgeye geçiyor.
- Eşiğe ulaşan hareket parmak yeniden kenara kadar geri götürüldüğünde iptal
  ediliyor; görsel başlangıç durumuna dönüyor ve parmak bırakıldığında eylem çalışmıyor.
- `Uygulama aç` eylemi artık tahmini ekran adı kullanmıyor; Android'in ilgili
  uygulama için bildirdiği gerçek açılış ekranını kullanıyor.
- Aynı tetikleme alanına ayrı `Hızlı çekme` ve `Çekip bekletme` eylemleri
  atanabilen iki aşamalı hareket modeli eklendi.
- Eski `SWIPE`, `SHORT_SWIPE` ve `LONG_SWIPE` kayıtları yeni modele kayıpsız
  taşınıyor.
- Çekip bekletme süresi Ayarlar ekranında `150–700 ms` arasında değiştirilebilir.
- Bekletme eşiği için sarı görsel durum, ayrı titreşim ve parmak titremesine
  dayanıklı geri dönüş toleransı eklendi.
- Root ile öndeki uygulamayı kişisel profilde zorla durdurma eylemi eklendi.
  Akış Gesture, Android sistemi, System UI, Ayarlar ve kullanılan launcher
  güvenlik nedeniyle korunur.
- Ayarlar ekranına APatch/root erişimini gerçek uygulama sürecinden sınayan
  `Root hazır` durum kartı eklendi.
- Hızlı çekme ve bekletme kurallarının aynı alanda çakışmadan derlendiğini
  doğrulayan birim testleri eklendi.
- FNG'nin hareket mantığı, eylemleri, root işlevleri, görsel seçenekleri ve
  sistem uyumluluğu incelenerek `FNG_PARITY_TARGET.md` hedef belgesi oluşturuldu.
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

- Uygulama profilleri, ayrıntılı alan geometrisi, alternatif animasyon biçimleri
  ve ayar yedekleme/geri yükleme henüz eklenmedi.
- Fener eylemi kamera izni gerektiren güvenli akış tamamlanana kadar devre dışıdır.

## [0.1.0-foundation] - 2026-07-30

### Eklendi

- MIT lisanslı OpenSwipe kaynak geçmişi başlangıç tabanı olarak alındı.
