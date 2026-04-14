# Sohbet ve Dosya Paylaşım Uygulaması (Java Socket & Multithreading)

## Projenin Amacı
Bu projenin temel amacı, Java platformunda TCP/IP protokolü ve ağ (network) soket programlamasını kullanarak, birden fazla kullanıcının anlık olarak haberleşebileceği, özel veya genel mesaj gönderebileceği ve kendi aralarında dosya aktarımı yapabileceği, istemci-sunucu (client-server) tabanlı eşzamanlı bir sohbet yazılımı geliştirmektir.

## İstemci Sunucu (Client-Server) Mimarisi Açıklaması
Dağıtık uygulamaların temel tasarım deseni olan istemci-sunucu modelinde; veriyi ve ana yönetimi barındıran merkezi bir 'Sunucu (Server)' yazılımı ile bu veriden yararlanmak isteyen uç birim 'İstemci (Client)' yazılımları bulunur. Projemizde sunucu tek bir merkezde ağ bağlantılarını dinleyen uygulamadır. İstemciler ise kullanıcının sistemine kurulan arayüzlü uygulamalardır. Tüm mesaj ve aktarım trafiği, sunucu üzerinden organize edilerek yönlendirilmektedir.

## TCP Socket Nedir?
Transmission Control Protocol (TCP), veri paketlerinin sıralı, güvenilir ve kayıpsız bir şekilde ulaştırılmasını garanti eden bir ağ protokolüdür. Socket ise iki farklı ağ cihazı üzerinde çalışan yazılımların birbirleri ile bir iletişim tüneli oluşturabilmesini sağlayan bir uç noktasıdır (endpoint). Dosya ve anlık mesaj trafiğinde hiçbir verinin eksik gitmemesi adına, uygulamada bağlantı odaklı çalışan TCP soketleri kullanılmıştır.

## Multithreading Neden Kullanıldı?
Ağ üzerinden dinleme yapmak veya veri okumak genel bir uygulama işleyişini "bloke eder" (blocking I/O). Server, birinci istemci ile ilgilenirken ikinci bir istemcinin bağlantısını bekletmek ve tüm sistemi kilitlemek zorunda kalırdı. Aynı şekilde arayüzlü (GUI) bir uygulamada sunucudan mesaj gelmesini aynı mantıkla beklemek, kullanıcının butona tıklamasını engeller. Bu yüzden projedeki tüm asenkron işlemlerde Multithreading (çok iş parçacıklı yapı) kullanılmıştır. Bağlanan her istemci kendisine ait bağımsız bir `Thread` üzerinde yaşar ve bu sayede işlemler paralel gerçekleşir.

## Protokol Sistemi Nasıl Çalışıyor?
Projeye ait ağ haberleşmesi, tamamen istemci ile sunucu arasında karar verilmiş "|" (pipe) ayracı mantığına dayalı string komutlara ve binary okumalarına dayanır.
`DataInputStream` ve `DataOutputStream` kullanılarak önce bir string komut (UTF-8 formunda) gönderilir. Gelen komutun ilk kelimesi (örneğin "MESSAGE|", "FILE|") tetikleyici rol oynar. Komut geldiğinde parçalanır ve hedef işleme yönlendirilir.

## Server Tarafının Çalışma Mantığı
Sunucu (`ChatServer`), uygulamanın kalbi niteliğindedir. Belirlenen ağ portu üzerinden dinlemeye başlar. Sokete yeni bir istemci düştüğünde anında bir `ClientHandler` thread'i yaratılarak havuzdaki yerine alınır. Bağlı istemciler anlık bir iş parçacığı güvenli sözlükte (`ConcurrentHashMap`) adlarıyla tutulur. Sunucu her gelen paketi analiz eder ve hedefine, private (özel) ya da broadcast (genel) yönlendirir.

## Client Tarafının Çalışma Mantığı
İstemci yazılımı başlıca üç olay üzerinde durur: Arayüz akışları (`ChatGUI`), veri yollama sistemi (`ChatClient`) ve bir arka plan işçisi sayesinde sunucuyu sürekli dinleyen `ClientListener`. Kullanıcı arayüz üzerinden işlem yaptığında sokete bir paket atılır; aynı zamanda sunucudan bir veri gelirse Listener thread'i asenkron uyanır ve arayüze güncellemeleri yansıtır.

## GUI Bileşenlerinin Görevleri
Sistemin görsel mimarisi Java Swing üzerinden tasarlanmıştır. Pencerenin en üst bölümünde IP, Port, Kullanıcı Adı ve oturum açma komponentleri mevcuttur. Sol tarafta bir liste modeli bağlanan kullanıcıları saniyesinde yeniler. Orta panel tamamen mesaj geçmisi, alt bölüm ise ileti ve dosyaları sunucuya seçip postalama mekanizmaları üzerine kuruludur.

