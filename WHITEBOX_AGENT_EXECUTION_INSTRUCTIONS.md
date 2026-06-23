# Instruksi AI Agent — Eksekusi White Box Testing Aplikasi Mobile BPKPAD Balangan

Dokumen ini adalah **konteks operasional untuk AI agent** yang akan menjalankan keseluruhan white box testing berdasarkan file Excel `WhiteBox_Testing_Mobile_BPKPAD_Balangan.xlsx` dan repository Android `Sistem-Arsip-BPKPAD-Balangan-main3.zip`.

File ini harus diperlakukan sebagai **runbook**. Agent harus membaca, mengeksekusi, mencatat hasil, mengumpulkan bukti, dan mengembalikan ringkasan hasil eksekusi tanpa mengubah data produksi.

---

## 1. Tujuan

White box testing pada aplikasi ini bertujuan memvalidasi **struktur internal kode** dan **alur logika program** pada aplikasi mobile Sistem Arsip Keuangan BPKPAD Balangan. Fokus utamanya adalah:

1. Validasi jalur sukses dan gagal pada `ViewModel`.
2. Validasi percabangan input, error handling, dan state management.
3. Validasi query Room DAO dan keamanan parameterized query.
4. Validasi service internal seperti impor/ekspor Excel.
5. Validasi utility/visual transformation seperti format tanggal.
6. Validasi kandidat test lanjutan untuk OCR, AI parsing, dashboard, repository, dan sinkronisasi.

Agent **tidak boleh** memperlakukan white box testing sebagai sekadar klik UI. Test UI Compose boleh dijalankan, tetapi dikategorikan sebagai grey-box/white-box ringan karena menggunakan struktur internal screen dan test node.

---

## 2. Source of Truth

| Sumber | Lokasi | Fungsi |
|---|---|---|
| Excel test case | `/mnt/data/WhiteBox_Testing_Mobile_BPKPAD_Balangan.xlsx` | Daftar resmi 45 white box test case. |
| Repository | `/mnt/data/Sistem-Arsip-BPKPAD-Balangan-main3.zip` | Source code aplikasi Android Kotlin. |
| Project root setelah ekstraksi | `Sistem-Arsip-BPKPAD-Balangan-main/` | Lokasi menjalankan Gradle. |
| Unit test folder | `app/src/test/java/com/example/arsipbpkpad/` | Test JVM lokal. |
| Instrumented test folder | `app/src/androidTest/java/com/example/arsipbpkpad/` | Test Android/emulator. |
| Build script | `app/build.gradle.kts`, `gradle/libs.versions.toml` | Dependensi dan command Gradle. |

---

## 3. Prinsip Eksekusi Agent

1. **Jangan gunakan database produksi.** Jika test membutuhkan remote service, gunakan mock, staging, atau local fake.
2. **Jangan menulis secret asli** ke repository, log, screenshot, atau output markdown.
3. **Jangan mengubah source code aplikasi** kecuali tugas agent memang diminta untuk memperbaiki test yang gagal. Untuk eksekusi murni, perubahan tidak diperbolehkan.
4. **Jalankan test yang sudah ada terlebih dahulu**, lalu tandai test kandidat sebagai `Blocked/Need Implementation` bila file test belum ada.
5. Gunakan status hasil: `Pass`, `Failed`, `Blocked`, atau `Need Review`.
6. Untuk setiap kegagalan, simpan nama test class, nama method, command, error utama, path laporan Gradle, dan rekomendasi.

---

## 4. Persiapan Environment

### 4.1 Ekstraksi Repository

```bash
cd /mnt/data
rm -rf Sistem-Arsip-BPKPAD-Balangan-main
unzip -q Sistem-Arsip-BPKPAD-Balangan-main3.zip
cd Sistem-Arsip-BPKPAD-Balangan-main
```

Jika zip mengekstrak ke folder berbeda, cari `settings.gradle.kts` lalu gunakan folder tersebut sebagai root.

### 4.2 Validasi Struktur Project

