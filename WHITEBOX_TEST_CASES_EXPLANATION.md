# Penjelasan White Box Test Cases — Aplikasi Mobile BPKPAD Balangan

Dokumen ini menjelaskan **makna, cakupan, dan tujuan setiap test case** pada file Excel `WhiteBox_Testing_Mobile_BPKPAD_Balangan.xlsx`.

Dokumen ini dibuat sebagai konteks bagi AI agent agar agent tidak hanya menjalankan command Gradle, tetapi juga memahami **mengapa setiap test dilakukan**, **kode apa yang diuji**, dan **hasil seperti apa yang dianggap benar**.

---

## 1. Konteks Aplikasi

Aplikasi yang diuji adalah aplikasi mobile Android Kotlin untuk Sistem Indeks dan Pencarian Cepat Arsip Keuangan BPKPAD Balangan. Berdasarkan rancangan PKL, aplikasi mencakup fitur autentikasi, input arsip manual, OCR, impor Excel, pencarian metadata, pemetaan lokasi fisik, rekap/dashboard, dan sinkronisasi data.

Arsitektur aplikasi menggunakan MVVM dan Clean Architecture. Logika utama dipisahkan ke beberapa lapisan:

| Lapisan | Contoh Komponen | Fungsi |
|---|---|---|
| Presentation | `LoginViewModel`, `RapidInputViewModel`, `ArchiveListViewModel`, `ArchiveDetailViewModel` | Mengelola UI state, event, validasi ringan, dan pemanggilan use case. |
| Domain | `SaveArchiveUseCase`, `DeleteArchiveUseCase`, `ExportArchivesUseCase`, `ParseMetadataWithAiUseCase` | Menampung aturan bisnis aplikasi. |
| Data | `ArchiveRepositoryImpl`, `ExcelServiceImpl`, `ArchiveDao`, `StagingArchiveDao` | Menangani database lokal, remote service, dan file processing. |
| Utility | `DateVisualTransformation`, `DateUtils`, `CurrencyVisualTransformation` | Transformasi input dan helper teknis. |

Karena itu, white box testing difokuskan pada ViewModel, UseCase, Repository, DAO, Service, dan Utility.

---

## 2. Kategori Test dalam Excel

| Jenis | Contoh | Keterangan |
|---|---|---|
| Pure White Box | `LoginViewModelTest.kt`, `RapidInputViewModelTest.kt`, `ArchiveDaoSearchTest.kt`, `ExcelServiceImplTest.kt` | Menguji langsung kode internal, branch, state, query, atau service. |
| Grey Box | `LoginScreenTest.kt`, `RapidInputScreenTest.kt`, `StagingBoxListScreenTest.kt` | Menguji UI Compose tetapi memakai struktur internal screen/test node. |
| Candidate White Box | `AnalyticsViewModel.kt`, `OcrRepositoryImpl.kt`, `ArchiveRepositoryImpl.kt` | Test case dirancang di Excel, tetapi test otomatis mungkin belum dibuat. |

Agent harus memahami bahwa test kandidat tidak boleh dianggap lulus sampai file test dan assertion aktual tersedia.

---

## 3. Ringkasan Jumlah Test Case

| Fitur | Jumlah | Catatan |
|---|---:|---|
| Autentikasi | 6 | Sudah ada di repo |
| Input Manual | 15 | Sudah ada di repo |
| Staging Box | 2 | Sudah ada di repo |
| Pencarian & Filter | 6 | Sudah ada di repo |
| Manajemen Arsip | 2 | Sudah ada di repo |
| Impor & Ekspor Excel | 2 | Sudah ada di repo |
| Format Tanggal | 4 | Sudah ada di repo |
| Dashboard & Rekap | 2 | Kandidat/perlu dibuat |
| OCR & AI Parsing | 2 | Kandidat/perlu dibuat |
| Repository & Sinkronisasi | 4 | Kandidat/perlu dibuat |

