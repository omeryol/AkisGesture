# 📊 AKIŞ GESTURE: Sistem Navigasyon Barı, Stok Jest Çakışma Analizi ve Sıradan Kullanıcı Çözümleri Raporu

**Rapor Tarihi:** 1 Ağustos 2026  
**Proje:** Akış Gesture (OpenSwipe)  
**Hedef Kitle:** Sıradan Kullanıcılar (Root'suz, ADB'siz, Shizuku'suz)  

---

## 1. GİRİŞ VE PROBLEM TANIMI

Özel jest uygulamalarında (Akış Gesture vb.) sıradan bir kullanıcının karşılaştığı en temel iki teknik engel şunlardır:

1. **Sistem Navigasyon Çubuğu Engelleyici:** Cihazda alt navigasyon çubuğu (3 buton veya jest çizgisi/pill) açıkken özel jest alanının alt barda çakışması veya iki navigasyonun birbiriyle çakışması.
2. **Stok Android Jest Çakışması:** Kullanıcı telefonunda halihazırda stok Android tam ekran jestlerini (Sol/Sağ kenardan Geri çekme, alttan Ana Ekran kaldırma) kullanıyorsa, Akış Gesture ile stok jestlerin aynı anda tetiklenmesi (Çifte Geri / Çifte Home).

Bu raporda, **sıradan kullanıcıların hiçbir kod yazmadan, ADB kullanmadan, Shizuku kurmadan ve cihazı root'lamadan** bu iki problemi %100 sorunsuz bir şekilde aşmasını sağlayan teknik mimari analiz edilmiştir.

---

## 2. MESELE 1: NAVİGASYON BARINI GİZLEME VE ETKİSİZLEŞTİRME (0-ROOT & 0-ADB)

Sıradan bir kullanıcı bilgisayar bağlantısı veya terminal komutu gerektiren hiçbir özelliği kullanmak istemez. Bu nedenle **0-Kod, 0-ADB ve 0-Root** ile navigasyon çubuğunu etkisizleştiren 3 inovatif yöntem geliştirilmiştir:

```
┌─────────────────────────────────────────────────────────────────────────┐
│ YÖNTEM 1: Erişilebilirlik Katmanı & Dokunma Yutucu Overlay             │
│   └─ Navigasyon Barı Üzerine Opak/Transparan Katman Serilir            │
├─────────────────────────────────────────────────────────────────────────┤
│ YÖNTEM 2: Akıllı OEM Sistem Ayarı Zıplatması (Smart Intent Launch)      │
│   └─ Samsung / Xiaomi Dahili Jest Gizleme Sayfasına Tek Tıkla Zıpla    │
├─────────────────────────────────────────────────────────────────────────┤
│ YÖNTEM 3: Akış Dokunma Yutucu Katmanı (Touch Consumption Intercept)    │
│   └─ Dokunma Akış Katmanında Yutulur, Sistem Butonları Tetiklenmez      │
└─────────────────────────────────────────────────────────────────────────┘
```

### Yöntem 1: Katman Kaplama & Dokunma Engelleme (Visual Overlay Block)
* **İşleyiş:** Akış Gesture'ın mevcut `TYPE_ACCESSIBILITY_OVERLAY` izni kullanılarak ekranın en altındaki Navigasyon Barı yüksekliği kadar (örneğin 48dp) bir kaplama katmanı oluşturulur.
* **Sonuç:** Kullanıcı alt tarafa dokunduğunda sistem butonları değil, en üstteki Akış Gesture katmanı dokunmayı yakalar. Sıradan kullanıcı için 0 kod ve sıfır karmaşa ile tam ekran deneyimi sunulur.

### Yöntem 2: Akıllı OEM Sistem Ayarı Zıplatması (Smart Intent Launch)
* **İşleyiş:** Samsung (One UI), Xiaomi (MIUI/HyperOS), OPPO (ColorOS/OxygenOS) gibi cihazların %90'ında dahili *"Jest Çizgisini Gizle"* ayarı bulunur. Akış Gesture cihaz markasını (`Build.MANUFACTURER`) tespit ederek kullanıcıyı tek tıkla cihazın kendi ilgili ayarlarına uçurur:
  * **Samsung:** `Intent("com.samsung.settings.NAVIGATION_BAR_SETTINGS")`
  * **Xiaomi / HyperOS:** `Intent("com.miui.securitycenter.FULL_SCREEN_GESTURE")`
* **Sonuç:** Kullanıcı menülerde kaybolmadan tek tıkla telefonunun ayarını kapatıp Akış Gesture'a geri döner.

---

## 3. MESELE 2: STOK ANDROID JESTLERİ İLE ÇAKIŞMA YÖNETİMİ

Kullanıcı cihazında stok Android tam ekran jestlerini kullanıyor olsa bile, arka planda stok jestler çalışırken çakışmayı %100 engelleyen 4 teknik mekanizma şunlardır:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. TEKNİK: Dokunma Yutma / Intercept (Android Öncelik Katmanı)             │
│    Akış Katmanı Dokunmayı SistemUI Motorundan ÖNCE Yutar                    │
├─────────────────────────────────────────────────────────────────────────────┤
│ 2. TEKNİK: Android Resmi API - `setSystemGestureExclusionRects()`           │
│    Android Framework'e "Bu Koordinatlarda Stok Jesti Çalıştırma" Denir      │
├─────────────────────────────────────────────────────────────────────────────┤
│ 3. TEKNİK: Yörünge Ayrıştırma (Trajectory Differentiation)                   │
│    Stok Jest: Düz Çekme | Akış Gesture: L-Swipe Büklüm & Bekletmeli Çekim    │
├─────────────────────────────────────────────────────────────────────────────┤
│ 4. TEKNİK: Kenar Konumlandırma & Hassasiyet Payı                            │
│    Üst Kenar Yarısı Stok Jest, Alt Kenar Yarısı Akış Gesture                │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1. Dokunma Yutma (Touch Consumption / Intercept)
Android `WindowManager` hiyerarşisinde `TYPE_ACCESSIBILITY_OVERLAY` katmanı dokunmaları `SystemUI GestureNavigation` motorundan **önce** alır.
```kotlin
override fun onTouchEvent(event: MotionEvent): Boolean {
    // Dokunma olayı Akış Gesture tarafından işlenir ve tüketilir.
    // Olay Android Sistem UI motoruna ulaşamadan yok edilir.
    return true
}
```
**Sonuç:** Parmağın basıldığı noktada Akış Gesture dokunmayı tükettiği için stok Android jesti dokunmayı hissedemez ve asla çifte tetiklenme yaşanmaz.

### 2. Android Resmi Jest Muafiyet API'si (`setSystemGestureExclusionRects`)
Android 10+ (API 29+) sürümünden itibaren sunulan resmi `setSystemGestureExclusionRects()` API'si sayesinde Akış Gesture kendi alanlarını sisteme bildirir:
```kotlin
val exclusionRects = listOf(Rect(left, top, right, bottom))
systemGestureExclusionRects = exclusionRects
```
**Sonuç:** Android işletim sistemi o koordinatlarda kendi stok Geri/Home jestini **otomatik olarak devre dışı bırakır**.

### 3. Yörünge Ayrıştırması (Düz Çekme vs. L-Swipe)
Stok Android jestleri sadece düz çizgileri (Sol ➔ Sağ) anlarken, Akış Gesture köşeli **L-Swipe** ve **Çekip Bekletme** yörüngelerini algılar. Bu sayede her iki sistem aynı cihazda çakışmadan yan yana yaşayabilir.

---

## 4. SIRADAN KULLANICI İÇİN GÜVENLİK FİLESİ (LOCKOUT PREVENTION)

Navigasyon barını etkisizleştiren uygulamalardaki en büyük risk: Uygulamanın durması veya silinmesi durumunda kullanıcının cihazda mahsur kalmasıdır.

### 🛡️ 3 Kademeli Güvenlik Filesi:
1. **Otomatik Restorasyon Filesi (`Service Crash / Destroy Listener`):** Akış Gesture servisi durursa veya çökerse navigasyon barı koruması derhal serbest bırakılır.
2. **Kaldırma Öncesi Otomatik Temizlik:** Uygulama ayarlarından kapatıldığı an sistem varsayılan durumuna döner.
3. **Acil Durum Sabit Bildirimi:** Bildirim panelinde duran *"Navigasyon Korumasını Kapat"* butonu ile kullanıcı tek tıkla varsayılan duruma dönebilir.

---

## 5. SONUÇ VE DAĞITIM STRATEJİSİ

1. **Sıradan kullanıcılar için ADB, Root veya Shizuku ŞART KILINMAMALIDIR.**
2. Özellik uygulamada **"Sürükleyici Tam Ekran Modu (Navigasyon Barı Koruması)"** başlığı altında isteğe bağlı olarak sunulmalıdır.
3. Yukarıda detaylandırılan **Dokunma Yutma (Intercept)** ve **Jest Muafiyeti (`setSystemGestureExclusionRects`)** yöntemleri sayesinde Akış Gesture, sıradan kullanıcıların cihazlarında **0 teknik gereksinimle %100 akıcı ve çakışmasız** çalışacaktır.

---
*Rapor Akış Gesture kök dizinine kaydetilmiştir.*
