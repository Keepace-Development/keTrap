# 🛡️ keTrap - Gelişmiş Minecraft Tuzak ve Yenileme Eklentisi

`keTrap`, Minecraft sunucuları için sıfırdan, yüksek performans ve modülerlik odaklı geliştirilmiş profesyonel bir tuzak yönetim ve otomatik yenileme (`regeneration`) eklentisidir. Sunucu sahiplerine oyuncular için dinamik tuzak alanları kurma, bu alanları hassas koordinatlarla sınırlama, tetiklendiğinde veya süre aşımında blokları otomatik eski haline döndürme ve modern GUI sistemleri üzerinden tam kontrol sağlama imkanı tanır.

## 📋 İçindekiler

* [Özellikler ve Öne Çıkanlar](#-özellikler-ve-öne-çıkanlar)
* [Sistem Gereksinimleri](#-sistem-gereksinimleri)
* [Proje Mimarisi ve Paket Yapısı (Hangi Dosya Nerede?)](#-proje-mimarisi-ve-paket-yapısı-hangi-dosya-nerede)
* [Komutlar ve Yetkilendirme (Permissions)](#-komutlar-ve-yetkilendirme-permissions)
* [Yapılandırma Dosyaları (Config & Lang)](#-yapılandırma-dosyaları-config--lang)
* [Kurulum ve Derleme Rehberi](#-kurulum-ve-derleme-rehberi)
* [Sorun Giderme](#-sorun-giderme)
* [Lisans ve Kullanım Notu](#-lisans-ve-kullanım-notu)

---

## ✨ Özellikler ve Öne Çıkanlar

- **Modüler OOP Mimarisi:** Sorumlulukların sınıflar arasında net bir şekilde bölündüğü (`Single Responsibility Principle`), sürdürülebilir ve temiz kod tabanı.
- **Gelişmiş Alan Yönetimi (Selection):** Dünyalar üzerinde hassas köşe seçimleri yaparak özel tuzak sınırları belirleme.
- **Akıllı Otomatik Yenilenme (Regeneration Engine):** Tuzaklar tetiklendiğinde veya kırıldığında, özelleştirilebilir zamanlayıcılar (`BukkitTask`) eşliğinde orijinal blok yapısına arka planda geri döner.
- **Modern GUI Arayüzleri:** Komut ezberlemeye gerek kalmadan envanter menüleri üzerinden pratik tuzak yönetimi.
- **PlaceholderAPI Entegrasyonu:** Sunucu genelindeki diğer eklentilerle kusursuz veri alışverişi.
- **Düşük Performans Maliyeti:** Asenkron işlemler ve optimize edilmiş event dinleyicileri sayesinde sunucu TPS değerini sabit tutar.

---

## 💻 Sistem Gereksinimleri

Sunucunuzda aşağıdaki bileşenlerin bulunması gerekir:

1. **Paper 1.20+** veya uyumlu bir Spigot/Paper sunucu sürümü
2. **Java 17** veya üzeri
3. *(İsteğe bağlı)* **PlaceholderAPI** (İstatistikler ve değişkenler için)

---

## 📂 Proje Mimarisi ve Paket Yapısı (Hangi Dosya Nerede?)

Proje, `com.ketrap.ketrap` kök paketi altında mantıksal katmanlara ayrılmıştır. Aşağıda projedeki her bir dosyanın tam konumu ve ne işe yaradığı detaylıca açıklanmıştır:

```text
keTrap/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/com/ketrap/ketrap/
        │   ├── keTrap.java
        │   ├── commands/
        │   │   ├── TrapAdminCommand.java
        │   │   └── TrapCommand.java
        │   ├── data/
        │   │   └── TrapData.java
        │   ├── listeners/
        │   │   ├── MenuListener.java
        │   │   └── TrapListener.java
        │   ├── managers/
        │   │   ├── MenuManager.java
        │   │   ├── RegenerationManager.java
        │   │   ├── SelectionManager.java
        │   │   └── TrapManager.java
        │   └── utils/
        │       ├── Lang.java
        │       ├── MenuHolder.java
        │       └── PlaceholderHook.java
        └── resources/
            ├── config.yml
            └── plugin.yml
```

### 1. Ana Sınıf (Main)
* **`src/main/java/com/ketrap/ketrap/keTrap.java`**
  * Eklentinin yaşam döngüsü yöneticisidir (`JavaPlugin` sınıfını extend eder). Sunucu açıldığında (`onEnable`) yapılandırmaları yükler, yöneticileri başlatır, komutları ve event dinleyicilerini kaydeder. Kapanış anında (`onDisable`) aktif görevleri ve verileri güvenle sonlandırır.

### 2. Komut İşleyicileri (`com/ketrap/ketrap/commands/`)
* **`TrapCommand.java`**
  * Standart oyuncuların kullandığı `/trap` komutunu işler. Oyunculara bilgi mesajları iletir veya genel tuzak arayüzünü açar.
* **`TrapAdminCommand.java`**
  * Sunucu yetkililerine özel komutları (`/trapadmin create`, `/trapadmin delete`, `/trapadmin reload` vb.) yönetir, yetki kontrollerini yapar ve konsol girdilerini işler.

### 3. Veri Yönetimi (`com/ketrap/ketrap/data/`)
* **`TrapData.java`**
  * Bir tuzağın bütün özelliklerini (Benzersiz ismi, dünya adı, minimum/maksimum koordinat sınırları, yenilenme süresi ve blok verileri) nesne yönelimli olarak temsil eden veri modeli (Data Model) sınıfıdır.

### 4. Olay Dinleyicileri (`com/ketrap/ketrap/listeners/`)
* **`TrapListener.java`**
  * Oyuncuların tuzak bölgelerine giriş/çıkış hareketlerini, tuzak tetikleme alanlarındaki etkileşimlerini ve bölge koruma kurallarını dinler.
* **`MenuListener.java`**
  * GUI pencereleri açıkken oyuncuların envanter içinde yaptığı tıklama olaylarını yakalar, eşya çalınmasını veya envanterin bozulmasını engeller ve ilgili menü fonksiyonunu tetikler.

### 5. Yöneticiler / İş Mantığı (`com/ketrap/ketrap/managers/`)
* **`SelectionManager.java`**
  * Oyuncuların dünyalar üzerinde değnek veya komutlar yardımıyla belirledikleri başlangıç ve bitiş koordinatlarını geçici hafızada (`HashMap`) tutarak yeni tuzak alanlarının sınırlarını hesaplar.
* **`TrapManager.java`**
  * Sunucuda kayıtlı olan tüm tuzakların listesini yönetir. Tuzakların aktifliğe göre saklanması, disk üzerine kaydedilmesi ve çağrılması işlemlerini koordine eder.
* **`RegenerationManager.java`**
  * Tuzak bloklarının kırılması veya yok edilmesi durumunda devreye girer. Belirlenen saniye gecikmesiyle arka planda çalışarak tuzak alanını ilk kaydedildiği orijinal blok dizilimine geri döndüren mekanizmayı yönetir.
* **`MenuManager.java`**
  * Oyuncuların arayüzde gördüğü GUI pencerelerinin şablonlarını oluşturur. Menü içerisindeki öğelerin yerleşimini, ikonlarını ve tıklama aksesuarlarını dinamik olarak hazırlar.

### 6. Yardımcı Araçlar (`com/ketrap/ketrap/utils/`)
* **`Lang.java`**
  * Yapılandırma dosyalarından metinleri çeker ve Minecraft renk kodlarını (`&` karakterini `§` formatına dönüştürerek) oyunculara şık bir dille yansıtır.
* **`MenuHolder.java`**
  * Özel GUI envanterlerini standart oyuncu veya sandık envanterlerinden ayırt edebilmek için kullanılan `InventoryHolder` arayüzünün özel implementasyonudur.
* **`PlaceholderHook.java`**
  * PlaceholderAPI eklentisi ile entegrasyon kurarak tuzak istatistiklerinin harici pluginler veya scoreboard'lar üzerinde gösterilmesine olanak tanır.

### 7. Kaynak Dosyalar (`src/main/resources/`)
* **`plugin.yml`**
  * Spigot motorunun eklentiyi tanıması için zorunlu olan metadata dosyasıdır. Main sınıfını (`com.ketrap.ketrap.keTrap`), komut tanımlamalarını ve yetki ağacını barındırır.
* **`config.yml`**
  * Eklentinin varsayılan mesajlarını, yenilenme sürelerini, hata loglama seviyelerini ve genel ayarları içeren ana yapılandırma dosyasıdır.

---

## 📌 Komutlar ve Yetkilendirme (Permissions)

### Komut Listesi
- `/trap` - Ana tuzak arayüzünü veya bilgi ekranını açar.
- `/trapadmin create <isim>` - Seçilen bölgeyi yeni bir tuzak olarak kaydeder.
- `/trapadmin delete <isim>` - Belirtilen tuzak alanını veritabanından siler.
- `/trapadmin reload` - Eklentinin yapılandırma dosyalarını (`config.yml`, dil dosyaları vb.) yeniden yükler.

### İzin Matrisi
- `ketrap.use` *(Varsayılan: Herkes)* - Oyuncuların temel tuzak komutlarını ve özelliklerini kullanmasını sağlar.
- `ketrap.admin` *(Varsayılan: Yalnızca Operatörler - OP)* - Yönetici komutlarına (`/trapadmin`) tam erişim izni verir.

---

## ⚙️ Kurulum ve Derleme Rehberi

1. **Projeyi Derleme:**
   - Projeyi bir IDE (IntelliJ IDEA veya Eclipse) içine import edin veya terminalden kök dizine gelin.
   - Maven kullanarak projeyi şu komutla derleyin:
     ```text
     mvn clean package
     ```
   - Oluşan `target/keTrap-<versiyon>.jar` dosyasını alın.

2. **Sunucuya Aktarma:**
   - `.jar` dosyasını Minecraft sunucunuzun `plugins/` dizinine kopyalayın.

3. **Başlatma ve Yapılandırma:**
   - Sunucunuzu başlatın veya yeniden başlatın.
   - `plugins/keTrap/config.yml` dosyasını sunucu konseptinize uygun olarak düzenleyin ve `/trapadmin reload` komutuyla güncelleyin.

---

## 🔍 Sorun Giderme

### `/trap` veya `/trapadmin` komutları çalışmıyor
- Eklentinin `plugins` klasöründe olduğundan emin olun.
- Konsolda eklentinin başarıyla yüklenip yüklenmediğini kontrol edin.
- `/plugins` komutunu kullanarak `keTrap` eklentisinin yeşil renkte olduğunu doğrulayın.

### Tuzaklar yenilenmiyor
- `RegenerationManager` sınıfının aktif görevleri tetiklediğinden emin olmak için konsoldaki hata loglarını inceleyin.
- Dünya koruma eklentilerinin (WorldGuard vb.) eklentinin blok değiştirmesine engel olup olmadığını kontrol edin.

---

## 📜 Lisans ve Kullanım Notu

Bu proje **MIT Lisansı** ile korunmaktadır. Özgürce inceleyebilir, geliştirebilir ve kendi sunucularınızda kullanabilirsiniz. Sorunlar veya öneriler için GitHub üzerinden Issues sekmesini kullanabilirsiniz.