---

## 4. Penjelasan Per Kelompok Test Case

### 4.1. Autentikasi

**Nomor test case:** 1, 2, 3, 4, 5, 6  
**Tujuan kelompok:** Kelompok ini memastikan jalur login diuji dari sisi ViewModel dan UI Compose. Fokusnya adalah kredensial valid, kredensial salah, input kosong, state loading, event input, dan tampilan error.

|No|Nama File Test|Nama Tes|Metode dan Endpoint|Role|Tujuan|Input|Ekspektasi Output|Status Implementasi|
|---|---|---|---|---|---|---|---|---|
|1|LoginViewModelTest.kt|LGN_001 - login successful with valid credentials|Unit Test / LoginViewModel.authenticateAdmin()|All Role|Menguji jalur sukses autentikasi saat email dan password valid.|email=valid@example.com; password=password; Supabase auth.signInWith returns Unit|isLoginSuccessful=true dan errorMessage=null.|Sudah ada di repo|
|2|LoginViewModelTest.kt|LGN_003 - login failure with invalid credentials|Unit Test / LoginViewModel.authenticateAdmin()|All Role|Menguji percabangan gagal ketika Supabase Auth melempar exception kredensial tidak valid.|email=wrong@example.com; password=wrong; auth.signInWith throws Invalid credentials|isLoginSuccessful=false dan errorMessage berisi pesan gagal masuk.|Sudah ada di repo|
|3|LoginViewModelTest.kt|LGN_006 - login fails when fields are empty|Unit Test / validasi form login|All Role|Menguji jalur validasi sebelum request auth ketika email dan password kosong.|email=''; password=''|Request auth tidak dieksekusi dan errorMessage='Email dan password tidak boleh kosong.'|Sudah ada di repo|
|4|LoginScreenTest.kt|testLoginButtonShowsLoadingIndicator|Compose UI Test / LoginScreen|All Role|Menguji state UI loading pada tombol login.|LoginUiState(isLoading=true)|Teks tombol 'Log In' tidak tampil karena diganti indikator loading.|Sudah ada di repo|
|5|LoginScreenTest.kt|testEnteringCredentials|Compose UI Test / LoginScreen input events|All Role|Menguji alur input email, password, dan klik tombol login pada layar Compose.|Input email=test@example.com; password=password123; klik Log In|TextField menerima input dan event login dapat dipicu tanpa crash.|Sudah ada di repo|
|6|LoginScreenTest.kt|testErrorMessageDisplay|Compose UI Test / LoginScreen error dialog|All Role|Menguji tampilan pesan error login pada state gagal.|LoginUiState(errorMessage='Email atau password yang Anda masukkan salah.')|Dialog/teks 'Login Gagal' dan pesan error tampil di layar.|Sudah ada di repo|

### 4.2. Input Manual

**Nomor test case:** 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21  
**Tujuan kelompok:** Kelompok ini adalah inti white box testing karena memeriksa branch terbesar pada RapidInputViewModel dan RapidInputScreen: validasi field wajib, nominal, duplikasi, auto-bundle, editing staging, copy type, copy count, dan validasi tahun.