```bash
pwd
ls -la
ls app/src/test/java/com/example/arsipbpkpad
ls app/src/androidTest/java/com/example/arsipbpkpad
```

Project valid apabila file berikut tersedia:

```text
settings.gradle.kts
build.gradle.kts
app/build.gradle.kts
gradlew
app/src/main/java/com/example/arsipbpkpad/
app/src/test/java/com/example/arsipbpkpad/
app/src/androidTest/java/com/example/arsipbpkpad/
```

### 4.3 Validasi Java dan Gradle Wrapper

```bash
java -version
chmod +x ./gradlew
./gradlew --version
```

### 4.4 File `local.properties`

Aplikasi membaca `SUPABASE_URL`, `SUPABASE_KEY`, dan `GROQ_API_KEY` dari `local.properties`. Untuk white box test unit yang memakai mock, secret asli tidak dibutuhkan. Jika file belum ada, buat file minimal:

```properties
SUPABASE_URL=
SUPABASE_KEY=
GROQ_API_KEY=
```

Jangan memasukkan secret produksi.

---

## 5. Klasifikasi Test

### 5.1 Unit Test JVM

Unit test dijalankan tanpa emulator. Command utama:

```bash
./gradlew testDebugUnitTest --continue
```

Command dengan log lebih detail:

```bash
./gradlew testDebugUnitTest --continue --stacktrace --info
```

Laporan hasil:

```text
app/build/reports/tests/testDebugUnitTest/index.html
app/build/test-results/testDebugUnitTest/
```

### 5.2 Instrumented Test Android

Instrumented test membutuhkan emulator atau perangkat Android.

```bash
adb devices
./gradlew connectedDebugAndroidTest --continue
```

Laporan hasil:

```text
app/build/reports/androidTests/connected/index.html
app/build/outputs/androidTest-results/connected/
```

Jika tidak ada emulator/device, tandai semua instrumented test sebagai `Blocked` dengan alasan `No connected Android device/emulator`.

### 5.3 Kandidat Test

Baris Excel yang memakai nama file seperti `AnalyticsViewModel.kt`, `HomeViewModel.kt`, `OcrRepositoryImpl.kt`, `AiParserRepositoryImpl.kt`, `ArchiveRepositoryImpl.kt`, `StorageLocationRepositoryImpl.kt`, dan `TransactionBundleRepositoryImpl.kt` adalah **test kandidat** jika file test belum ada. Agent harus mengecek apakah file test terkait sudah ada, lalu menandai sebagai `Blocked/Need Implementation` jika belum ada.

---

## 6. Command Eksekusi Per Kelompok Test

### 6.1 Autentikasi

```bash
./gradlew testDebugUnitTest --tests "com.example.arsipbpkpad.presentation.auth.LoginViewModelTest" --stacktrace
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.arsipbpkpad.presentation.auth.LoginScreenTest
```

Fokus assertion: login sukses, login gagal, field kosong, state loading, dan tampilan error.

### 6.2 Input Manual dan Staging

```bash
./gradlew testDebugUnitTest --tests "com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputViewModelTest" --stacktrace
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.arsipbpkpad.presentation.archive.add.manual.RapidInputScreenTest
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.arsipbpkpad.presentation.archive.add.manual.StagingBoxListScreenTest
```

Fokus assertion: field wajib, nominal, duplikasi, auto-bundle, editing staging, copy type, copy count, dan validasi tahun.

### 6.3 Pencarian, Filter, dan Room DAO

```bash
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.arsipbpkpad.data.local.ArchiveDaoSearchTest
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.arsipbpkpad.data.local.ArchiveDaoTest
./gradlew testDebugUnitTest --tests "com.example.arsipbpkpad.presentation.archive.list.ArchiveListViewModelTest" --stacktrace
```

Fokus assertion: keyword search, filter tahun, SQL Injection safety, insert/read lokal, dan guard double export.

### 6.4 Manajemen Arsip

```bash
./gradlew testDebugUnitTest --tests "com.example.arsipbpkpad.presentation.archive.detail.ArchiveDetailViewModelTest" --stacktrace
```

