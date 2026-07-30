# Değişiklik Günlüğü

Bu projedeki önemli değişiklikler bu dosyada tutulur.

## [Unreleased]

- HyperOS koruması artık yalnızca erişilebilirlik ayarındaki kaydı değil,
  hizmetin gerçekten bağlı olup olmadığını da kontrol ediyor. Ayar açık fakat
  bağlantı düşmüşse yalnızca Akış bileşeni olay sonrasında yeniden bağlanıyor.
- Ekran açma, kilit açma, cihaz açılışı ve uygulama güncellemesi kontrollerine
  kısa bağlanma bekleme süresi ve 30 saniyelik yeniden deneme sınırı eklendi;
  saniyelik sorgu veya sürekli kapat-aç döngüsü kullanılmıyor.
- Kullanıcı kutucuk, intent veya etkinlik üzerinden Akış'ı durdurduğunda ön plan
  koruma hizmeti de kapanıyor ve olay kontrolleri kullanıcı kararını geri almıyor.
- Eylem seçimine telefondaki başlatılabilir uygulamaları listeleyen ve arayan
  `Uygulama aç` bölümü eklendi. Uygulama adı doğrudan genel eylem aramasında da
  bulunabiliyor ve seçim yedeklere paket adıyla kaydediliyor.
- Seçilen uygulamalar eylem seçicisinde, kural tablosunda ve ayrıntı ekranında
  genel bir simge yerine kendi gerçek uygulama simgesiyle gösteriliyor.
- Artık kullanılmayan eski eylem modeli ve içindeki uygulanmamış klavye seçici
  yolu kaldırıldı; hareket motoru tek `ActionNode` yürütme hattını kullanıyor.
- Alan adlarındaki teknik `üçte bir` ifadeleri kaldırıldı. Dikey kenarlarda
  `Üst / Orta / Alt bölüm`, alt kenarda `Sol / Orta / Sağ bölüm` dili
  kullanılıyor; özel alanlar da anlaşılır yüzde aralığıyla gösteriliyor.
- Yeni kural oluşturma akışı üç adımdan iki adıma indirildi. Kenar ve alan aynı
  ekranda, hızlı ve bekletmeli hareketler ise tek özet ekranında ayarlanıyor.
- Yeni kural ekranındaki uzun açılır eylem listesi kaldırıldı. Mevcut kurallarla
  aynı aranabilir eylem seçici kullanılıyor; seçilen eylem tek dokunuşla
  değiştirilebiliyor veya kaldırılabiliyor.
- Hareket tablosunda boş `Hızlı` veya `Beklet` alanına dokununca ara pencere
  kaldırıldı; ilgili eylem artık doğrudan atanabiliyor.
- Eylem seçimine arama alanı ve sık kullanılan eylemler bölümü eklendi. Geri,
  ana ekran, son uygulamalar, uygulama geçişi ve temel paneller tek dokunuşla
  seçilebiliyor.
- Alt kenardaki uygulama geçişi için parmağı izleyen yön kapsülü eklendi.
  Sağ/sol yön oku hareket sırasında görünür; eşik tamamlandığında kapsül çerçeve
  ve dokunsal geri bildirimle hazır durumunu bildirir, işlem yine bırakınca çalışır.
- Yatay uygulama geçişini dikey alt-kenar hareketinden ayıran saf yön/eşik
  politikası ve birim testleri eklendi.
- Eylem listesinde bulunmasına rağmen çalışmayan `Fener` işlemi gerçek kamera
  fener denetimine bağlandı. Gerekli kamera izni yalnız kişisel profilde root
  üzerinden veriliyor; erişilebilirlik hizmeti izin verilmeden önce kameraya
  erişmeye çalışmıyor.
- Ana ekran; hizmet durumu, etkin hareketler, kullanılan kenarlar ve koruma
  bilgisini tek bakışta gösteren kompakt bir gösterge paneline dönüştürüldü.
- Yedek yükleme işlemine, mevcut ayarların değiştirileceğini açıkça gösteren
  son onay adımı eklendi.
- Ayarlar ekranında artık kullanılmayan eski geniş kart bileşenleri temizlendi.
- Ayarlar ekranına tüm hareket kurallarını, görünümü ve uygulama engellerini
  JSON dosyasına yedekleme ve geri yükleme eklendi.
- MacroDroid için başlat, durdur ve durum değiştir işlemleri hem anlaşılır
  uygulama etkinlikleri hem de Locale/Tasker uyumlu eklenti olarak eklendi.
- Erişilebilirlik hizmetinin kısa ve tam bileşen adlarının çift kayıt
  oluşturması önlendi.
- Alt kenarda sağa veya sola sürükleyerek açık uygulamalar arasında iki yönde
  geçiş eklendi. Önceki/sonraki uygulama işlemleri artık kararlı bir uygulama
  sırası üzerinden çalışıyor.
- Kenar alanlarının yan genişlik ve alt yükseklik ölçekleri artık 0 dp'den başlıyor.

### Düzeltildi

- HyperOS/Android 15 üzerinde erişilebilirlik hizmeti bağlanırken hareket
  katmanlarının bazen sistem pencere anahtarı hazır olmadan oluşturulması
  giderildi. Sol, sağ ve alt hareket alanları artık hizmet hazır olduktan sonra
  güvenli biçimde başlatılıyor.