|No|Nama File Test|Nama Tes|Metode dan Endpoint|Role|Tujuan|Input|Ekspektasi Output|Status Implementasi|
|---|---|---|---|---|---|---|---|---|
|7|RapidInputViewModelTest.kt|INP_002 - test validation fails when fields are empty|Unit Test / RapidInputViewModel.OnAddToBoxClick|Arsiparis|Menguji validasi field wajib pada form input arsip setelah konteks box dikonfirmasi.|warehouse=Gedung A; rack=Rak 1; box=Box 1; year=2026; docNumber kosong; subject kosong|validationErrors['docNumber']='Nomor dokumen wajib diisi' dan validationErrors['subject']='Uraian dokumen wajib diisi'.|Sudah ada di repo|
|8|RapidInputViewModelTest.kt|INP_003 - test nominal validation|Unit Test / validasi nominal RapidInputViewModel|Arsiparis|Menguji jalur batas nominal ketika nilai nominal negatif diproses oleh ViewModel.|nominal=-1; field wajib lain valid|Sistem tidak boleh menyimpan nominal tidak valid; jika implementasi belum menolak, test menjadi dasar perbaikan validasi.|Sudah ada di repo|
|9|RapidInputViewModelTest.kt|INP_006 - test invalid nominal characters|Unit Test / nominal.toDoubleOrNull()|Arsiparis|Menguji jalur input nominal berisi karakter non-numerik.|nominal='abc'; field wajib lain valid|Nilai nominal menjadi null/tidak valid dan tidak menyebabkan crash.|Sudah ada di repo|
|10|RapidInputViewModelTest.kt|test duplicate document number handling|Unit Test / archiveRepository.checkDocumentNumberAndTypeExists|Arsiparis|Menguji percabangan duplikasi dokumen dengan nomor dan status salinan yang sama.|docNumber=DUP-001; copyType=ORIGINAL; repository returns true|state.error='Nomor dokumen ini sudah ada dengan status yang sama.'|Sudah ada di repo|
|11|RapidInputViewModelTest.kt|INP_011 - test auto-bundle SP2D, SPM, and SPJ|Unit Test / auto-bundle SP2D-SPM-SPJ|Arsiparis|Menguji logika pembentukan bundel otomatis ketika dokumen SP2D dibuat bersama SPM dan SPJ.|docType=SP2D; autoBundle=true; nomor SP2D dan SPM terisi; SPJ description terisi|stagingRepository.insertToStaging dipanggil tepat 3 kali untuk dokumen terkait.|Sudah ada di repo|
|12|RapidInputViewModelTest.kt|MNG_001 - test editing staged document|Unit Test / edit staged document|Arsiparis|Menguji jalur edit dokumen staging dan penyimpanan ulang dengan nilai baru.|mockDoc id=doc-123; documentNumber OLD-NUM diubah menjadi NEW-NUM|Data staging tersimpan dengan id tetap doc-123 dan documentNumber=NEW-NUM.|Sudah ada di repo|
|13|RapidInputViewModelTest.kt|INP_007 - copy count locked to 1 for ORIGINAL|Unit Test / OnCopyTypeChange & OnCopyCountChange|Arsiparis|Menguji branch status dokumen asli agar jumlah salinan terkunci.|copyType=ORIGINAL; copyCount input=5|state.copyCount tetap '1'.|Sudah ada di repo|
|14|RapidInputViewModelTest.kt|INP_008 - copy count can be changed for COPY|Unit Test / OnCopyTypeChange & OnCopyCountChange|Arsiparis|Menguji branch dokumen salinan agar jumlah salinan dapat diubah.|copyType=COPY; copyCount input=5|state.copyCount berubah menjadi '5'.|Sudah ada di repo|
|15|RapidInputViewModelTest.kt|INP_010 - duplicate allowed if copy status different|Unit Test / duplicate warning|Arsiparis|Menguji jalur duplikasi nomor dokumen dengan status salinan berbeda.|docNumber=DUP-123; copyType=COPY; repository check type=false; repository check number=true|showDuplicateWarning=true dan state.error=null.|Sudah ada di repo|
|16|RapidInputViewModelTest.kt|INP_012 - auto bundle error if SPM number empty|Unit Test / auto-bundle validation|Arsiparis|Menguji validasi wajib nomor SPM saat auto-bundle aktif.|docType=SP2D; autoBundle=true; spmDocNumber kosong|validationErrors['spmDocNumber']='Nomor SPM wajib diisi'.|Sudah ada di repo|
|17|RapidInputViewModelTest.kt|BOX_003 - year must be 4 digits|Unit Test / validasi konteks box|Arsiparis|Menguji validasi tahun pada konteks box input arsip.|warehouse=Gedung A; rack=Rak 1; box=Box 1; year=26|validationErrors['year']='Tahun tidak valid (harus 4 digit)'.|Sudah ada di repo|
|18|RapidInputScreenTest.kt|testValidationErrorsWhenFieldsEmpty|Compose UI Test / RapidInputScreen|Arsiparis|Menguji render pesan validasi field wajib pada UI Compose.|RapidInputUiState(validationErrors docNumber dan subject)|Pesan 'Nomor dokumen wajib diisi' dan 'Uraian dokumen wajib diisi' tampil.|Sudah ada di repo|
|19|RapidInputScreenTest.kt|testCopyCountDisabledWhenOriginal|Compose UI Test / RapidInputScreen copy type ORIGINAL|Arsiparis|Menguji UI branch dokumen asli agar field jumlah salinan tidak ditampilkan.|copyType=ORIGINAL; copyCount=1|Node teks 'Jumlah Salinan' tidak ada pada UI.|Sudah ada di repo|
|20|RapidInputScreenTest.kt|testCopyCountEnabledWhenCopy|Compose UI Test / RapidInputScreen copy type COPY|Arsiparis|Menguji UI branch dokumen salinan agar field jumlah salinan tampil.|copyType=COPY; copyCount=2|Node teks 'Jumlah Salinan' tampil pada UI.|Sudah ada di repo|
|21|RapidInputScreenTest.kt|testAutoBundleToggleShowsSpmField|Compose UI Test / RapidInputScreen auto-bundle|Arsiparis|Menguji UI branch auto-bundle pada jenis dokumen SP2D.|docType=SP2D; isAutoBundleEnabled=true|Field 'Nomor SPM' dan 'Nomor SP2D' tampil.|Sudah ada di repo|