Fokus assertion: delete sukses dan delete gagal.

### 6.5 Impor dan Ekspor Excel

```bash
./gradlew testDebugUnitTest --tests "com.example.arsipbpkpad.data.service.ExcelServiceImplTest" --stacktrace
```

Fokus assertion: `exportToExcel` menghasilkan output stream, `importFromExcel` membaca file valid.

### 6.6 Format Tanggal

```bash
./gradlew testDebugUnitTest --tests "com.example.arsipbpkpad.utils.DateVisualTransformationTest" --stacktrace
```

Fokus assertion: format `dd-MM-yyyy`, input parsial, dan offset mapping kursor.

### 6.7 Kandidat Dashboard, OCR, Repository, dan Sinkronisasi

```bash
find app/src/test app/src/androidTest -type f | grep -E "Analytics|Home|Ocr|AiParser|ArchiveRepository|StorageLocation|TransactionBundle"
```

Jika belum ada, tandai test case terkait sebagai `Blocked - test belum diimplementasikan`.

Kandidat nama file test jika agent diberi izin membuat test baru:

```text
app/src/test/java/com/example/arsipbpkpad/presentation/analytics/AnalyticsViewModelTest.kt
app/src/test/java/com/example/arsipbpkpad/presentation/home/HomeViewModelTest.kt
app/src/androidTest/java/com/example/arsipbpkpad/data/repository/OcrRepositoryImplTest.kt
app/src/test/java/com/example/arsipbpkpad/domain/usecase/ParseMetadataWithAiUseCaseTest.kt
app/src/test/java/com/example/arsipbpkpad/data/repository/ArchiveRepositoryImplTest.kt
app/src/test/java/com/example/arsipbpkpad/data/repository/StorageLocationRepositoryImplTest.kt
app/src/test/java/com/example/arsipbpkpad/data/repository/TransactionBundleRepositoryImplTest.kt
```

---

## 7. Daftar Test Case dari Excel

Jumlah test case: **45**.