- Akış Gesture açıkken `Geri` hareketi doğrudan uygulamanın kendi sayfa
  geçmişinde çalışacak şekilde düzeltildi; ana sayfadayken normal çıkış
  davranışı korunur.
- Çekip bekletme hareketi yalnızca o alana ikincil eylem atanmışsa hazırlanıyor.
  Eşik dolduğunda yalnızca hazır duruma geçiyor; eylem parmak bırakılınca bir
  kez çalışıyor. Parmak kenara geri götürülürse hareket tamamen iptal ediliyor.
- `Önceki uygulama` eylemi güvenilmez çift son-uygulamalar çağrısı yerine gerçek
  uygulama geçmişini kullanacak şekilde düzeltildi.

### Eklendi

- Görsel tasarım AI konsepti temel alınarak Hareketler ekranı `Alt / Sol / Sağ`
  sekmelerine ayrılan kompakt tablo düzenine dönüştürüldü. Alan, hızlı eylem ve
  bekletme eylemi aynı satırda görülüyor; silme ve etkinlik seçenekleri üç nokta
  menüsüne taşındı.
- Telefon haritası ana kural listesinden çıkarılarak gerektiğinde açılan ikincil
  `Harita` görünümüne taşındı.
- Ayarlar ekranındaki büyük açılır kartlar kaldırıldı. Ana görünüm kısa bölüm
  başlıkları ve ince ayırıcılı satırlardan oluşuyor; kaydırma çubukları, renk ve
  simge ayrıntıları odaklanmış ayar pencerelerinde açılıyor.
- Ayarlara FNG benzeri `Çalışmayacağı yerler` menüsü eklendi. Hareketler kilit
  ekranında, klavye açıkken, yatay ekranda ve kullanıcının seçtiği uygulamalarda
  ayrı ayrı duraklatılabiliyor; koşul bittiğinde kendiliğinden geri geliyor.
- Sistem durumuna göre duraklatmanın yalnızca seçilen koşullarda çalıştığını
  doğrulayan birim testi eklendi.
- Hızlı Ayarlar'a Akış hareketlerini açıp kapatan ve gerçek hizmet durumunu
  gösteren `Akış` kutucuğu eklendi.
- MacroDroid ve diğer otomasyonların kullanabilmesi için yalnızca Akış'ın kendi
  hizmetini yöneten `START`, `STOP` ve `TOGGLE` yayın intentleri eklendi.
- HyperOS erişilebilirlik kaydını beklenmedik biçimde düşürdüğünde, ekran
  açılışı veya kullanıcı kilit açma olayında root üzerinden yalnızca Akış
  bileşenini geri ekleyen koruma eklendi. Kullanıcı kutucuktan veya `STOP`
  intentiyle kapattığında otomatik onarım yapılmıyor.
- Hareket kuralı kartları yatay sıkışan düğmeler yerine alan başlığı ve her
  harekete ait ayrı, simgeli işlem satırlarıyla yeniden tasarlandı.
- Eylem seçimleri emoji metinleri yerine her işlemin anlamını gösteren gerçek
  vektör simgelerle yenilendi. Gezinme, sistem, panel, medya, donanım ve root
  eylemlerinin her biri kendi simgesini kullanıyor.
- Uzun eylem seçenekleri kategori kategori açılan listelere, kural oluşturma
  ekranındaki eylem seçimi ise tek satırlık açılır alana dönüştürüldü.
- Ayarlar ekranı `Hareket hissi`, `Görünüm`, `Uygulama davranışı` ve `Gelişmiş`
  başlıklarında toplanan açılır bölümlere dönüştürüldü.
- Hareket alanı haritası varsayılan olarak kompakt özet gösteriyor; düzenleme
  veya canlı deneme gerektiğinde tek dokunuşla açılıyor.
- Açık ve koyu temanın yüzeyleri, köşe yapısı ve vurgu renkleri daha modern,
  sakin ve tutarlı bir görsel sistemle yenilendi.
- Telefon haritasına, gerçek hareket motoruyla aynı mesafe, sönümleme ve bekleme
  değerlerini kullanan canlı `Dene` görünümü eklendi.
- Hareket motoru için eşik hesabını tek yerde tutan ortak model ve birim testleri
  eklendi.
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
- Renk seçicinin parlaklık alt sınırı gerçek siyah için yüzde sıfıra indirildi;
  siyah, beyaz ve varsayılan mavi kısa yolları eklendi.
- Ayarlar ekranındaki boşluklar ve kart içleri daha kompakt hale getirildi.
- Hızlı çekme ve çekip bekletme simgelerine `<`, `>`, `<<` ve `>>`
  seçenekleri eklendi.
- Kurallar ekranına gerçek bölge oranlarını gösteren, renkli kenarlarına
  dokunularak doğrudan düzenlenebilen telefon biçimli hareket haritası eklendi.
- Haritadaki bir alana dokunulduğunda hızlı çekme ve çekip bekletme eylemleri
  artık aynı pencerede atanabiliyor; tek tek sayfalar arasında dönmek gerekmiyor.
- Haritadaki alanlar parmakla taşınabiliyor; başlangıç veya bitiş ucundan
  sürüklendiğinde güvenli asgari uzunluk korunarak uzatılıp kısaltılabiliyor.
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