### 4.3. Staging Box

**Nomor test case:** 22, 23  
**Tujuan kelompok:** Kelompok ini memastikan dialog/tahap penentuan lokasi fisik box memiliki validasi yang benar sebelum dokumen dimasukkan ke staging.

|No|Nama File Test|Nama Tes|Metode dan Endpoint|Role|Tujuan|Input|Ekspektasi Output|Status Implementasi|
|---|---|---|---|---|---|---|---|---|
|22|StagingBoxListScreenTest.kt|testAddBoxDialogValidation|Compose UI Test / Add Box Dialog|Arsiparis|Menguji pesan validasi lokasi fisik saat tambah box tanpa data wajib.|validationErrors warehouse, rack, box; klik 'Tambah Box'|Pesan 'Gudang wajib diisi', 'Nomor rak wajib diisi', dan 'Nomor box wajib diisi' tampil.|Sudah ada di repo|
|23|StagingBoxListScreenTest.kt|testYearValidationMessage|Compose UI Test / Year validation dialog|Arsiparis|Menguji pesan validasi tahun pada dialog tambah box.|validationErrors['year']='Tahun tidak valid (harus 4 digit)'; klik 'Tambah Box'|Pesan 'Tahun tidak valid (harus 4 digit)' tampil.|Sudah ada di repo|

### 4.4. Pencarian & Filter

**Nomor test case:** 24, 25, 26, 27, 28, 29  
**Tujuan kelompok:** Kelompok ini menguji query Room DAO, filter tahun, pencarian keyword, pencegahan SQL Injection, penyimpanan lokal, state filter, serta guard agar export tidak berjalan ganda.