|No|Fitur|Nama File Test|Nama Tes|Metode dan Endpoint|Role|Status Implementasi|
|---|---|---|---|---|---|---|
|1|Autentikasi|LoginViewModelTest.kt|LGN_001 - login successful with valid credentials|Unit Test / LoginViewModel.authenticateAdmin()|All Role|Sudah ada di repo|
|2|Autentikasi|LoginViewModelTest.kt|LGN_003 - login failure with invalid credentials|Unit Test / LoginViewModel.authenticateAdmin()|All Role|Sudah ada di repo|
|3|Autentikasi|LoginViewModelTest.kt|LGN_006 - login fails when fields are empty|Unit Test / validasi form login|All Role|Sudah ada di repo|
|4|Autentikasi|LoginScreenTest.kt|testLoginButtonShowsLoadingIndicator|Compose UI Test / LoginScreen|All Role|Sudah ada di repo|
|5|Autentikasi|LoginScreenTest.kt|testEnteringCredentials|Compose UI Test / LoginScreen input events|All Role|Sudah ada di repo|
|6|Autentikasi|LoginScreenTest.kt|testErrorMessageDisplay|Compose UI Test / LoginScreen error dialog|All Role|Sudah ada di repo|
|7|Input Manual|RapidInputViewModelTest.kt|INP_002 - test validation fails when fields are empty|Unit Test / RapidInputViewModel.OnAddToBoxClick|Arsiparis|Sudah ada di repo|
|8|Input Manual|RapidInputViewModelTest.kt|INP_003 - test nominal validation|Unit Test / validasi nominal RapidInputViewModel|Arsiparis|Sudah ada di repo|
|9|Input Manual|RapidInputViewModelTest.kt|INP_006 - test invalid nominal characters|Unit Test / nominal.toDoubleOrNull()|Arsiparis|Sudah ada di repo|
|10|Input Manual|RapidInputViewModelTest.kt|test duplicate document number handling|Unit Test / archiveRepository.checkDocumentNumberAndTypeExists|Arsiparis|Sudah ada di repo|
|11|Input Manual|RapidInputViewModelTest.kt|INP_011 - test auto-bundle SP2D, SPM, and SPJ|Unit Test / auto-bundle SP2D-SPM-SPJ|Arsiparis|Sudah ada di repo|
|12|Input Manual|RapidInputViewModelTest.kt|MNG_001 - test editing staged document|Unit Test / edit staged document|Arsiparis|Sudah ada di repo|
|13|Input Manual|RapidInputViewModelTest.kt|INP_007 - copy count locked to 1 for ORIGINAL|Unit Test / OnCopyTypeChange & OnCopyCountChange|Arsiparis|Sudah ada di repo|
|14|Input Manual|RapidInputViewModelTest.kt|INP_008 - copy count can be changed for COPY|Unit Test / OnCopyTypeChange & OnCopyCountChange|Arsiparis|Sudah ada di repo|
|15|Input Manual|RapidInputViewModelTest.kt|INP_010 - duplicate allowed if copy status different|Unit Test / duplicate warning|Arsiparis|Sudah ada di repo|
|16|Input Manual|RapidInputViewModelTest.kt|INP_012 - auto bundle error if SPM number empty|Unit Test / auto-bundle validation|Arsiparis|Sudah ada di repo|
|17|Input Manual|RapidInputViewModelTest.kt|BOX_003 - year must be 4 digits|Unit Test / validasi konteks box|Arsiparis|Sudah ada di repo|
|18|Input Manual|RapidInputScreenTest.kt|testValidationErrorsWhenFieldsEmpty|Compose UI Test / RapidInputScreen|Arsiparis|Sudah ada di repo|
|19|Input Manual|RapidInputScreenTest.kt|testCopyCountDisabledWhenOriginal|Compose UI Test / RapidInputScreen copy type ORIGINAL|Arsiparis|Sudah ada di repo|
|20|Input Manual|RapidInputScreenTest.kt|testCopyCountEnabledWhenCopy|Compose UI Test / RapidInputScreen copy type COPY|Arsiparis|Sudah ada di repo|
|21|Input Manual|RapidInputScreenTest.kt|testAutoBundleToggleShowsSpmField|Compose UI Test / RapidInputScreen auto-bundle|Arsiparis|Sudah ada di repo|
|22|Staging Box|StagingBoxListScreenTest.kt|testAddBoxDialogValidation|Compose UI Test / Add Box Dialog|Arsiparis|Sudah ada di repo|
|23|Staging Box|StagingBoxListScreenTest.kt|testYearValidationMessage|Compose UI Test / Year validation dialog|Arsiparis|Sudah ada di repo|
|24|Pencarian & Filter|ArchiveDaoSearchTest.kt|testSearchByKeyword|Instrumented Test / Room DAO getArchivesList(keyword)|Arsiparis|Sudah ada di repo|
|25|Pencarian & Filter|ArchiveDaoSearchTest.kt|testFilterByYear|Instrumented Test / Room DAO getArchivesList(years)|Arsiparis|Sudah ada di repo|
|26|Pencarian & Filter|ArchiveDaoSearchTest.kt|testSqlInjectionInSearch|Instrumented Test / parameterized query Room|Arsiparis|Sudah ada di repo|
|27|Pencarian & Filter|ArchiveDaoTest.kt|writeArchiveAndReadInList|Instrumented Test / Room insertArchive & getPendingArchives|Arsiparis|Sudah ada di repo|
|28|Pencarian & Filter|ArchiveListViewModelTest.kt|SCH_002 - filter by year and confirm updates state|Unit Test / ArchiveListViewModel filter state|Arsiparis|Sudah ada di repo|
|29|Pencarian & Filter|ArchiveListViewModelTest.kt|EXP_011 - double tap export prevention|Unit Test / ArchiveListViewModel export guard|Arsiparis/Kepala Subbag|Sudah ada di repo|
|30|Manajemen Arsip|ArchiveDetailViewModelTest.kt|MNG_002 - deleteArchive successful|Unit Test / DeleteArchiveUseCase success|Arsiparis|Sudah ada di repo|
|31|Manajemen Arsip|ArchiveDetailViewModelTest.kt|MNG_005 - deleteArchive server error|Unit Test / DeleteArchiveUseCase error|Arsiparis|Sudah ada di repo|
|32|Impor & Ekspor Excel|ExcelServiceImplTest.kt|EXP_008 - exportToExcel produces a valid output stream|Unit Test / ExcelServiceImpl.exportToExcel|Arsiparis/Kepala Subbag|Sudah ada di repo|
|33|Impor & Ekspor Excel|ExcelServiceImplTest.kt|IMP_001 - importFromExcel parses valid excel file|Unit Test / ExcelServiceImpl.importFromExcel|Arsiparis|Sudah ada di repo|
|34|Format Tanggal|DateVisualTransformationTest.kt|test formatting with 8 digits|Unit Test / DateVisualTransformation.filter|All Role|Sudah ada di repo|
|35|Format Tanggal|DateVisualTransformationTest.kt|test partial formatting|Unit Test / DateVisualTransformation.filter|All Role|Sudah ada di repo|
|36|Format Tanggal|DateVisualTransformationTest.kt|test offset mapping original to transformed|Unit Test / OffsetMapping originalToTransformed|All Role|Sudah ada di repo|
|37|Format Tanggal|DateVisualTransformationTest.kt|test offset mapping transformed to original|Unit Test / OffsetMapping transformedToOriginal|All Role|Sudah ada di repo|
|38|Dashboard & Rekap|AnalyticsViewModel.kt|REP_001 - hitung rekap arsip per periode|Unit Test kandidat / GetAnalyticsUseCase|Kepala Subbag|Kandidat/perlu dibuat|
|39|Dashboard & Rekap|HomeViewModel.kt|DASH_001 - muat ringkasan dashboard|Unit Test kandidat / GetYearStatsUseCase|All Role|Kandidat/perlu dibuat|
|40|OCR & AI Parsing|OcrRepositoryImpl.kt|OCR_001 - ekstraksi teks gambar dokumen valid|Instrumented Test kandidat / ML Kit Text Recognition|Arsiparis|Kandidat/perlu dibuat|
|41|OCR & AI Parsing|AiParserRepositoryImpl.kt|OCR_005 - parsing metadata hasil OCR|Unit/Integration Test kandidat / ParseMetadataWithAiUseCase|Arsiparis|Kandidat/perlu dibuat|
|42|Repository & Sinkronisasi|ArchiveRepositoryImpl.kt|OFF_001 - simpan arsip lokal pending sync|Unit/Integration Test kandidat / repository local-first|Arsiparis|Kandidat/perlu dibuat|
|43|Repository & Sinkronisasi|ArchiveRepositoryImpl.kt|OFF_002 - sinkronisasi arsip ke Supabase|Integration Test kandidat / sync local to remote|Arsiparis|Kandidat/perlu dibuat|
|44|Repository & Sinkronisasi|StorageLocationRepositoryImpl.kt|TRK_001 - simpan dan baca lokasi fisik arsip|Unit/Integration Test kandidat / storage location repository|Arsiparis|Kandidat/perlu dibuat|
|45|Repository & Sinkronisasi|TransactionBundleRepositoryImpl.kt|BND_001 - relasi bundle transaksi|Unit/Integration Test kandidat / transaction bundle repository|Arsiparis|Kandidat/perlu dibuat|

