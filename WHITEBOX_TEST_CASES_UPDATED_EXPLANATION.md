# Penjelasan Test Cases White Box Terbaru — SIARSIP BPKPAD Balangan

Dokumen ini menjelaskan pembaruan test cases setelah perubahan ERD dan implementasi fitur lokasi fisik pada aplikasi mobile Sistem Arsip Keuangan BPKPAD Balangan.

## 1. Alasan Test Case Diperbarui

Pada iterasi pertama, white box testing berfokus pada autentikasi, input manual, staging, pencarian, manajemen arsip, Excel service, format tanggal, serta beberapa kandidat test untuk dashboard, OCR, repository, dan sinkronisasi.

Baseline terbaru mengubah cakupan sistem karena struktur ERD tidak lagi hanya menyimpan metadata dokumen. Sistem sekarang memuat struktur lokasi fisik yang lebih eksplisit melalui:

- `Room`
- `Shelf`
- `Box`
- `StorageLocationRepository`
- `BoxManagementViewModel`
- `idStorageLocation` pada `ArchiveDocument`
- metadata lokasi pada `ArchiveMetadata`
- proses `BulkInsertArchivesUseCase` untuk menghubungkan staging box ke lokasi final
- activity log untuk perubahan arsip dan lokasi

Karena itu, test suite tidak cukup hanya diperbarui secara minor. Test perlu diperluas agar mencakup jalur logika lokasi, mapping dokumen-lokasi, relasi bundle transaksi, dan audit trail.

## 2. Ringkasan Perubahan dari Iterasi Pertama

| Area | Iterasi Pertama | Versi Terbaru |
|---|---|---|
| Jumlah test case | 45 | 95 skenario uji |
| Fokus lokasi | Masih kandidat/terbatas pada warehouse-rack-box string | Menjadi modul utama berbasis `Room`, `Shelf`, `Box`, dan `StorageLocationRepository` |
| Input arsip | Validasi form dan staging | Validasi form + konteks lokasi hierarkis + OCR autofill + bundle |
| Staging | Box sederhana | Session box yang dipetakan ke lokasi final saat bulk upload |
| Repository | Sebagian kandidat | Ditambah test untuk local-first, sync, mapper, activity log, storage location |
| Dashboard | Kandidat | Tetap kandidat, ditambah rekap range tahun dan potensi rekap lokasi |
| OCR | Kandidat | Ditambah ScanViewModel success/error dan error handling gambar buruk |
| Risiko teknis | Compose UI blocked | Ditambah risiko compile/refactor akibat constructor `RapidInputViewModel` dan field `showDuplicateWarning` |

## 3. Cakupan Test Terbaru per Kelompok

### 3.1 Autentikasi

Kelompok ini memvalidasi `LoginViewModel`, `AuthRepositoryImpl`, dan `LoginScreen`. Pembaruan penting adalah penambahan test format email dan mapping role. Hal ini diperlukan karena akses sistem berbeda antara Arsiparis dan Kepala Subbag.

### 3.2 Konteks Box dan Lokasi

Kelompok ini merupakan tambahan utama akibat perubahan ERD. Test tidak lagi cukup hanya memeriksa string `warehouse`, `rack`, dan `box`; sistem sekarang harus menguji cascade `Room -> Shelf -> Box`, validasi box duplikat, pembuatan staged box, dan peringatan retensi.

### 3.3 Input Manual

Kelompok input manual tetap penting karena `RapidInputViewModel` memuat banyak branch validasi. Pembaruan utamanya adalah setup session lokasi baru, validasi nominal yang lebih rinci, pengujian SPJ terhadap bundle, OCR autofill, serta warning duplikasi nomor dokumen.

### 3.4 Storage Location

Kelompok ini baru dan harus ditambahkan karena repository lokasi sudah berdiri sebagai modul tersendiri. Test harus memeriksa `getOrCreateLocation`, pembuatan room, shelf, box, flow pembacaan lokasi, serta filter pada `BoxManagementViewModel`.

### 3.5 Bulk Upload dan Mapping

Kelompok ini menguji transisi dari staging menuju arsip final. Bagian paling penting adalah memastikan setiap dokumen final memperoleh `idStorageLocation`, bundle lokal dipetakan ke bundle remote, dan staging tidak dihapus bila proses gagal.

### 3.6 Pencarian dan Filter

Test lama masih relevan untuk document number, filter tahun, dan SQL injection safety. Namun perlu tambahan test untuk pencarian berdasarkan `classificationCode` dan lokasi fisik jika fitur pencarian terbaru memang menampilkan filter lokasi.

### 3.7 Manajemen Arsip

Test delete sukses/gagal tetap digunakan. Tambahan baru adalah detail dokumen harus menampilkan related bundle dan lokasi fisik karena kebutuhan sistem sekarang tidak hanya menemukan metadata, tetapi juga posisi dokumen fisik.

### 3.8 Impor dan Ekspor Excel

Test lama tetap berlaku. Tambahan wajib adalah mapping kolom lokasi pada proses import/export agar data migrasi dari Excel lama tetap dapat mengisi struktur ERD terbaru.

### 3.9 Utility

Test tanggal tetap digunakan. Tambahan test nominal/currency disarankan karena nominal merupakan field penting pada dokumen SP2D/SPM dan sering dipengaruhi format ribuan.

### 3.10 Dashboard dan Rekap

