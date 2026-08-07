## 🇹🇷 Türkçe

### 🐛 Kural Ekleme Kritik Düzeltmesi
- **Yeni Kural Eklenemiyor Hatası Giderildi**: `AddRuleDialog` içinde eylem seçici (`ActionPicker`) açılırken Compose Navigation tetikleniyordu. Bu durum diyaloğun composable state'ini (`showAddDialog`, `quickAction` vs.) sıfırlıyordu ve hiçbir kural eklenemiyordu.
- **Inline ActionPicker**: Eylem seçici artık navigation üzerinden değil, aynı composable ağacı içinde tam ekran `Dialog` olarak açılıyor. Seçilen eylemler ve diyalog durumu korunuyor.
- **Birden Fazla Eylem Desteği**: Aynı kural için Quick Swipe + Hold + L-Yukarı + L-Aşağı eylemlerinin hepsi tek diyalog oturumunda atanabiliyor.

---

## 🇬🇧 English

### 🐛 Rule Creation Fix
- **Fixed Rule Creation Bug**: Fixed critical issue where opening ActionPicker triggered Compose Navigation, resetting `AddRuleDialog` state and preventing rule creation.
- **Inline ActionPicker**: Action picker now opens as a full-screen inline dialog without navigation, properly preserving all dialog state and selected actions.
- **Multi-Action Support**: Assign Quick Swipe, Hold, L-Up, and L-Down actions seamlessly in a single dialog session.

---

## 📦 İndirme / Downloads
- **İmzalı APK (Signed APK)**: `AkisGesture-v1.3.7.apk`