---

## 8. Alur Kerja Wajib Agent

### Langkah 1 — Preflight

```bash
cd /mnt/data/Sistem-Arsip-BPKPAD-Balangan-main
chmod +x ./gradlew
./gradlew --version
./gradlew :app:tasks --all | grep -E "testDebugUnitTest|connectedDebugAndroidTest|assembleDebug"
adb devices || true
```

Catat versi Gradle, versi Java, task testing yang tersedia, serta ketersediaan emulator/device.

### Langkah 2 — Jalankan Unit Test

```bash
./gradlew testDebugUnitTest --continue --stacktrace
```

Simpan hasil:

```bash
mkdir -p testing-evidence/whitebox/unit
cp -r app/build/reports/tests/testDebugUnitTest testing-evidence/whitebox/unit/report || true
cp -r app/build/test-results/testDebugUnitTest testing-evidence/whitebox/unit/results || true
```

### Langkah 3 — Jalankan Instrumented Test

Jika ada emulator/device:

```bash
./gradlew connectedDebugAndroidTest --continue --stacktrace
```

Simpan hasil:

```bash
mkdir -p testing-evidence/whitebox/instrumented
cp -r app/build/reports/androidTests/connected testing-evidence/whitebox/instrumented/report || true
cp -r app/build/outputs/androidTest-results/connected testing-evidence/whitebox/instrumented/results || true
adb logcat -d > testing-evidence/whitebox/instrumented/logcat_after_test.txt || true
```