|No|Nama File Test|Nama Tes|Metode dan Endpoint|Role|Tujuan|Input|Ekspektasi Output|Status Implementasi|
|---|---|---|---|---|---|---|---|---|
|24|ArchiveDaoSearchTest.kt|testSearchByKeyword|Instrumented Test / Room DAO getArchivesList(keyword)|Arsiparis|Menguji query pencarian lokal berdasarkan keyword dokumen.|Seed: SP2D-001, SPM-002, SP2D-003; keyword=SP2D|DAO mengembalikan 2 data yang sesuai keyword SP2D.|Sudah ada di repo|
|25|ArchiveDaoSearchTest.kt|testFilterByYear|Instrumented Test / Room DAO getArchivesList(years)|Arsiparis|Menguji branch filter tahun pada Room Database.|Seed tahun 2024, 2025, 2026; filter years=[2026]|DAO mengembalikan 1 data dengan year=2026.|Sudah ada di repo|
|26|ArchiveDaoSearchTest.kt|testSqlInjectionInSearch|Instrumented Test / parameterized query Room|Arsiparis|Menguji keamanan query pencarian terhadap payload SQL Injection.|keyword="' OR '1'='1"|Payload diperlakukan sebagai literal string; hasil kosong dan database tidak rusak.|Sudah ada di repo|
|27|ArchiveDaoTest.kt|writeArchiveAndReadInList|Instrumented Test / Room insertArchive & getPendingArchives|Arsiparis|Menguji penyimpanan arsip lokal dan pembacaan daftar pending sinkronisasi.|ArchiveEntity id=1; documentNumber=DOC-001; syncStatus=DRAFT|getPendingArchives mengembalikan 1 data dengan documentNumber=DOC-001 dan syncStatus=DRAFT.|Sudah ada di repo|
|28|ArchiveListViewModelTest.kt|SCH_002 - filter by year and confirm updates state|Unit Test / ArchiveListViewModel filter state|Arsiparis|Menguji perubahan state filter tahun saat pengguna mengonfirmasi filter.|OnYearToggle(2026) lalu OnConfirmFilter|selectedYears={2026} dan isFilterConfirmed=true.|Sudah ada di repo|
|29|ArchiveListViewModelTest.kt|EXP_011 - double tap export prevention|Unit Test / ArchiveListViewModel export guard|Arsiparis/Kepala Subbag|Menguji pengaman double tap agar proses ekspor tidak dijalankan ganda.|ExportExcel(outputStream) dipanggil dua kali cepat setelah filter dikonfirmasi|exportArchivesUseCase dipanggil tepat 1 kali.|Sudah ada di repo|

### 4.5. Manajemen Arsip

**Nomor test case:** 30, 31  
**Tujuan kelompok:** Kelompok ini menguji jalur delete arsip pada detail ViewModel, baik ketika use case sukses maupun ketika server/repository mengembalikan error.

|No|Nama File Test|Nama Tes|Metode dan Endpoint|Role|Tujuan|Input|Ekspektasi Output|Status Implementasi|
|---|---|---|---|---|---|---|---|---|
|30|ArchiveDetailViewModelTest.kt|MNG_002 - deleteArchive successful|Unit Test / DeleteArchiveUseCase success|Arsiparis|Menguji jalur sukses penghapusan arsip dari halaman detail.|archiveId=123; deleteArchiveUseCase returns DomainResult.Success(Unit)|callback sukses dipanggil.|Sudah ada di repo|
|31|ArchiveDetailViewModelTest.kt|MNG_005 - deleteArchive server error|Unit Test / DeleteArchiveUseCase error|Arsiparis|Menguji jalur gagal penghapusan arsip ketika use case mengembalikan error.|archiveId=123; deleteArchiveUseCase returns DomainResult.Error('Server Error')|uiState.errorMessage='Server Error'.|Sudah ada di repo|

### 4.6. Impor & Ekspor Excel

**Nomor test case:** 32, 33  
**Tujuan kelompok:** Kelompok ini menguji service internal untuk menulis file Excel dan membaca kembali file Excel menjadi data arsip.