## Özel Mesaj Sistemi Nasıl Çalışıyor?
Uygulama arka planda @kullanici formatını dinlemektedir. Bir istemci gönderim esnasında mesajına `@Ayşe Merhaba` yazarak postalar. Sunucu içindeki `ClientHandler`, mesajı parçaladığı an cümlede @ sembolünün varlığını tespit ederse "Broadcast" işlemi yapmak yerine, kelimeyi ayırarak sistemde 'Ayşe' kullanıcısını arar. Doğrudan onun bağlantı referansına yazma yaparak araya özel (Private) bir tünel açar. Normal kullanıcıların sekmelerinde bu iletişim görülmez.

## Dosya Gönderme Mekanizması
Client sınıfına bağlı `FileTransferManager`, `JFileChooser` yoluyla lokal makinede seçilen disk dosyasını önce bir byte bytearray formatında RAM'e doldurur (Files.readAllBytes). Ardından `FILE|gonderen|dosyaAdi|baytBoyutu` formatında string komutu yollar. Hemen peşi sıra byte array ağa akıtılır. Sunucu tarafı önce header komutunu çözer, belirtilen miktar kadar veriyi `readFully` komutu ile karşılar. Diğer bilgisayarlara ise aynı yolla yollar. Onların ekranında beliren iletişim kutusuna "Evet" demeleri sonucu bu veriler disklerine yazılır.

## Online Kullanıcı Listesi Nasıl Güncelleniyor?
Her bağlanma (CONNECT) ve bağlantı bitirme veya Exception durumu (EXIT) anlarında Sunucu tetiklenir. Sunucu yeni oluşan aktif üyeler listesini aralarına virgül koyarak `USERLIST|ali,ayse` formatıyla her bir istemciye yayın (broadcast) olarak gönderir. O esnada açık olan client'ların arayüz tablosu eskisini siler ve bu listeyle saniye saniyesine güncellenir.

## Bağlantı Kesilince Sistem Ne Yapıyor?
Client kendi isteği ile çıkışa basarsa "EXIT|username" komutu yollanır. Bağlantı ani koparsa okuma bloğu `IOException` atar. Bu gerçekleştiği an `finally` blokları sayesinde soket anında düşürülür, açık I/O stream akışları kapanır, ve sunucuya bu kullanıcının gidişi loglanıp listeden ismi silinir. Anında diğer kişilere yeni bir USERLIST bildirilir.

### Sınıfların (Class) Görev Tanımları
- **ChatServer**: Uygulama sunucusudur. Port dinler, istemci listesini bellekte tutar ve ortak fonksiyonlar uygular.
- **ClientHandler**: Kendi döngüsünde varolan özel bir Thread'dir. Her istemci için tektir ve sadece onu temsil eder.
- **ChatClient**: İş platformu mantığını çalıştıran ana motorudur. IP ve port kullanarak bağ kurar ve bağlantı yönetir.
- **ClientListener**: Thread tabanlıdır. İstemci adına ağdan gelen bayt akışlarını veya mesajları bekleyen bekçidir.
- **ChatGUI**: İstemci tarafında projenin gözüken yüzü, Form'dur. Bütün komponent tepkilerini dinler.
- **FileTransferManager**: Dosya işlemlerinde kilitlenen GUI'den uzaklaştırılarak byte dönüştürme ve File System mantığını yöneten refactor edilmiş özel kısımdır.

### Program Nasıl Çalıştırılır?
1. **Server Nasıl Başlatılır:**
   Proje derlenip çalışırken sistemsel hiçbir I/O parametresine ihtiyaç yoktur. Çözüm dizinindeki `ChatServer` sınıfını çalıştırın (Run). Konsolda "Server baslatildi. Port: 5000" satırını görmeniz yeterlidir.
2. **Client Nasıl Bağlanır:**
   `ChatGUI` veya `ChatClient` üzerinden ana sınıfı çalıştırdığınız an önünüze bir arayüz gelir. Hedef IP kısmına `127.0.0.1` veya sunucunun dış IP adresini verin; bir kullanıcı adı doldurarak 'Bağlan' a basmanız yeterlidir. Daha fazla kullanıcı ve eşzamanlı test yapmak için IDE içerisinde (Allow multiple instances vs.) ya da komut penceresinden peş peşe `ChatGUI` uygulamasını defalarca çalıştırabilirsiniz.