Jika tidak ada emulator/device, jangan memaksa; tandai instrumented test sebagai `Blocked`.

### Langkah 4 — Cocokkan Hasil dengan Excel

Untuk setiap baris Excel, cek file test, cek method test, cek hasil Gradle, lalu isi status akhir di laporan agent.

### Langkah 5 — Buat Ringkasan Final

Agent harus menghasilkan ringkasan:

```markdown
# Ringkasan Eksekusi White Box Testing

- Total test case Excel: 45
- Test case berhasil dijalankan: ...
- Pass: ...
- Failed: ...
- Blocked: ...
- Need Review: ...
- Evidence folder: `testing-evidence/whitebox/`

## Daftar Kegagalan
| Test Case | File | Penyebab | Bukti | Rekomendasi |
|---|---|---|---|---|
```

---

## 9. Format Evidence

```text
testing-evidence/
└── whitebox/
    ├── unit/
    │   ├── report/
    │   ├── results/
    │   └── unit-test-summary.md
    ├── instrumented/
    │   ├── report/
    │   ├── results/
    │   ├── logcat_after_test.txt
    │   └── instrumented-test-summary.md
    └── whitebox-final-summary.md
```

Untuk test Compose UI, bila gagal karena UI assertion, ambil screenshot bila device mendukung:

```bash
adb exec-out screencap -p > testing-evidence/whitebox/instrumented/failure_screen.png
```

---

## 10. Troubleshooting

| Gejala | Penyebab Umum | Tindakan |
|---|---|---|
| `No connected devices` | Emulator belum aktif | Jalankan emulator atau tandai instrumented test `Blocked`. |
| `SDK location not found` | `local.properties` belum berisi `sdk.dir` | Jalankan di Android Studio/CI Android yang benar. |
| `MockK exception` | Mock target tidak sesuai signature | Periksa method yang dimock dan update matcher. |
| `Hilt generated class error` | KSP/Hilt belum generate | Jalankan `./gradlew clean assembleDebug`. |
| `Room schema/export error` | Konfigurasi Room berubah | Cek `AppDatabase`, entity, DAO, dan migration. |
| `Compose node not found` | Teks/test tag berubah | Sesuaikan assertion dengan UI aktual. |
| Test kandidat tidak ada | Excel berisi rencana lanjutan | Tandai `Blocked - perlu implementasi test`. |

---

## 11. Batasan

1. Jangan menyatakan test `Pass` hanya karena file test ada.
2. Jangan menyatakan fitur `Pass` jika Gradle gagal compile.
3. Jangan menjalankan test pada Supabase produksi.
4. Jangan mengisi hasil coverage jika plugin coverage belum dikonfigurasi.
5. Jangan membuat klaim performa tanpa pengukuran.
6. Jangan mengubah kode aplikasi untuk membuat test lulus tanpa instruksi eksplisit.

---

## 12. Output Akhir yang Diharapkan dari Agent

1. Ringkasan eksekusi per command.
2. Tabel hasil 45 test case.
3. Daftar test yang belum bisa dijalankan.
4. Daftar test kandidat yang perlu dibuat.
5. Evidence folder path.
6. Rekomendasi perbaikan bila ada test gagal.