|No|Nama File Test|Nama Tes|Metode dan Endpoint|Role|Tujuan|Input|Ekspektasi Output|Status Implementasi|
|---|---|---|---|---|---|---|---|---|
|32|ExcelServiceImplTest.kt|EXP_008 - exportToExcel produces a valid output stream|Unit Test / ExcelServiceImpl.exportToExcel|Arsiparis/Kepala Subbag|Menguji logika penulisan file Excel dari daftar arsip.|archives=[SP2D-001, SPM-002]; outputStream=ByteArrayOutputStream|Output stream berisi byte Excel dan tidak kosong.|Sudah ada di repo|
|33|ExcelServiceImplTest.kt|IMP_001 - importFromExcel parses valid excel file|Unit Test / ExcelServiceImpl.importFromExcel|Arsiparis|Menguji parsing file Excel valid menjadi data arsip.|Excel hasil export dengan documentNumber=SP2D-IMPORT|importFromExcel mengembalikan 1 data dengan documentNumber=SP2D-IMPORT.|Sudah ada di repo|

### 4.7. Format Tanggal

**Nomor test case:** 34, 35, 36, 37  
**Tujuan kelompok:** Kelompok ini menguji utility DateVisualTransformation untuk memastikan format visual tanggal dan mapping posisi cursor aman.

|No|Nama File Test|Nama Tes|Metode dan Endpoint|Role|Tujuan|Input|Ekspektasi Output|Status Implementasi|
|---|---|---|---|---|---|---|---|---|
|34|DateVisualTransformationTest.kt|test formatting with 8 digits|Unit Test / DateVisualTransformation.filter|All Role|Menguji format tampilan tanggal dari 8 digit input.|input='12032024'|Output teks menjadi '12-03-2024'.|Sudah ada di repo|
|35|DateVisualTransformationTest.kt|test partial formatting|Unit Test / DateVisualTransformation.filter|All Role|Menguji format tanggal parsial saat pengguna baru mengetik sebagian digit.|input='12' dan input='1203'|Output masing-masing menjadi '12-' dan '12-03-'.|Sudah ada di repo|
|36|DateVisualTransformationTest.kt|test offset mapping original to transformed|Unit Test / OffsetMapping originalToTransformed|All Role|Menguji mapping posisi kursor dari string asli ke string hasil format.|input='12032024'|Mapping offset sesuai penambahan tanda '-' pada output.|Sudah ada di repo|
|37|DateVisualTransformationTest.kt|test offset mapping transformed to original|Unit Test / OffsetMapping transformedToOriginal|All Role|Menguji mapping posisi kursor dari string hasil format ke string asli.|output='12-03-2024'|Mapping offset kembali ke posisi original dengan benar.|Sudah ada di repo|

### 4.8. Dashboard & Rekap

**Nomor test case:** 38, 39  
**Tujuan kelompok:** Kelompok ini adalah kandidat white box untuk menguji agregasi dashboard dan rekap arsip per periode. Jika file test belum ada, status harus Blocked/Need Implementation.

|No|Nama File Test|Nama Tes|Metode dan Endpoint|Role|Tujuan|Input|Ekspektasi Output|Status Implementasi|
|---|---|---|---|---|---|---|---|---|
|38|AnalyticsViewModel.kt|REP_001 - hitung rekap arsip per periode|Unit Test kandidat / GetAnalyticsUseCase|Kepala Subbag|Menguji logika rekap jumlah dokumen per tahun/periode untuk dashboard laporan.|Data arsip beberapa tahun dan beberapa jenis dokumen|State analytics berisi total, distribusi jenis dokumen, dan data grafik sesuai agregasi.|Kandidat/perlu dibuat|
|39|HomeViewModel.kt|DASH_001 - muat ringkasan dashboard|Unit Test kandidat / GetYearStatsUseCase|All Role|Menguji pemuatan statistik total dokumen dan dokumen terbaru pada dashboard.|Repository mengembalikan statistik tahun 2026 dan daftar dokumen terbaru|Dashboard state memuat total dokumen dan recent archives tanpa error.|Kandidat/perlu dibuat|