Test dashboard pada iterasi pertama masih berstatus kandidat. Pada versi terbaru, test harus tetap dibuat untuk `HomeViewModel` dan `AnalyticsViewModel`, termasuk rekap range tahun dan potensi rekap lokasi.

### 3.11 OCR dan AI Parsing

Test OCR tidak boleh hanya memeriksa ekstraksi teks. Versi terbaru perlu memeriksa alur lengkap dari OCR ke `ParsedMetadata`, lalu ke `RapidInputViewModel` atau `ScanViewModel`.

### 3.12 Repository, Sinkronisasi, Log, dan Mapper

Kelompok ini mengunci integritas data. Test mapper perlu ditambahkan agar `idStorageLocation` dan metadata lokasi tidak hilang ketika data bergerak dari DTO, entity, dan domain model.

## 4. Daftar Test Case Terbaru

|No|Fitur|Nama File Test|Nama Tes|Metode dan Endpoint|Role|Tujuan|Input|Ekspektasi Output|Status|Catatan|
|---|---|---|---|---|---|---|---|---|---|---|
|1|Autentikasi|LoginViewModelTest.kt|LGN_001 - login sukses kredensial valid|Unit / LoginViewModel.authenticateAdmin()|All Role|Menguji jalur sukses login dan perubahan state.|email valid; password valid; AuthRepository.login=Success|isLoading=false; isLoginSuccessful=true; errorMessage=null|Existing - tetap|Pertahankan dari iterasi 1.|
|2|Autentikasi|LoginViewModelTest.kt|LGN_002 - format email tidak valid|Unit / validasi email LoginViewModel|All Role|Menguji validasi lokal sebelum request login.|email='abc'; password terisi|AuthRepository.login tidak dipanggil; errorMessage='Format email tidak valid.'|Baru|Perlu ditambah karena kode terbaru punya validasi email.|
|3|Autentikasi|LoginViewModelTest.kt|LGN_003 - login gagal kredensial salah|Unit / LoginViewModel.authenticateAdmin()|All Role|Menguji jalur error dari AuthRepository.|email salah; password salah; repository=Error|isLoginSuccessful=false; errorMessage berisi pesan gagal|Existing - tetap|Sesuaikan mock ke AuthRepository, bukan Supabase langsung.|
|4|Autentikasi|LoginViewModelTest.kt|LGN_006 - login gagal field kosong|Unit / validasi form login|All Role|Menguji validasi email/password kosong.|email=''; password=''|Request login tidak dieksekusi; errorMessage='Email dan password tidak boleh kosong.'|Existing - tetap|Sudah relevan.|
|5|Autentikasi|AuthRepositoryImplTest.kt|LGN_007 - mapping role arsiparis|Unit / AuthRepositoryImpl.mapEmailToRole via login/checkSession|Arsiparis|Menguji pemetaan role dari email instansi.|email='admin123@balangankab.go.id'|currentUserRole=ARSIPARIS|Baru|Diperlukan karena fitur role memengaruhi akses menu.|
|6|Autentikasi|AuthRepositoryImplTest.kt|LGN_008 - mapping role kasubbag|Unit / AuthRepositoryImpl.mapEmailToRole via login/checkSession|Kepala Subbag|Menguji pemetaan role Kepala Subbag.|email='kassubag@balangankab.go.id'|currentUserRole=KASSUBAG|Baru|Tambahkan fake Supabase/session.|
|7|Autentikasi|LoginScreenTest.kt|LGN_UI_001 - loading indicator login|Compose UI / LoginScreen|All Role|Menguji render state loading.|LoginUiState(isLoading=true)|Tombol login menampilkan indikator loading|Existing - refactor|Test lama blocked; perlu stabilkan testTag/semantics.|
|8|Autentikasi|LoginScreenTest.kt|LGN_UI_002 - input email/password|Compose UI / LoginScreen|All Role|Menguji event input credential.|email dan password diketik; klik login|TextField menerima input dan event login terpanggil|Existing - refactor|Gunakan testTag agar tidak rapuh.|
|9|Autentikasi|LoginScreenTest.kt|LGN_UI_003 - pesan error login tampil|Compose UI / LoginScreen|All Role|Menguji render error message.|LoginUiState(errorMessage='Email atau password salah.')|Dialog/teks error tampil|Existing - refactor|Blocked pada iterasi lama.|
|10|Konteks Box/Lokasi|RapidInputViewModelTest.kt|BOX_001 - load rooms saat init|Unit / RapidInputViewModel.init()|Arsiparis|Memastikan daftar gudang/ruang dimuat dari repository lokasi.|StorageLocationRepository.getRooms()=Success([Room])|roomsList=Success dan berisi room|Baru|Constructor test wajib tambah mock StorageLocationRepository.|
|11|Konteks Box/Lokasi|RapidInputViewModelTest.kt|BOX_002 - pilih room memuat shelves|Unit / OnRoomSelected|Arsiparis|Menguji cascade room -> shelf.|Room(id=R1); getShelvesByRoom(R1)=Success([Shelf])|selectedRoom terisi; shelvesList=Success|Baru|Menutup dampak ERD lokasi hierarkis.|
|12|Konteks Box/Lokasi|RapidInputViewModelTest.kt|BOX_003 - tahun harus 4 digit|Unit / OnConfirmBoxContext|Arsiparis|Menguji validasi tahun konteks box.|year='26'|validationErrors['year']='Tahun tidak valid (harus 4 digit)'|Existing - refactor|Test lama masih pakai warehouse/rack lama; update ke selectedRoom/selectedShelf.|
|13|Konteks Box/Lokasi|RapidInputViewModelTest.kt|BOX_004 - room wajib dipilih|Unit / OnConfirmBoxContext|Arsiparis|Menguji validasi room/gudang pada ERD baru.|selectedRoom=null; selectedShelf valid; typedBox valid; year=2026|validationErrors['warehouse']='Gudang wajib dipilih'|Baru|Mengganti validasi string warehouse lama.|
|14|Konteks Box/Lokasi|RapidInputViewModelTest.kt|BOX_005 - shelf wajib dipilih|Unit / OnConfirmBoxContext|Arsiparis|Menguji validasi rak/shelf pada ERD baru.|selectedRoom valid; selectedShelf=null; typedBox valid; year=2026|validationErrors['rack']='Nomor rak wajib dipilih'|Baru|Perlu mock checkBoxExists tidak terpanggil jika shelf null.|
|15|Konteks Box/Lokasi|RapidInputViewModelTest.kt|BOX_006 - nama box wajib diisi|Unit / OnConfirmBoxContext|Arsiparis|Menguji validasi box kosong.|selectedRoom valid; selectedShelf valid; typedBox=''|validationErrors['box']='Nama Box wajib diisi'|Baru|Sesuai ERD boxes.|
|16|Konteks Box/Lokasi|RapidInputViewModelTest.kt|BOX_007 - box duplikat pada shelf sama|Unit / checkBoxExists branch|Arsiparis|Menguji pencegahan duplikasi box pada rak yang sama.|selectedShelf=S1; typedBox='B-01'; checkBoxExists=true|validationErrors['box']='Box dengan nomor ini sudah ada di rak tersebut'|Baru|Penting karena lokasi kini entitas tersendiri.|
|17|Konteks Box/Lokasi|RapidInputViewModelTest.kt|BOX_008 - konfirmasi box valid membuat staged box|Unit / saveStagedBox|Arsiparis|Menguji pembuatan sesi box valid.|Room A; Shelf R-01; Box B-01; year=2026; checkBoxExists=false|saveStagedBox dipanggil; isBoxContextSet=true; navigationEvent berisi sessionId|Baru|Perlu collect navigationEvent.|
|18|Konteks Box/Lokasi|RapidInputViewModelTest.kt|BOX_009 - warning retensi untuk arsip >10 tahun|Unit / OnConfirmBoxContext retention warning|Arsiparis|Menguji branch peringatan retensi.|year lebih tua dari currentYear-10|warningMessage berisi peringatan retensi|Baru|Gunakan current year dinamis atau mock clock bila memungkinkan.|
|19|Konteks Box/Lokasi|RapidInputScreenTest.kt|BOX_UI_001 - dialog lokasi menampilkan error field wajib|Compose UI / RapidInputScreen location form|Arsiparis|Menguji render pesan validasi lokasi pada UI.|validationErrors warehouse/rack/box/year|Pesan validasi tampil pada layar|Baru|Menggantikan test StagingBoxList lama jika UI sudah berpindah.|
|20|Konteks Box/Lokasi|StagingBoxListScreenTest.kt|BOX_UI_002 - daftar staged box tampil|Compose UI / StagingBoxListScreen|Arsiparis|Menguji tampilan sesi box yang sudah dibuat.|existingStagedBoxes=[Gedung A/Rak 1/Box 1/2026]|Kartu/row sesi box tampil|Existing - perlu update|Sesuaikan label menjadi Room/Shelf/Box bila UI sudah berubah.|
|21|Input Manual|RapidInputViewModelTest.kt|INP_002 - field dokumen wajib|Unit / OnAddToBoxClick|Arsiparis|Menguji validasi nomor dokumen dan uraian.|session valid; documentNumber=''; subject=''|validationErrors docNumber dan subject tampil|Existing - refactor|Harus setup session dengan selectedRoom/selectedShelf.|
|22|Input Manual|RapidInputViewModelTest.kt|INP_003 - nominal negatif ditolak|Unit / validateInput|Arsiparis|Menguji validasi nominal kurang dari nol.|nominal='-1'; doc wajib valid|validationErrors['nominal']='Nominal tidak boleh kurang dari nol'|Existing - tetap/refactor|Setup lokasi baru.|
|23|Input Manual|RapidInputViewModelTest.kt|INP_004 - nominal wajib untuk SP2D/SPM|Unit / validateInput|Arsiparis|Menguji nominal wajib untuk dokumen pencairan.|docType=SP2D; nominal=''|validationErrors['nominal']='Nominal harus berupa angka lebih dari 0'|Baru|Aturan sudah ada di kode.|
|24|Input Manual|RapidInputViewModelTest.kt|INP_005 - SPJ tanpa bundle wajib nominal|Unit / validateInput|Arsiparis|Menguji aturan nominal pada SPJ mandiri.|docType=SPJ; selectedBundleId=null; nominal=''|Nominal error muncul|Baru|Penting setelah ada transaction bundle.|
|25|Input Manual|RapidInputViewModelTest.kt|INP_006 - karakter nominal tidak valid|Unit / nominal.toDoubleOrNull()|Arsiparis|Menguji nominal non-numerik.|nominal='abc'|validationErrors nominal muncul; tidak crash|Existing - tetap/refactor|Setup lokasi baru.|
|26|Input Manual|RapidInputViewModelTest.kt|INP_007 - copy count terkunci untuk ORIGINAL|Unit / OnCopyTypeChange + OnCopyCountChange|Arsiparis|Menguji dokumen asli hanya 1 lembar.|copyType=ORIGINAL; copyCount input=5|copyCount tetap '1'|Existing - tetap|Masih relevan.|
|27|Input Manual|RapidInputViewModelTest.kt|INP_008 - copy count dapat diubah untuk COPY|Unit / OnCopyTypeChange + OnCopyCountChange|Arsiparis|Menguji salinan dapat memiliki jumlah >1.|copyType=COPY; copyCount=5|copyCount='5'|Existing - tetap|Masih relevan.|
|28|Input Manual|RapidInputViewModelTest.kt|INP_009 - copy count COPY minimal 1|Unit / OnAddToBoxClick|Arsiparis|Menguji validasi jumlah salinan.|copyType=COPY; copyCount=0|validationErrors['copyCount']='Jumlah salinan minimal 1'|Baru|Ada branch di kode.|
|29|Input Manual|RapidInputViewModelTest.kt|INP_010 - duplikasi nomor beda copy type butuh warning|Unit / duplicate check|Arsiparis|Menguji nomor dokumen sama namun tipe salinan berbeda.|checkDocumentNumberExists=true; checkDocumentNumberAndTypeExists=false; forceSave=false|warning duplicate muncul atau state warning valid|Existing - perlu review|Kode memanggil showDuplicateWarning, tetapi field belum terlihat pada state.|
|30|Input Manual|RapidInputViewModelTest.kt|INP_011 - auto-bundle SP2D-SPM-SPJ|Unit / auto-bundle|Arsiparis|Menguji pembuatan 3 dokumen dalam satu bundle lokal.|docType=SP2D; autoBundle=true; nomor SP2D/SPM; deskripsi SPJ|insertToStaging dipanggil 3 kali dengan bundleId sama|Existing - refactor|Setup lokasi baru dan mock duplikasi.|
|31|Input Manual|RapidInputViewModelTest.kt|INP_012 - auto-bundle wajib nomor SPM|Unit / auto-bundle validation|Arsiparis|Menguji validasi nomor SPM.|docType=SP2D; autoBundle=true; spmDocumentNumber=''|validationErrors['spmDocNumber']='Nomor SPM wajib diisi'|Existing - refactor|Setup lokasi baru.|
|32|Input Manual|RapidInputViewModelTest.kt|INP_013 - pilih bundle mengisi nominal|Unit / OnBundleSelected|Arsiparis|Menguji nominal otomatis dari bundle terpilih.|stagedBundles=[id=B1, nominal=50000]; OnBundleSelected(B1)|selectedBundleId=B1; nominal='50000'|Baru|Penting untuk relasi SPJ ke bundle.|
|33|Input Manual|RapidInputViewModelTest.kt|INP_014 - hasil OCR mengisi form|Unit / OnOcrResultReceived|Arsiparis|Menguji autofill metadata hasil OCR.|ParsedMetadata(docNumber, subject, docType, nominal, year)|state form terisi sesuai metadata|Baru|Menghubungkan OCR ke input manual.|
|34|Input Manual|RapidInputViewModelTest.kt|MNG_001 - edit dokumen staging|Unit / OnEditStagedDoc + OnAddToBoxClick|Arsiparis|Menguji edit item staging dengan id tetap.|mockDoc id=doc-123; docNumber diubah|insertToStaging dipanggil dengan id lama dan data baru|Existing - refactor|Perlu mock StorageLocationRepository.|
|35|Input Manual|RapidInputScreenTest.kt|INP_UI_001 - pesan validasi field wajib tampil|Compose UI / RapidInputScreen|Arsiparis|Menguji render error validasi dokumen.|validationErrors docNumber/subject|Pesan validasi tampil|Existing - refactor|Blocked iterasi lama.|
|36|Input Manual|RapidInputScreenTest.kt|INP_UI_002 - jumlah salinan tersembunyi untuk ORIGINAL|Compose UI / RapidInputScreen|Arsiparis|Menguji branch UI copyType ORIGINAL.|copyType=ORIGINAL|Label 'Jumlah Salinan' tidak tampil|Existing - perbaiki|Gagal di iterasi lama; cek UI aktual.|
|37|Input Manual|RapidInputScreenTest.kt|INP_UI_003 - jumlah salinan tampil untuk COPY|Compose UI / RapidInputScreen|Arsiparis|Menguji branch UI copyType COPY.|copyType=COPY|Label 'Jumlah Salinan' tampil|Existing - refactor|Blocked iterasi lama.|
|38|Input Manual|RapidInputScreenTest.kt|INP_UI_004 - auto-bundle tampilkan field SPM/SPJ|Compose UI / RapidInputScreen|Arsiparis|Menguji branch UI auto-bundle.|docType=SP2D; autoBundle=true|Field SPM dan SPJ tampil|Existing - refactor|Blocked iterasi lama.|
|39|Storage Location|StorageLocationRepositoryImplTest.kt|LOC_001 - getOrCreateLocation memakai room existing|Unit/Integration / getOrCreateLocation|Arsiparis|Menguji lokasi existing tidak dibuat ulang.|getRoomByName returns Room; getShelfByName returns Shelf; box existing|Mengembalikan existing box id|Baru|Gunakan fake Supabase atau mock repository internal.|
|40|Storage Location|StorageLocationRepositoryImplTest.kt|LOC_002 - getOrCreateLocation membuat room baru|Unit/Integration / createRoom path|Arsiparis|Menguji branch room belum ada.|room belum ada; createRoom success|Room dibuat dan log CREATE LOCATION_ROOM tercatat|Baru|Dampak ERD rooms.|
|41|Storage Location|StorageLocationRepositoryImplTest.kt|LOC_003 - getOrCreateLocation membuat shelf baru|Unit/Integration / createShelf path|Arsiparis|Menguji branch shelf belum ada.|room ada; shelf belum ada|Shelf dibuat dan log CREATE LOCATION_SHELF tercatat|Baru|Dampak ERD shelves.|
|42|Storage Location|StorageLocationRepositoryImplTest.kt|LOC_004 - getOrCreateLocation membuat box baru|Unit/Integration / createBox path|Arsiparis|Menguji branch box belum ada.|room/shelf ada; box belum ada|Box dibuat dan id dikembalikan|Baru|Dampak ERD boxes.|
|43|Storage Location|StorageLocationRepositoryImplTest.kt|LOC_005 - getRooms sukses|Integration / getRooms flow|Arsiparis|Menguji pembacaan daftar room.|Supabase rooms returns list|Flow emits Loading lalu Success(list room)|Baru|Perlu fake client/staging.|
|44|Storage Location|StorageLocationRepositoryImplTest.kt|LOC_006 - getShelvesByRoom sukses|Integration / getShelvesByRoom|Arsiparis|Menguji pembacaan rak per room.|roomId='R1'|Flow emits Success(list shelf dengan roomId R1)|Baru|Penting untuk cascading dropdown.|
|45|Storage Location|StorageLocationRepositoryImplTest.kt|LOC_007 - checkBoxExists true/false|Unit/Integration / checkBoxExists|Arsiparis|Menguji deteksi duplikasi box.|shelfId=S1; name=B-01|true jika data ada; false jika tidak ada/error|Baru|Dipakai validasi box context.|
|46|Storage Location|BoxManagementViewModelTest.kt|LOC_008 - filter box membutuhkan shelf|Unit / BoxManagementViewModel.applyFilters|Arsiparis|Menguji perilaku daftar box sebelum shelf dipilih.|selectedFilterShelf=null|boxes=Idle|Baru|Sesuai kode ViewModel.|
|47|Storage Location|BoxManagementViewModelTest.kt|LOC_009 - filter box berdasarkan shelf|Unit / setFilterShelf|Arsiparis|Menguji filter box di halaman manajemen lokasi.|allBoxesList berisi beberapa shelf; selectedShelf=S1|boxes=Success hanya box shelf S1|Baru|Tambahkan test baru karena ada screen lokasi.|
|48|Storage Location|BoxManagementScreenTest.kt|LOC_UI_001 - list box berdasarkan room/rak tampil|Compose UI / BoxManagementScreen|Arsiparis|Menguji tampilan manajemen lokasi.|rooms, shelves, boxes tersedia|Dropdown room/rak dan daftar box tampil|Baru|Instrumented Compose UI.|
|49|Bulk Upload & Mapping|BulkInsertArchivesUseCaseTest.kt|BULK_001 - session tidak ditemukan|Unit / BulkInsertArchivesUseCase.invoke|Arsiparis|Menguji error saat session staging tidak ada.|getStagedBoxById=null|DomainResult.Error('Session box tidak ditemukan')|Baru|Sesuai DomainConstants.|
|50|Bulk Upload & Mapping|BulkInsertArchivesUseCaseTest.kt|BULK_002 - staging kosong|Unit / BulkInsertArchivesUseCase.invoke|Arsiparis|Menguji error jika tidak ada dokumen dalam box.|stagedBox ada; stagedDocs=[]|DomainResult.Error('Staging session kosong')|Baru|Sesuai DomainConstants.|
|51|Bulk Upload & Mapping|BulkInsertArchivesUseCaseTest.kt|BULK_003 - lokasi gagal dibuat|Unit / location error path|Arsiparis|Menguji propagasi error lokasi.|getOrCreateLocation=Error('network')|DomainResult.Error diawali 'Gagal inisialisasi lokasi'|Baru|Kritis setelah ERD lokasi.|
|52|Bulk Upload & Mapping|BulkInsertArchivesUseCaseTest.kt|BULK_004 - upload sukses menulis storage id|Unit / finalDocs mapping|Arsiparis|Menguji dokumen final mendapat idStorageLocation.|locationId='BOX-ID'; stagedDocs valid|saveArchives dipanggil dengan idStorageLocation='BOX-ID'; deleteStagedBox dipanggil|Baru|Test inti perubahan ERD.|
|53|Bulk Upload & Mapping|BulkInsertArchivesUseCaseTest.kt|BULK_005 - bundle lokal dipetakan ke remote bundle|Unit / transaction bundle mapping|Arsiparis|Menguji local bundleId diganti id remote.|stagedDocs bundleId=local-B1; createBundle=remote-B1|saveArchives menerima bundleId='remote-B1'|Baru|Menutup relasi transaction_bundles.|
|54|Bulk Upload & Mapping|BulkInsertArchivesUseCaseTest.kt|BULK_006 - gagal create bundle membatalkan save|Unit / createBundle error path|Arsiparis|Menguji rollback logical saat bundle gagal dibuat.|createBundle=Error|archiveRepository.saveArchives tidak dipanggil; staging tetap ada|Baru|Kritis untuk integritas data.|
|55|Bulk Upload & Mapping|RapidInputViewModelTest.kt|BULK_007 - upload satu box sukses|Unit / OnConfirmUpload|Arsiparis|Menguji state setelah bulk upload sukses.|bulkInsertArchivesUseCase=Success|isUploadSuccess=true; successMessage upload tampil|Baru|Ada branch di ViewModel.|
|56|Bulk Upload & Mapping|RapidInputViewModelTest.kt|BULK_008 - upload satu box gagal|Unit / OnConfirmUpload|Arsiparis|Menguji state error bulk upload.|bulkInsertArchivesUseCase=Error('gagal')|error='gagal'; isLoading=false|Baru|Ada branch di ViewModel.|
|57|Bulk Upload & Mapping|RapidInputViewModelTest.kt|BULK_009 - upload semua box partial failure|Unit / OnConfirmAllUpload|Arsiparis|Menguji beberapa session dengan satu gagal.|existingStagedBoxes=2; result pertama success, kedua error|isUploadSuccess=false; error=lastError|Baru|Perlu mock multiple invoke.|
|58|Pencarian & Filter|ArchiveDaoSearchTest.kt|SCH_001 - search by document number|Instrumented / Room DAO getArchivesList(query)|Arsiparis|Menguji pencarian keyword nomor dokumen.|Seed SP2D-001, SPM-002; keyword=SP2D|Mengembalikan data SP2D saja|Existing - tetap|Sudah ada, query masih documentNumber-only.|
|59|Pencarian & Filter|ArchiveDaoSearchTest.kt|SCH_002 - filter by year|Instrumented / Room DAO years|Arsiparis/Kepala Subbag|Menguji filter tahun.|Seed 2024, 2025, 2026; years=[2026]|Hanya data tahun 2026|Existing - tetap|Sudah ada.|
|60|Pencarian & Filter|ArchiveDaoSearchTest.kt|SCH_003 - SQL injection safety|Instrumented / parameterized Room query|Arsiparis/Kepala Subbag|Menguji payload injection diperlakukan literal.|query="' OR '1'='1"|Hasil kosong; DB aman|Existing - tetap|Sudah ada.|
|61|Pencarian & Filter|ArchiveDaoSearchTest.kt|SCH_004 - search by classificationCode|Instrumented / Room DAO candidate|Arsiparis/Kepala Subbag|Menguji kebutuhan pencarian klasifikasi arsip.|classificationCode='900.1.3.1'|Data dengan kode klasifikasi sesuai muncul|Baru|Perlu query baru bila fitur ini dimasukkan.|
|62|Pencarian & Filter|ArchiveDaoSearchTest.kt|SCH_005 - search/filter by location metadata|Instrumented / Room DAO candidate|Arsiparis/Kepala Subbag|Menguji pencarian berdasarkan lokasi fisik.|metadata.warehouse='Gedung A'; rack='R-01'; box='B-01'|Data lokasi sesuai muncul|Baru|Perlu dukungan query JSON/metadata atau field terpisah.|
|63|Pencarian & Filter|ArchiveListViewModelTest.kt|SCH_006 - confirm filter year update state|Unit / ArchiveListViewModel|Arsiparis/Kepala Subbag|Menguji state filter tahun.|OnYearToggle(2026); OnConfirmFilter|selectedYears={2026}; isFilterConfirmed=true|Existing - tetap|Sudah ada.|
|64|Pencarian & Filter|ArchiveListViewModelTest.kt|EXP_011 - guard double export|Unit / ArchiveListViewModel export guard|Arsiparis/Kepala Subbag|Menguji ekspor tidak dipanggil ganda.|ExportExcel dipanggil dua kali cepat|UseCase export dipanggil 1 kali|Existing - tetap|Sudah ada.|
|65|Manajemen Arsip|ArchiveDaoTest.kt|MNG_000 - insert local dan baca pending|Instrumented / Room insertArchive + getPendingArchives|Arsiparis|Menguji simpan lokal status DRAFT.|ArchiveEntity syncStatus=DRAFT|getPendingArchives mengembalikan data tersebut|Existing - tetap|Sudah ada.|
|66|Manajemen Arsip|ArchiveDetailViewModelTest.kt|MNG_002 - deleteArchive sukses|Unit / DeleteArchiveUseCase|Arsiparis|Menguji jalur sukses hapus arsip.|deleteArchiveUseCase=Success|callback sukses dipanggil|Existing - tetap|Sudah ada.|
|67|Manajemen Arsip|ArchiveDetailViewModelTest.kt|MNG_005 - deleteArchive server error|Unit / DeleteArchiveUseCase error|Arsiparis|Menguji jalur error hapus arsip.|deleteArchiveUseCase=Error('Server Error')|uiState.errorMessage='Server Error'|Existing - tetap|Sudah ada.|
|68|Manajemen Arsip|ArchiveDetailViewModelTest.kt|MNG_006 - detail menampilkan related bundle|Unit / getArchivesByBundleId|Arsiparis/Kepala Subbag|Menguji dokumen terkait pada bundle.|ArchiveDetail bundleId=B1; repository related docs tersedia|uiState.relatedDocuments berisi dokumen satu bundle|Baru|Repositori detail sudah memanggil getArchivesByBundleId.|
|69|Manajemen Arsip|ArchiveDetailScreenTest.kt|MNG_UI_001 - detail lokasi fisik tampil|Compose UI / ArchiveDetailScreen|Arsiparis/Kepala Subbag|Menguji render lokasi pada detail.|ArchiveMetadata warehouse/rack/boxNumber terisi|Label gudang, rak, box tampil sesuai metadata|Baru|Validasi visual fitur lokasi.|
|70|Impor & Ekspor Excel|ExcelServiceImplTest.kt|EXP_008 - exportToExcel valid stream|Unit / ExcelServiceImpl.exportToExcel|Arsiparis/Kepala Subbag|Menguji file Excel hasil ekspor tidak kosong.|archives=[SP2D-001, SPM-002]|outputStream berisi byte Excel|Existing - tetap|Sudah ada.|
|71|Impor & Ekspor Excel|ExcelServiceImplTest.kt|IMP_001 - importFromExcel valid|Unit / ExcelServiceImpl.importFromExcel|Arsiparis|Menguji parsing file Excel valid.|Excel berisi SP2D-IMPORT|Mengembalikan data arsip sesuai|Existing - tetap|Sudah ada.|
|72|Impor & Ekspor Excel|ExcelServiceImplTest.kt|IMP_002 - import Excel dengan kolom lokasi|Unit / ExcelServiceImpl.importFromExcel|Arsiparis|Menguji mapping kolom gudang/rak/box dari Excel.|Excel berisi warehouse, rack, boxNumber|ArchiveMetadata lokasi terisi|Baru|Diperlukan setelah ERD lokasi.|
|73|Impor & Ekspor Excel|ExcelServiceImplTest.kt|EXP_009 - export menyertakan idStorageLocation/lokasi|Unit / ExcelServiceImpl.exportToExcel|Arsiparis/Kepala Subbag|Menguji lokasi ikut terekspor.|ArchiveDocument idStorageLocation dan metadata lokasi terisi|Kolom lokasi muncul dan nilainya benar|Baru|Agar laporan sesuai struktur terbaru.|
|74|Utility|DateVisualTransformationTest.kt|DATE_001 - format 8 digit|Unit / DateVisualTransformation.filter|All Role|Menguji format tanggal lengkap.|input='12032024'|output='12-03-2024'|Existing - tetap|Sudah ada.|
|75|Utility|DateVisualTransformationTest.kt|DATE_002 - format parsial|Unit / DateVisualTransformation.filter|All Role|Menguji input tanggal parsial.|input='12'; input='1203'|output='12-' dan '12-03-'|Existing - tetap|Sudah ada.|
|76|Utility|DateVisualTransformationTest.kt|DATE_003 - offset original ke transformed|Unit / OffsetMapping|All Role|Menguji mapping posisi cursor.|input 8 digit|offset sesuai sisipan '-'|Existing - tetap|Sudah ada.|
|77|Utility|DateVisualTransformationTest.kt|DATE_004 - offset transformed ke original|Unit / OffsetMapping|All Role|Menguji mapping balik posisi cursor.|output '12-03-2024'|offset kembali benar|Existing - tetap|Sudah ada.|
|78|Utility|CurrencyVisualTransformationTest.kt|CUR_001 - format nominal ribuan|Unit / CurrencyVisualTransformation|Arsiparis|Menguji tampilan nominal input.|input='1000000'|output nominal terformat ribuan|Baru|Perlu ditambah karena nominal penting pada SP2D/SPM.|
|79|Dashboard & Rekap|HomeViewModelTest.kt|DASH_001 - muat ringkasan dashboard|Unit / GetYearStatsUseCase|All Role|Menguji statistik total dan recent archives.|Repository mengembalikan year stats dan recent data|HomeUiState memuat total/tahun tanpa error|Existing kandidat - buat|Blocked pada iterasi 1.|
|80|Dashboard & Rekap|AnalyticsViewModelTest.kt|REP_001 - rekap arsip per tahun|Unit / GetAnalyticsUseCase|Kepala Subbag|Menguji agregasi dashboard tahunan.|Data nominal beberapa kode klasifikasi tahun 2026|totalBudget dan budgetByClassification benar|Existing kandidat - buat|Blocked pada iterasi 1.|
|81|Dashboard & Rekap|AnalyticsViewModelTest.kt|REP_002 - rekap range tahun|Unit / GetAnalyticsRangeUseCase|Kepala Subbag|Menguji agregasi lintas tahun.|startYear=2024; endYear=2026|Agregasi sesuai range|Baru|Kode memiliki getAnalyticsDataForRange.|
|82|Dashboard & Rekap|BoxManagementViewModelTest.kt|REP_003 - rekap jumlah box per lokasi|Unit kandidat / repository getAllBoxes|Arsiparis/Kepala Subbag|Menguji rekap lokasi penyimpanan.|BoxDetails multi-room/shelf|Jumlah box per lokasi dapat dihitung/ditampilkan|Baru|Opsional jika dashboard lokasi ditampilkan.|
|83|OCR & AI Parsing|OcrRepositoryImplTest.kt|OCR_001 - ekstraksi teks gambar valid|Instrumented / ML Kit Text Recognition|Arsiparis|Menguji OCR pada fixture dokumen jelas.|Image fixture SP2D jelas|String OCR tidak kosong|Existing kandidat - buat|Blocked pada iterasi 1; butuh fixture.|
|84|OCR & AI Parsing|OcrRepositoryImplTest.kt|OCR_002 - gambar buram menghasilkan error/empty safe|Instrumented / OcrRepositoryImpl|Arsiparis|Menguji robust handling gambar tidak terbaca.|Image fixture blur/kosong|Tidak crash; ResultState/Error/empty ditangani|Baru|Penting untuk kualitas foto arsip.|
|85|OCR & AI Parsing|ParseMetadataWithAiUseCaseTest.kt|OCR_005 - parsing metadata hasil OCR|Unit / ParseMetadataWithAiUseCase|Arsiparis|Menguji mapping teks OCR ke metadata.|Raw text berisi nomor, nominal, pihak ketiga, perihal|ParsedMetadata field target terisi|Existing kandidat - buat|Blocked pada iterasi 1; gunakan fake AiParserRepository.|
|86|OCR & AI Parsing|ScanViewModelTest.kt|OCR_006 - ScanViewModel success state|Unit / ScanViewModel|Arsiparis|Menguji state scan setelah ekstraksi berhasil.|URI gambar valid; ExtractTextUseCase success; parser success|ScanUiState success dan parsed metadata terisi|Baru|Menghubungkan OCR dengan UI state.|
|87|OCR & AI Parsing|ScanViewModelTest.kt|OCR_007 - ScanViewModel error state|Unit / ScanViewModel|Arsiparis|Menguji error handling scan.|ExtractTextUseCase error|isLoading=false; errorMessage tampil|Baru|Perlu test coroutine.|
|88|Repository & Sinkronisasi|ArchiveRepositoryImplTest.kt|OFF_001 - saveArchive local-first pending|Unit/Integration / saveArchive|Arsiparis|Menguji local save tetap sukses saat remote gagal.|ArchiveDocument valid; API mock gagal|Room tersimpan DRAFT; DomainResult.Success(Unit)|Existing kandidat - buat|Blocked pada iterasi 1.|
|89|Repository & Sinkronisasi|ArchiveRepositoryImplTest.kt|OFF_002 - syncPendingArchives sukses|Unit/Integration / syncPendingArchives|Arsiparis|Menguji sinkronisasi draft ke remote.|pending archives ada; upsert success|syncStatus berubah SYNCED|Existing kandidat - buat|Blocked pada iterasi 1.|
|90|Repository & Sinkronisasi|ArchiveRepositoryImplTest.kt|OFF_003 - syncPendingArchives gagal remote|Unit/Integration / syncPendingArchives|Arsiparis|Menguji status lokal tidak berubah ketika remote gagal.|pending archives ada; upsert error|DomainResult.Error; data tetap DRAFT|Baru|Validasi offline-first.|
|91|Repository & Sinkronisasi|ActivityLogRepositoryImplTest.kt|LOG_001 - log create archive|Unit/Integration / logActivity|Arsiparis|Menguji audit trail saat arsip dibuat.|action=CREATE; entityType=ARCHIVE|record log terkirim/tersimpan dengan actorId|Baru|ERD menyertakan logs/activity_logs.|
|92|Repository & Sinkronisasi|ActivityLogRepositoryImplTest.kt|LOG_002 - log update/delete lokasi|Unit/Integration / logActivity dari StorageLocationRepository|Arsiparis|Menguji audit trail perubahan lokasi.|update/delete room/shelf/box|Log action UPDATE/DELETE tercatat|Baru|Penting karena lokasi fisik harus terlacak.|
|93|Repository & Sinkronisasi|TransactionBundleRepositoryImplTest.kt|BND_001 - create bundle transaksi|Unit/Integration / createBundle|Arsiparis|Menguji penyimpanan bundle transaksi.|name, description, year valid|DomainResult.Success(bundleId)|Existing kandidat - buat|Blocked pada iterasi 1.|
|94|Repository & Sinkronisasi|ArchiveMapperTest.kt|MAP_001 - mapper domain/entity mempertahankan lokasi|Unit / ArchiveMapper|All Role|Menguji `idStorageLocation` dan metadata tidak hilang saat mapping.|ArchiveDocument dengan idStorageLocation dan metadata lokasi|toEntity/toDomain mempertahankan nilai lokasi|Baru|Kritis setelah perubahan ERD.|
|95|Repository & Sinkronisasi|ArchiveMapperTest.kt|MAP_002 - mapper dto validasi UUID lokasi|Unit / ArchiveMapper.toDomain|All Role|Menguji idStorageLocation invalid menjadi null.|ArchiveDto idStorageLocation='invalid'|Domain idStorageLocation=null|Baru|Sesuai fungsi isValidUuid di mapper.|

## 5. Prioritas Implementasi

Prioritas tertinggi:

1. Refactor test lama yang gagal compile karena constructor `RapidInputViewModel` berubah.
2. Tambahkan test `BOX_001` sampai `BOX_009`.
3. Tambahkan test `BULK_001` sampai `BULK_009`.
4. Tambahkan mapper test `MAP_001` dan `MAP_002`.
5. Tambahkan test import/export lokasi.
6. Baru setelah itu lanjutkan OCR, dashboard, dan activity log.

## 6. Catatan Evaluatif

White box testing terbaru harus dianggap sebagai baseline baru, bukan sekadar tambahan dari iterasi pertama. Perubahan ERD membuat lokasi fisik menjadi bagian dari logika inti aplikasi. Jika lokasi tidak diuji, maka hasil white box testing tidak lagi cukup kuat untuk mendukung klaim bahwa sistem telah berjalan sesuai rancangan terbaru.
