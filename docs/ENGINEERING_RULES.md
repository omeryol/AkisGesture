# Akış Gesture Geliştirme Kuralları

## Sınırlar

- `synccontac`, FNG ve diğer projelerin kaynaklarına dokunulmaz.
- FNG APK'sından çıkarılmış kod projeye alınmaz.
- Başka projeden alınan her kodun lisansı ve kaynağı kaydedilir.
- Uygulama APK'sı sistem veya ayrıcalıklı uygulama olarak kurulmaz.

## Hareket motoru

- Parmak hareketi olayları tek bir durum makinesinde işlenir.
- Eşikler ekran yoğunluğundan bağımsız birimlerle tanımlanır.
- Mesafe, hız ve bekleme birbirinden bağımsız ölçülür.
- Parmak küçültme/yaklaştırma gibi çoklu dokunma gelecekte eklenirse sürekli
  ölçek değeri kullanılır; adımlı sıçrama oluşturulmaz.
- Her hareket için iptal, yanlış yön ve ekran döndürme senaryosu test edilir.

## Dayanıklılık

- Süreç kimliği sağlık göstergesi değildir.
- Erişilebilirlik bağlantısı ve aktif kenar pencereleri ayrı ayrı doğrulanır.
- Kurtarma işlemleri olay tabanlı ve sınırlı tekrar sayılıdır.
- Uygulama arayüzü kurtarma amacıyla kendiliğinden öne getirilmez.
- Profil ve kullanıcı kimliği açıkça doğrulanmadan root işlemi yapılmaz.

## Kullanıcı deneyimi

- Teknik terimler ana ekranda gösterilmez.
- Durumlar “Hazır”, “İzin gerekiyor” ve “Tekrar bağlanıyor” gibi açık
  ifadelerle anlatılır.
- Varsayılan ayarlar güvenli ve hemen kullanılabilir olmalıdır.
- Gelişmiş seçenekler ana akışı kalabalıklaştırmamalıdır.

## Teslim kapıları

Bir sürüm hazır sayılmadan önce:

1. Temiz debug ve release derlemesi tamamlanır.
2. Birim testleri ve statik kontroller geçer.
3. POCO F5 Pro üzerinde yeniden başlatma, ekran kapatma/açma ve yatay ekran
   senaryoları denenir.
4. FNG ile yan yana kullanımda çakışma olmadığı doğrulanır.
5. Geri dönüş yolu ve yedek açıkça belgelenir.