### 4.9. OCR & AI Parsing

**Nomor test case:** 40, 41  
**Tujuan kelompok:** Kelompok ini adalah kandidat test untuk repository OCR dan AI parsing. Fokusnya pada ekstraksi teks dan pemetaan metadata hasil OCR.

|No|Nama File Test|Nama Tes|Metode dan Endpoint|Role|Tujuan|Input|Ekspektasi Output|Status Implementasi|
|---|---|---|---|---|---|---|---|---|
|40|OcrRepositoryImpl.kt|OCR_001 - ekstraksi teks gambar dokumen valid|Instrumented Test kandidat / ML Kit Text Recognition|Arsiparis|Menguji alur internal repository OCR saat gambar dokumen valid diproses.|Image fixture dokumen SP2D yang jelas|Repository mengembalikan string hasil OCR tidak kosong.|Kandidat/perlu dibuat|
|41|AiParserRepositoryImpl.kt|OCR_005 - parsing metadata hasil OCR|Unit/Integration Test kandidat / ParseMetadataWithAiUseCase|Arsiparis|Menguji pemetaan teks OCR menjadi metadata arsip seperti nomor dokumen, nominal, pihak ketiga, dan uraian.|Raw text OCR berisi nomor SP2D, nominal, pihak ketiga, dan perihal|ParsedMetadata terisi sesuai field target dan siap diverifikasi pada staging.|Kandidat/perlu dibuat|

### 4.10. Repository & Sinkronisasi

**Nomor test case:** 42, 43, 44, 45  
**Tujuan kelompok:** Kelompok ini adalah kandidat test untuk local-first repository, sinkronisasi Supabase, penyimpanan lokasi fisik, dan relasi bundle transaksi.

|No|Nama File Test|Nama Tes|Metode dan Endpoint|Role|Tujuan|Input|Ekspektasi Output|Status Implementasi|
|---|---|---|---|---|---|---|---|---|
|42|ArchiveRepositoryImpl.kt|OFF_001 - simpan arsip lokal pending sync|Unit/Integration Test kandidat / repository local-first|Arsiparis|Menguji jalur penyimpanan arsip ke Room saat sinkronisasi remote belum dilakukan.|ArchiveDocument valid; jaringan dimatikan/mock remote gagal|Data tersimpan lokal dengan status pending/draft dan tidak hilang.|Kandidat/perlu dibuat|
|43|ArchiveRepositoryImpl.kt|OFF_002 - sinkronisasi arsip ke Supabase|Integration Test kandidat / sync local to remote|Arsiparis|Menguji jalur sinkronisasi data lokal ke Supabase saat jaringan tersedia.|Data lokal pending_sync; koneksi aktif; Supabase mock success|Data terkirim ke remote dan status lokal berubah synced.|Kandidat/perlu dibuat|
|44|StorageLocationRepositoryImpl.kt|TRK_001 - simpan dan baca lokasi fisik arsip|Unit/Integration Test kandidat / storage location repository|Arsiparis|Menguji logika pemetaan lokasi gudang, rak, tingkat, dan box terhadap dokumen arsip.|storageLocation=Gedung A/Rak 1/Box 1; archiveId valid|Lokasi fisik tersimpan dan dapat dibaca pada detail arsip.|Kandidat/perlu dibuat|
|45|TransactionBundleRepositoryImpl.kt|BND_001 - relasi bundle transaksi|Unit/Integration Test kandidat / transaction bundle repository|Arsiparis|Menguji penyimpanan relasi bundel transaksi SP2D, SPM, dan SPJ.|bundle SP2D-BUNDLE-001 dengan dokumen SP2D, SPM, SPJ|Semua dokumen terhubung pada bundle_id yang sama.|Kandidat/perlu dibuat|

---

## 5. Traceability terhadap Komponen Repository

