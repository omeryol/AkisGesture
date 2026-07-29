# Akış Gesture — FNG İşlev Eşliği Hedefi

## Ana hedef

Akış Gesture; FNG'nin kullanıcıya sağladığı hareket, eylem, özelleştirme ve
dayanıklılık işlevlerini özgün, test edilebilir ve güncel Android sürümleriyle
uyumlu bir mimariyle karşılayacaktır. FNG'nin özel kaynak kodu, görselleri veya
markası kopyalanmayacaktır.

## İnceleme sonucu

FNG'nin iki temel hareketi şunlardır:

1. **Hızlı çekme:** Parmağın kenardan yönlü biçimde çekilip bekleme süresi
   dolmadan bırakılması.
2. **Çekip bekletme:** Yeterli çekme mesafesine ulaşıldıktan sonra parmağın
   ayarlanabilir süre boyunca ekranda tutulması.

Bu ayrım yalnızca kısa ve uzun mesafe değildir. Süre, mesafe, yön doğruluğu ve
parmağın bırakılıp bırakılmaması birlikte değerlendirilir. Her tetikleyici
bölgesi iki ayrı eylem taşıyabilir. Uzun eylemin bırakıldığında veya eşik
geçildiği anda çalışması ayrıca seçilebilir.

FNG'de hassasiyet alt, sol ve sağ tetikleyici için ayrı tutulur. İncelenen
sürümde varsayılan hassasiyet seviyesi `2` olarak görünür; kamuya açık
kaynaklarda güvenilir bir sabit piksel karşılığı yoktur. Akış Gesture bu değeri
kopyalamak yerine ekran yoğunluğuna göre dp, süre ve hız ölçümleri kullanacaktır.

## Hareket motoru hedefleri

- Alt, sol ve sağ kenarda bağımsız tetikleyiciler
- Kenar başına birden fazla, istenirse eşit olmayan bölge
- Bölge konumu, uzunluğu, kalınlığı ve hassasiyeti
- Hızlı çekme ve çekip bekletme için ayrı eylemler
- Ayarlanabilir bekleme süresi
- Eylemi eşikte veya parmak bırakıldığında çalıştırma
- Yanlış yöndeki hareketi iptal etme
- Parmağın küçük geri hareketlerinde kararlı eşik davranışı
- Dikey ve yatay ekran için doğru kenar dönüşümü
- Tek elle erişimi kolaylaştıran yan tetikleyici konumu
- Klavye açıldığında yan alanları taşıma veya duraklatma
- Her uygulama için etkinleştirme, duraklatma ve farklı profil
- Kilit ekranı, tam ekran, yatay ekran, araç modu, uygulama yükleyicisi ve
  izin ekranlarında ayrı davranış

Başlangıç kalibrasyonu:

- Hızlı çekme mesafesi: `24–36 dp` aralığında cihaz testi
- Çekip bekletme süresi: başlangıçta `280 ms`, kullanıcı aralığı `150–700 ms`
- Yön sapması: başlangıçta en fazla `35°`
- Geri dönüş toleransı: eşik geçildikten sonra küçük parmak titremelerinde
  durumun hemen düşmemesi için histerezis

Bu sayılar FNG'den kopyalanmış değerler değil, gerçek cihaz ölçümüyle
ayarlanacak Akış başlangıç değerleridir.

## Eylem eşliği

### Gezinme ve sistem

- Geri
- Ana ekran
- Son uygulamalar
- Önceki uygulama
- Sonraki uygulama
- Son uygulamalara çift dokunma davranışı
- Bölünmüş ekran
- Güç menüsü
- Bildirimler
- Hızlı ayarlar
- Menü
- Klavye seçici
- Ses paneli
- Ekranı kilitle
- Ekran görüntüsü
- Gezinme çubuğunu göster/gizle

### Uygulama ve arama

- Uygulama aç
- Uygulama kısayolu aç
- Google arama katmanı
- Sesli arama
- Sistem asistanı
- Sesli asistan
- Belirli tuş kodunu gönder

### Döndürme ve üretici özellikleri

- Otomatik döndürmeyi aç/kapat
- Geçerli uygulamanın yönünü değiştir
- Zorlanmış döndürme
- Desteklenen Xiaomi/MIUI/HyperOS cihazlarda tek el modu

### Root eylemleri

- Öndeki uygulamayı zorla durdur
- Kullanıcının seçtiği uygulamayı zorla durdur
- Root gerektiren ekran kilidi/ekran görüntüsü için güvenli yedek yol
- Uyumlu sistemlerde gezinme çubuğu yönetimi

Korunan paketler zorla durdurma hedefi olamaz:

- Akış Gesture
- Android System ve System UI
- Kullanılan ana ekran uygulaması
- Telefon, Ayarlar ve paket yükleyici
- Kullanıcının ayrıca korumaya aldığı uygulamalar

Root komutları yalnızca kişisel kullanıcı (`user 0`) kapsamında çalışır.
Island veya başka çalışma profilleri açıkça seçilmedikçe hedeflenmez.

## Görsel ve dokunsal geri bildirim

- Alt ve yan kenarlar için ayrı animasyon seçimi
- Akış, baloncuk, damla, sade simge ve kapalı görsel modları
- Renk, saydamlık ve görsel boyut ayarı
- Parmağı gecikmesiz izleme
- Hızlı çekme hazır olduğunda birinci durum
- Bekletmeli eylem hazır olduğunda ikinci durum ve eylem simgesi
- Başlangıç, eşik ve onay için ayrı dokunsal geri bildirim
- Sistem dokunsal geri bildirimi veya özel titreşim seçimi
- Ekran kaydında/görüntüsünde geri bildirimi gizleme seçeneği

## Sistem uyumu ve dayanıklılık

- Erişilebilirlik hizmeti yeniden bağlandığında tam ve tekil başlangıç
- Yeniden başlatmadan sonra kuralların ve animasyonun geri yüklenmesi
- Yinelenen pencere oluşturmama
- Ekran açılışında uygulama arayüzünü öne getirmeme
- Olay tabanlı sağlık kontrolü; saniyelik sürekli sorgulama yapmama
- HyperOS pil ve otomatik başlatma durumunu anlaşılır biçimde gösterme
- Klavye, kilit ekranı, ekran döndürme ve diğer katmanlarla çakışmama
- Ayarları dışa aktarma ve geri yükleme
- Koyu/açık tema
- Uygulama simgesini gizleme seçeneği

## Uygulama sırası

1. Hızlı çekme / çekip bekletme durum makinesi
2. İki eylemli bölge düzenleyicisi ve gerçek cihaz kalibrasyonu
3. Root köprüsü ve güvenli zorla durdurma
4. Eksik sistem eylemleri
5. Bölge geometrisi ve uygulama profilleri
6. Animasyon seçenekleri
7. Klavye, kilit ekranı, yatay/tam ekran uyumluluğu
8. Yedekleme, geri yükleme ve dayanıklılık kapıları

## Tamamlanma ölçütü

Bir özellik yalnızca arayüzde görünmesiyle tamamlanmış sayılmaz. Her özellik:

- birim veya durum makinesi testi,
- gerçek POCO F5 Pro / HyperOS denemesi,
- kişisel profil ve Island ayrımı kontrolü,
- yeniden başlatma ve ekran aç/kapat testi,
- README ve CHANGELOG kaydı

ile doğrulanacaktır.
