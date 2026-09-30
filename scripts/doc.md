Tüm dosyaları okudum, ProjectAnalyzer dahil. Bir bulguyu da çalıştırıp doğruladım (aşağıda 1. madde). Derleme yapmadım, geri kalanı kod okuyarak çıkardım. Etki büyüklüğüne göre sıraladım.

**Doğrulanmış hata**

**1. README'deki önerilen include glob'u tek modüllü projede hiç dosya bulmaz.**  
--include='**/src/main/java/**' filtresini Java'nın PathMatcher'ı ile denedim:



src/main/java/com/[A.java](http://A.java)      -> false   (proje kökünde src varsa)

mod/src/main/java/com/[A.java](http://A.java)  -> true

Java glob'unda **/ önek olarak en az bir / ister, gitignore gibi boş öneki kabul etmez. Aynı sorun --exclude için de geçerli. Tek modüllü projede "0 dosya" alırsın. Çözüm için accept içinde göreli yolun başına / ekleyerek eşle ("/" + relative), ya da her glob için** / önekini kaldırılmış bir ikinci matcher da derle.

**Ölçeklenebilirlik: SUMMARY modu belleği kurtarmıyor**

**2. --detail=summary yalnızca çıktıyı küçültüyor, bellek kullanımını değil.** parseAll tüm FileMetric ağacını (her metod, riskBreakdown, riskFactors, ancestorPath) bellekte biriktiriyor. stripDetail, buildSummary ve buildHotspots bittikten sonra çalışıyor. Milyonlarca metodda asıl darboğaz burası. Şöyle yapılabilir:

- Her worker dosyayı işleyince FileMetric'i (SUMMARY'de metodsuz haliyle) döndürsün.
- Summary sayaçları (LongAdder/toplayıcılar) ve top-N için sınırlı PriorityQueue worker içinde güncellensin.
- Böylece metod ağacı hiç birikmez.

**3. Metod başına şişen veri.** attachHierarchy her MethodMetric ve ClassMetric'i baştan kopyalıyor. ancestorPath ise her metod için dosya yolunu ve sınıf adını yeni string olarak yeniden üretiyor. JSON'a da her metod için bunlar yazılıyor. ancestorPath zaten file + class + signature'dan türetilebilir, saklamayıp Markdown/JSON yazarken üret. Kopyalamayı da önlemek için moduleRoot/ancestorPath alanlarını hiç eklemeyebilirsin.

**4. Çıktı tek String olarak üretiliyor.** gson.toJson(report) tüm JSON'u belleğe alıyor. StandaloneReportMarkdown.render da "Tüm metodlar" tablosunu tek StringBuilder'da topluyor. Çözüm: JsonWriter ile akış halinde yazmak. Markdown'da ise varsayılan olarak sadece MEDIUM+ metodları listelemek. ReadableReportMain de JSON'u readString + fromJson ile komple yüklüyor, büyük dosyada aynı sorun var.

**5. Exclude glob'ları sadece dosyada uygulanıyor.** PathGlobFilter.accept yalnızca visitFile içinde çağrılıyor. Yani hariç tuttuğun dizinin içi yine de baştan sona geziliyor. Dizinler için de preVisitDirectory'de eşleştirip SKIP_SUBTREE dön. Ayrıca her dosya için Path.of(relative) yeniden oluşturuluyor, gereksiz.

**6. Yığın (stack) boyutu.** StackOverflowError yakalıyorsun, güzel. Ama uzun a + b + c + ... zincirleri çok yaygın (özellikle generated SQL/string kodunda). Executors.newFixedThreadPool varsayılan stack ile çalışıyor. Özel ThreadFactory ile new Thread(null, r, name, 64L << 20) kullanırsan bu dosyalar başarısız olmak yerine parse edilir. OutOfMemoryError ise hiç yakalanmıyor, worker'ı sessizce öldürüp future.get()'te tüm taramayı düşürür.

**Metrik doğruluğu**

**7. record, compact constructor ve initializer'lar görünmüyor.** ComplexityVisitor'da RecordDeclaration, CompactConstructorDeclaration ve InitializerDeclaration için visit yok, varsayılan seviyen de JAVA_17. Sonuçları:

- Üst düzey bir record içindeki metodlarda typeStack.peek() == null olur, analyseCallable sessizce return eder, yani o metodlar raporda yok.
- static { ... } ve örnek başlatıcı bloklarındaki karmaşıklık hiçbir yerde sayılmıyor (methodStack boş, addDecisionPoint etkisiz).
- Alan başlatıcılarındaki lambda'lar da aynı şekilde.  
Modern kodda bu, "rapor temiz görünüyor" ama aslında eksik demektir.

**8. Anonim sınıf metodları ayırt edilemiyor.** Anonim sınıftaki metodlar dış sınıfın methods listesine kardeş olarak ekleniyor, ancak anonim olduğu belli olmuyor. Aynı imzalı iki run() aynı görünür. Tanımlayıcıya başlangıç satırı ekle (özellikle baseline karşılaştırması yapacaksan).

**9. Sınıf LOC'u iç sınıfları içeriyor ama WMC içermiyor.** buildClassMetric countCodeLines(start, end) ile iç sınıfların satırlarını dış sınıfa da yazıyor, WMC'de ise iç sınıf metodları yok. Sonuç, dış sınıfın risk skoru tutarsız şişer.

**10. Proje risk skoru büyük kod tabanında anlamsız kalır.** buildSummary metod riskini LOC ağırlıklı ortalıyor. 1M metodluk repoda %99'u basit ise yüzlerce KRİTİK metod olsa bile skor DÜŞÜK çıkar. Bunun yerine şunları ekle: KRİTİK+YÜKSEK metodların toplam LOC oranı, KLOC başına kritik metod sayısı veya p95/p99 skoru.

**11. Ağırlıklar neredeyse etkisiz.** Skor max(alt skorlar) + 0.15 × blend ve seviye yalnızca dominant'a göre belirleniyor. WEIGHT_* sadece o %15'lik kısmı etkiliyor, ama log ve README "CC=%40 ..." diyerek ağırlıklar belirleyiciymiş gibi sunuyor. Ya belgeyi düzelt ya da modeli sadeleştir. Ayrıca compoundBonus cap'ten önceki değeri raporluyor, finalScore ise cap'li, ikisi tutarsız görünebilir.

**12. Modül kökü yanlış tespit ediliyor.** MethodHierarchy.moduleRoot yolun ilk segmentini alıyor. Proje kökünden taradığında modül adı src çıkar (tek modül), services/foo/src/... gibi iç içe monorepoda ise services çıkar. Gezerken en yakın pom.xml/build.gradle bulunan dizini modül olarak kaydetmek doğru sonucu verir.

**Gizli varsayılanlar**

**13. IGNORED_DIRECTORIES sabit ve README ile uyuşmuyor.** README "target/build/.gradle atlanır" diyor, kodda out, bin, dist, tmp, vendor, generated, coverage, .mvn da var, kapatma yolu da yok. src/main/java/com/firma/build/ veya .../tmp/, .../bin/ diye bir paket varsa (çok yaygın) sessizce taranmaz ve raporda da görünmez. Öneri: sadece proje köküyle aynı seviyedeki derleme çıktısı dizinlerini atla, --no-default-excludes bayrağı ekle, atlanan dizin sayısını çıktıya yaz.

**14. Kullanıcı Java 21+ yazınca dosya "hata" olarak düşüyor.** record desenleri, switch desenleri vb. JAVA_17'de parse hatası verir. Kullanıcıyı bayrakla uğraştırmak yerine başarısız dosyayı bir üst seviyeyle yeniden dene (17 → 21), eski kodda (record/yield değişken adı) ise bir alt seviyeyle. LanguageLevel.valueOf bilinmeyen değerde ("No enum constant...") anlamsız hata veriyor; geçerli seviyeleri listele. Ayrıca sürümünde JAVA_25 enum'unun olup olmadığını README ile karşılaştır.

**Küçük ama işe yarar**

- **Çıkış kodu:** Dosyaların hepsi hata verse bile run 0 döner. --max-failure-ratio ve --fail-on-risk ile 2/3 gibi çıkış kodları ver, CI'da işe yarar.
- **Hata yönetimi:** Konsol `--- Tarama tanıları ---`, Markdown `## Tarama tanıları` + `docs/scan-error-management.md` (unexpected token senaryoları, karışık Java sürümleri).
- **Locale:** ComplexityVisitor'da Türkçe locale'e dikkat etmişsin (Locale.ROOT), ama ScanOptionsParser.parseDetail, LanguageLevelOption.parse ve ReadableReportMain (toLowerCase()/toUpperCase()) hâlâ varsayılan locale kullanıyor. Şu an değerlerde i olmadığı için patlamıyor, ama tutarlı olsun.
- **Tek kaynak:** Eşikler ve ağırlıklar RiskCalculator, AnalysisConsoleLogger (sabit yazı), buildRiskModel ve Markdown'da ayrı ayrı yazılı. Biri değişince diğerleri sessizce yalan söyler. Hepsini RiskCalculator sabitlerinden üret.
- **PmdXmlMarkdown:** DOM ile tüm XML'i yüklüyor (büyük repoda StAX daha uygun), shortenPath içinde "mock-modules/" sabit kalmış (gerçek projede uzun mutlak yol çıkar), beginline yoksa parseInt patlar.
- **errors sırası** worker'lara bağlı, dosyalar sıralanıyor ama hatalar sıralanmıyor. Rapor farkı almak için sırala.
- **codeLines.get(start, end+1)** her metod için yeni BitSet üretiyor. nextSetBit döngüsü daha ucuz.
- **--verbose** her metodu stderr'e basıyor; büyük repoda kullanılmaması gerektiğine dair uyarı ekle.
- **Test:** mock-modules klasörünü beklenen CC/nesting değerleriyle JUnit'e bağlarsan (özellikle record ve anonim sınıf için) 7–9. maddeler bir daha bozulmaz.

**Önerilen sıra**

1. Glob hatası (1) ve dizin atlama şeffaflığı (13)
2. record/initializer eksikliği (7): yanlış sonuç veriyor, bu yüzden bellek işlerinden önce
3. Worker içinde toplama + top-N heap + akışlı yazma (2, 3, 4)
4. Stack boyutu ve OOM yakalama (6)
5. Proje skoru ve modül kökü (10, 12)

İstersen 1, 7 ve 2. maddeler için doğrudan yama (diff) hazırlayabilirim.

,,,





1. **Bellek (asıl sorun).** Her metod için MethodMetric + RiskBreakdown + riskFactors + ancestorPath (4 string) nesnesi tutuluyor. Tahminen metod başına birkaç yüz bayt. Sonra bunların kopyası da çıkıyor (attachHierarchy). 1M metodda bu GB'ler demek. JVM'in varsayılan heap'i genelde RAM'in dörtte biri, yani OutOfMemoryError ihtimali yüksek. OOM de yakalanmadığı için tüm tarama, saatler sonra, sonuçsuz düşer.
2. **Çıktı üretimi.** gson.toJson tüm JSON'u tek string yapar. Pretty-print ile metod başına ~1-2 KB varsayarsan 1M metod 1-2 GB eder. Java string'i ~2 GB sınırına da çarpar. Markdown'daki "Tüm metodlar" tablosu da aynı şekilde büyür ve açılamayacak bir dosya olur.
3. **Hep ya da hiç.** Saatlerce süren tarama %90'da düşerse hiçbir şey kalmaz. Ara kayıt, cache, devam etme yok.
4. **Yanlış sonuç riski.** Glob hatası, gizli dizin atlama ve record/initializer eksikliği (önceki mesajdaki 1, 7, 13) büyük repoda "temiz görünen ama eksik" rapora yol açar.
5. **Sinyal kaybı.** Proje skoru LOC ağırlıklı ortalama olduğu için milyonlarca basit metod kritik olanları gömer. Rapor "DÜŞÜK" der.
6. **Gürültü.** Test kodu, generated kod ve DTO'lar hotspot listesini kirletir.

**Sadece optimize etmek yeter mi?**

Tek makinede **büyük ölçüde evet.** Şu üç değişiklikle bellek "metod sayısı" ile değil "dosya sayısı + top-N" ile orantılı olur:

- Worker içinde toplama: sayaçlar, dağılım, LOC ağırlıklı toplamlar ve sınırlı top-N heap. Metod ağacı biriktirme (önceki mesajdaki 2).
- Akışlı yazma (JsonWriter), Markdown'da sadece MEDIUM+ metodlar (madde 4).
- Detayı sadece MEDIUM ve üstü metodlar için sakla, LOW olanları sadece sayaca ekle. Metodların büyük çoğunluğu LOW olduğu için çıktı çok küçülür.

Ama optimizasyonun **kırılamayacağı tavan var:** parse CPU-bound, worker'lar zaten çekirdekleri kullanıyor. Süre tek makinede kısalmaz. Süre sorunu varsa ya çok makine ya artımlı tarama (sadece değişen dosyalar) gerekir.

**Nasıl böleriz?**

Üç kademeli plan:

**1. Modüle göre böl (ilk adım, en doğal).** pom.xml/build.gradle içeren ve src/main/java barındıran her dizin ayrı bir tarama olur. Her modül ayrı JVM'de, kendi heap'iyle çalışır; biri patlarsa diğerleri etkilenmez.



bash

find . -name pom.xml -o -name build.gradle -o -name build.gradle.kts \

  | xargs -n1 dirname | sort -u | while read m; do

  [ -d "$m/src/main/java" ] || continue

  name=$(echo "$m" | tr '/' '_')

  java -Xmx4g -XX:+UseParallelGC -jar target/java-code-analyzer.jar \

    --path="$m/src/main/java" --workers=4 --detail=summary \

    --output="out/$name.json" || echo "$m" >> out/failed-modules.txt

done

Bir eksik: --path verince göreli yollar modül içinden başlar, repo yolu kaybolur. --path-prefix=<modül> gibi bir seçenek ve modül adını rapora yazan bir alan ekle (önceki mesajdaki 12).

**2. Dev modülleri hash ile parçala.** Tek modül bile çok büyükse dosyaları yol hash'ine göre N parçaya böl: deterministik ve dengeli olur.



java

// ScanOptions'a --shard=i/N ekle

boolean inShard(String relativePath, int index, int total) {

    return Math.floorMod(relativePath.hashCode(), total) == index;

}

collectJavaFiles içinde filter.accept sonrası bu kontrolü uygula. Her shard tüm ağacı gezer (ucuz), sadece kendi dosyalarını parse eder.

**3. Sürekli kullanım için artımlı ol.** git diff --name-only <base> ile sadece değişen dosyaları tara, önceki sonuçlarla birleştir. İlk tam tarama bir kez yapılır, sonrası dakikalar sürer.

**Bölünmüş sonuçları birleştirmek**

Bugünkü AnalysisReport birleştirmeye uygun değil. projectRiskScore gibi oranlar toplanamaz. Her shard'ın çıktısına **ham toplayıcıları** yaz: weightedScoreSum, weightSum, metod/sınıf/LOC toplamları, CC toplamı ve max'ı, dağılım sayıları, top-N listesi, hatalar. Sonra ayrı bir merge komutu (yeni bir Main sınıfı) şunları yapar:

- Sayıları toplar, max'ları alır, oranları en sonda hesaplar.
- Top-N listelerini birleştirip tekrar sıralayıp kırpar.
- Hata listelerini birleştirir, hangi modül/shard'ın düştüğünü ayrıca raporlar.

Bunun için şu sözleşmeyi öneririm: her shard iki dosya üretsin, shard-*.summary.json (toplayıcılar + top-N) ve shard-*.files.jsonl (satır başına bir dosya, sadece MEDIUM+ metodlarla). merge bunları akışla okur ve tek Markdown'a dönüştürür, hiçbir aşamada her şey belleğe yüklenmez.

**Paralel çalıştırırken dikkat**

- shard sayısı × workers çekirdek sayısını aşmasın (aksi halde thread'ler birbirini bekler). Örn. 16 çekirdek: 4 shard × 4 worker.
- shard sayısı × -Xmx fiziksel RAM'i aşmasın.
- CI'da shard'ları matrix job olarak dağıtırsan süre makine sayısıyla düşer.

**Önerilen sıra**

1. Bir modülde ölçüm yap (-Xmx2g, --detail=summary, dosya/sn ve peak heap). Sonucu tüm repoya çarparak gerçekten neyin gerektiğini gör. Belki modül bölme bile yetiyordur.
2. Worker içinde toplama + akışlı çıktı + MEDIUM+ detay.
3. Modül döngüsü + --path-prefix + toplayıcı içeren shard çıktısı + merge.
4. Gerekirse --shard=i/N ve artımlı mod.

İstersen sıradaki adım olarak (2)'yi, yani worker içinde toplama, top-N heap ve akışlı JSON yazımını, mevcut sınıflarına uygun şekilde kod olarak yazayım.