| Komponen | Test Case Terkait | Alasan Diuji |
|---|---|---|
| `LoginViewModel.kt` | 1–3 | Mengelola state login, validasi input kosong, dan error dari Supabase Auth. |
| `LoginScreen.kt` | 4–6 | Menggambarkan state loading, input credential, dan error message pada UI. |
| `RapidInputViewModel.kt` | 7–17 | Mengandung banyak branch validasi input arsip, copy type, auto-bundle, duplicate check, dan staging. |
| `RapidInputScreen.kt` | 18–21 | Menampilkan hasil state dan branch UI dari RapidInputViewModel. |
| `StagingBoxListScreen.kt` | 22–23 | Menampilkan validasi lokasi fisik dan tahun pada dialog box. |
| `ArchiveDao.kt` | 24–27 | Menguji query pencarian, filter tahun, SQL injection safety, dan pending local archive. |
| `ArchiveListViewModel.kt` | 28–29 | Menguji state filter dan pencegahan double export. |
| `ArchiveDetailViewModel.kt` | 30–31 | Menguji jalur sukses/gagal delete arsip. |
| `ExcelServiceImpl.kt` | 32–33 | Menguji konversi data arsip ke Excel dan parsing Excel ke data arsip. |
| `DateVisualTransformation.kt` | 34–37 | Menguji format tanggal dan offset mapping cursor. |
| `AnalyticsViewModel.kt`, `HomeViewModel.kt` | 38–39 | Kandidat test agregasi dashboard dan rekap. |
| `OcrRepositoryImpl.kt`, `AiParserRepositoryImpl.kt` | 40–41 | Kandidat test ekstraksi teks dan parsing metadata OCR. |
| `ArchiveRepositoryImpl.kt`, `StorageLocationRepositoryImpl.kt`, `TransactionBundleRepositoryImpl.kt` | 42–45 | Kandidat test repository lokal, sinkronisasi, lokasi fisik, dan relasi bundle. |

---

## 6. Kriteria Kelulusan Umum

1. Test method berjalan tanpa compile error.
2. Assertion sesuai dengan ekspektasi output pada Excel.
3. Tidak ada crash, unhandled exception, atau coroutine leak yang menyebabkan test gagal.
4. Untuk test DAO, database in-memory/Room test menghasilkan data sesuai query.
5. Untuk Compose UI test, node UI yang diharapkan muncul/hilang sesuai state.
6. Untuk test kandidat, test baru harus dibuat terlebih dahulu sebelum dapat dinilai.

---

## 7. Catatan Penting untuk AI Agent

1. Baris 38–45 dalam Excel sebagian besar adalah **kandidat white box lanjutan**. Jangan beri status `Pass` hanya karena komponen main source ada.
2. Test UI Compose bisa disebut grey box karena menguji perilaku layar, tetapi masih berdasarkan state internal.
3. Test yang paling kuat secara white box adalah test pada `ViewModel`, `DAO`, `Service`, dan utility function.
4. Jika hasil aktual berbeda dari ekspektasi Excel, agent harus membedakan apakah masalahnya ada pada kode, test, atau ekspektasi yang perlu direvisi.
5. Jangan mengubah ekspektasi output untuk membuat test terlihat lulus. Laporkan mismatch secara jujur.

---

## 8. Template Laporan Hasil yang Direkomendasikan

```markdown
# Hasil Eksekusi White Box Testing

Tanggal eksekusi: <tanggal>
Repository: Sistem-Arsip-BPKPAD-Balangan-main
Branch/commit: <isi jika tersedia>

## Summary
| Metrik | Nilai |
|---|---:|
| Total test case Excel | 45 |
| Pass |  |
| Failed |  |
| Blocked |  |
| Need Review |  |

## Detail
| No | Fitur | File Test | Nama Tes | Status | Catatan |
|---:|---|---|---|---|---|

## Failure Analysis
| Test | Error Utama | Kemungkinan Penyebab | Rekomendasi |
|---|---|---|---|
```
