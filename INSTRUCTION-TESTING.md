# Instruksi AI Agent untuk Otomatisasi Test Cases Aplikasi Arsip Keuangan Mobile

Dokumen ini berisi instruksi operasional bagi AI agent atau automation agent untuk menjalankan otomatisasi test cases berdasarkan prioritas 1–3.

---

# 1. Tujuan Otomatisasi

AI agent bertugas membantu melakukan otomatisasi pengujian terhadap aplikasi arsip keuangan mobile yang memiliki fitur utama:

1. Input manual dokumen keuangan.
2. OCR dokumen dari kamera dan galeri.
3. Staging hasil OCR sebelum masuk database produksi.
4. Penyimpanan lokal menggunakan Room DB.
5. Sinkronisasi ke Supabase.
6. Pencarian dan filtering arsip.
7. Tracking lokasi fisik arsip.
8. Manajemen metadata arsip.
9. Audit log aktivitas.
10. Report, ekspor Excel, dan ekspor PDF.

---

# 2. Prinsip Kerja AI Agent

AI agent harus mengikuti prinsip berikut:

1. Jalankan test case berdasarkan prioritas.
2. Prioritas 1 wajib diotomatisasi terlebih dahulu.
3. Prioritas 2 dijalankan sebagai semi-otomatis karena sebagian membutuhkan kamera, galeri, permission, atau validasi visual.
4. Prioritas 3 dijalankan sebagai manual exploratory dengan bantuan checklist.
5. Jangan mengubah data produksi asli.
6. Gunakan data dummy dengan prefix khusus, misalnya:
   - `AUTO-INP`
   - `AUTO-OFF`
   - `AUTO-SCH`
   - `AUTO-MNG`
   - `AUTO-EXP`
7. Setiap hasil test harus disimpan dalam log eksekusi.
8. Jika test gagal, simpan bukti berupa screenshot, log error, payload, dan kondisi perangkat.

---

# 3. Lingkungan Pengujian

## 3.1 Environment

Gunakan environment khusus testing/staging.

| Komponen | Instruksi |
|---|---|
| Aplikasi | Build debug atau staging |
| Database lokal | Room DB test instance |
| Database server | Supabase staging |
| Akun Arsiparis | Gunakan akun dummy testing |
| Akun Kepala Subbag | Gunakan akun dummy testing |
| Device | Emulator Android dan minimal satu real device |
| Network | Online, offline, slow network, server error mock |
| Storage | Normal, hampir penuh, penuh |
| Permission | Granted dan denied |

## 3.2 Tools yang Disarankan

| Kebutuhan | Tools |
|---|---|
| Unit Test | JUnit, Kotlin Test |
| Android UI Test | Espresso, Compose UI Test, Appium |
| Mock API | MockWebServer |
| Room DB Test | In-memory Room Database |
| File Output Test | Apache POI untuk Excel, PDF parser untuk PDF |
| Performance Test | Android Benchmark, custom timer assertion |
| CI/CD | GitHub Actions, GitLab CI, atau Jenkins |

---

# 4. Strategi Eksekusi Berdasarkan Prioritas

## 4.1 Prioritas 1 — Full Automation

AI agent harus mengotomatisasi test case berikut:

- INP_001 sampai INP_006
- OFF_001 sampai OFF_009
- SCH_001 sampai SCH_006
- MNG_001 sampai MNG_005
- AUD_001 sampai AUD_003
- EXP_001 sampai EXP_011

Jenis automation yang digunakan:

| Area | Jenis Test |
|---|---|
| Input Manual | Unit Test dan UI Test |
| Offline & Sync | Integration Test |
| Search & Filter | Unit Test, Integration Test, Performance Test |
| Management | UI Test dan Integration Test |
| Audit Log | Integration Test |
| Export | UI Test dan File Output Test |

## 4.2 Prioritas 2 — Semi-Automation

AI agent boleh menjalankan otomatisasi parsial untuk:

- OCR_001 sampai OCR_011
- TRK_001
- REP_001

Instruksi:

1. Gunakan gambar fixture untuk OCR.
2. Simpan minimal empat tipe gambar:
   - `ocr_clear_document.jpg`
   - `ocr_blank.jpg`
   - `ocr_dark.jpg`
   - `ocr_blur.jpg`
3. Untuk validasi kamera, gunakan emulator camera mock jika tersedia.
4. Untuk permission, gunakan command automation untuk grant/revoke permission.
5. Hasil OCR tidak harus 100% sama, tetapi harus memenuhi aturan minimal:
   - Tidak crash.
   - Ada teks untuk gambar jernih.
   - Ada pesan error atau empty result untuk gambar buruk.
   - Data masuk staging, bukan langsung produksi.

## 4.3 Prioritas 3 — Manual Exploratory

AI agent harus membuat checklist dan membantu dokumentasi untuk:

- TRK_002 sampai TRK_006
- OCR_VISUAL_001
- PDF_VISUAL_001
- DASH_VISUAL_001

Instruksi:

1. Jalankan skenario pada real device.
2. Ambil screenshot sebelum dan sesudah aksi.
3. Catat observasi visual.
4. Tandai status:
   - Passed
   - Failed
   - Blocked
   - Need Review

---

# 5. Data Setup

## 5.1 Akun Dummy

Buat atau gunakan akun berikut pada environment staging:

| Role | Username | Password |
|---|---|---|
| Arsiparis | `arsiparis.test@example.com` | `Password123!` |
| Kepala Subbag | `kasubbag.test@example.com` | `Password123!` |

Jika aplikasi menggunakan autentikasi lain, AI agent harus mengambil kredensial dari secret manager CI/CD.

## 5.2 Data Arsip Dummy

Gunakan struktur data berikut:

```json
{
  "nomor_dokumen": "AUTO-DOC-001",
  "tahun_anggaran": "2026",
  "subbagian": "Keuangan",
  "jenis_dokumen": "SPJ",
  "nominal": 1000000,
  "tanggal_dokumen": "2026-06-17",
  "gedung": "Gedung A",
  "ruang": "Ruang 1",
  "rak": "Rak 2",
  "box": "Box 3"
}
```

## 5.3 Variasi Data BVA

Gunakan variasi berikut untuk test nominal:

| Kategori | Nilai |
|---|---|
| Invalid bawah | `-1` |
| Invalid batas | `0` |
| Valid minimum | `1` |
| Valid normal | `1000000` |
| Valid besar | `999999999` |
| Invalid format | `abc`, `10abc`, `@5000`, `Rp1000` |

Gunakan variasi berikut untuk rentang tanggal:

| Kategori | Tanggal Awal | Tanggal Akhir |
|---|---|---|
| Valid | `2026-01-01` | `2026-12-31` |
| Batas sama | `2026-06-17` | `2026-06-17` |
| Invalid | `2026-12-31` | `2026-01-01` |

---

# 6. Instruksi Automation per Modul

## 6.1 Modul Input Manual

AI agent harus:

1. Login sebagai Arsiparis.
2. Buka menu input dokumen.
3. Isi field berdasarkan test data.
4. Tekan tombol Simpan.
5. Validasi pesan sukses/error.
6. Validasi data muncul atau tidak muncul pada daftar arsip.
7. Untuk negative case, pastikan data tidak tersimpan.

Assertion wajib:

- Field wajib kosong harus memunculkan error.
- Nominal negatif dan nol harus ditolak.
- Huruf/simbol pada nominal tidak boleh tersimpan sebagai nilai valid.
- State form tidak hilang setelah rotasi layar.

---

## 6.2 Modul Offline dan Sync

AI agent harus:

1. Menyiapkan kondisi online/offline.
2. Saat offline, simpan data ke Room DB.
3. Pastikan status data adalah `pending_sync`.
4. Aktifkan internet.
5. Jalankan atau tunggu background sync.
6. Pastikan data masuk Supabase staging/test.
7. Pastikan status lokal berubah menjadi `synced`.

Assertion wajib:

- Data offline tidak hilang.
- Data tidak duplikat saat tombol Simpan ditekan berulang.
- Konflik nomor dokumen ditangani.
- Login tidak boleh berhasil saat offline jika belum ada sesi aktif.
- Storage penuh harus menghasilkan pesan error, bukan crash.

---

## 6.3 Modul Search dan Filter

AI agent harus:

1. Seed database dengan minimal 20 data dummy.
2. Gunakan variasi tahun, subbagian, jenis dokumen, dan nominal.
3. Jalankan pencarian keyword.
4. Jalankan filter kombinasi.
5. Uji kondisi online dan offline.
6. Uji server error.
7. Uji payload SQL injection.

Assertion wajib:

- Search valid menampilkan data sesuai keyword.
- Search tidak cocok menampilkan empty state.
- Search harus selesai kurang dari 2 detik untuk SCH_001.
- SQL injection tidak boleh mengembalikan data tidak sah atau merusak query.
- Server error tidak menyebabkan crash.

---

## 6.4 Modul Manajemen Arsip

AI agent harus:

1. Buat data dummy sebelum test.
2. Buka detail arsip.
3. Uji edit metadata valid.
4. Uji edit invalid, seperti nomor dokumen kosong dan nominal negatif.
5. Uji hapus data dengan dialog konfirmasi.
6. Uji server error saat update.

Assertion wajib:

- Edit valid berhasil tersimpan.
- Edit invalid ditolak.
- Hapus batal tidak menghapus data.
- Hapus setuju menghapus atau menandai data sesuai desain.
- Server error tidak menghapus data lama.

---

## 6.5 Modul Audit Log

AI agent harus:

1. Login sebagai Arsiparis.
2. Jalankan Create, Update, Delete.
3. Buka atau query audit log.
4. Validasi setiap aktivitas tercatat.

Assertion wajib:

- Log Create berisi user, waktu, aksi, dan ID dokumen.
- Log Update berisi user, waktu, aksi, dan ID dokumen.
- Log Delete berisi user, waktu, aksi, dan ID dokumen.
- Timestamp log tidak boleh kosong.
- User pelaku aksi harus sesuai akun login.

---

## 6.6 Modul Report dan Export

AI agent harus:

1. Login sebagai Kepala Subbag.
2. Seed data laporan.
3. Terapkan filter semester, tahun, dan subbagian.
4. Jalankan ekspor Excel.
5. Jalankan ekspor PDF detail dokumen.
6. Simulasikan storage penuh.
7. Simulasikan permission denied.
8. Jalankan double tap tombol ekspor.

Assertion wajib:

- Filter valid menghasilkan data sesuai parameter.
- Filter invalid ditolak.
- Tanggal awal lebih besar dari tanggal akhir ditolak.
- Excel `.xlsx` berhasil dibuat dan dapat dibuka.
- PDF berhasil dibuat dan tidak kosong.
- Storage penuh menghasilkan error.
- Permission denied menghasilkan error.
- Double tap tidak menghasilkan file duplikat.

---

# 7. Instruksi OCR Semi-Automation

AI agent harus:

1. Siapkan gambar fixture.
2. Jalankan OCR untuk gambar jernih.
3. Jalankan OCR untuk gambar blank, gelap, buram.
4. Validasi data masuk staging.
5. Validasi data tidak langsung masuk produksi sebelum verifikasi.
6. Uji koreksi manual di staging.
7. Simpan hasil koreksi ke database produksi.

Assertion wajib:

- Gambar jernih menghasilkan teks.
- Gambar buruk tidak membuat aplikasi crash.
- Hasil OCR kosong harus menampilkan field kosong atau pesan input manual.
- Format OCR tidak cocok harus meminta verifikasi manual.
- Data staging dapat disimpan menjadi arsip final.

---

# 8. Instruksi Permission Testing

Untuk permission kamera dan storage:

1. Jalankan test dengan permission granted.
2. Jalankan test dengan permission denied.
3. Jika menggunakan Android emulator, gunakan perintah ADB sesuai package aplikasi.

Contoh:

```bash
adb shell pm grant <package.name> android.permission.CAMERA
adb shell pm revoke <package.name> android.permission.CAMERA
```

Untuk Android versi baru, sesuaikan permission storage/media:

```bash
adb shell pm grant <package.name> android.permission.READ_MEDIA_IMAGES
adb shell pm revoke <package.name> android.permission.READ_MEDIA_IMAGES
```

Expected behavior:

- Permission granted: fitur berjalan.
- Permission denied: aplikasi menampilkan pesan yang jelas dan tidak crash.

---

# 9. Instruksi Network Testing

AI agent harus menguji kondisi berikut:

| Kondisi | Cara Uji |
|---|---|
| Online normal | Gunakan koneksi aktif |
| Offline | Disable network emulator/device |
| Slow network | Gunakan network throttling jika tersedia |
| Server error | Mock API response 500 |
| Timeout | Mock API delay melebihi batas |

Expected behavior:

- Offline menyimpan lokal jika user sudah login.
- Server error menampilkan pesan gagal.
- Timeout tidak menyebabkan aplikasi freeze.
- Sync berjalan ketika koneksi kembali aktif.

---

# 10. Instruksi File Output Testing

## 10.1 Excel

AI agent harus:

1. Jalankan ekspor Excel.
2. Pastikan file `.xlsx` terbentuk.
3. Pastikan ukuran file lebih dari 0 byte.
4. Buka file menggunakan library pembaca Excel.
5. Validasi header dan jumlah baris.

Expected header minimal:

- Nomor Dokumen
- Tahun Anggaran
- Subbagian
- Jenis Dokumen
- Nominal
- Tanggal Dokumen
- Lokasi

## 10.2 PDF

AI agent harus:

1. Jalankan ekspor PDF.
2. Pastikan file `.pdf` terbentuk.
3. Pastikan ukuran file lebih dari 0 byte.
4. Ekstrak teks PDF jika memungkinkan.
5. Validasi nomor dokumen dan metadata utama muncul.

Catatan: validasi kerapian PDF tetap perlu manual review pada Prioritas 3.

---

# 11. Format Laporan Hasil Test

AI agent harus menghasilkan laporan dengan format berikut:

| Field | Keterangan |
|---|---|
| Test Case ID | ID test case |
| Priority | 1, 2, atau 3 |
| Execution Type | Automated, Semi-Automated, Manual |
| Status | Passed, Failed, Blocked, Need Review |
| Environment | Emulator/Real Device, OS version |
| Build Version | Versi aplikasi |
| Start Time | Waktu mulai |
| End Time | Waktu selesai |
| Evidence | Screenshot, logcat, file output |
| Notes | Catatan tambahan |

Contoh hasil:

```json
{
  "test_case_id": "INP_003",
  "priority": 1,
  "execution_type": "Automated",
  "status": "Passed",
  "environment": "Android Emulator API 35",
  "build_version": "1.0.0-debug",
  "evidence": "screenshots/INP_003_pass.png",
  "notes": "Nominal -1 and 0 rejected; nominal 1 accepted."
}
```

---

# 12. Exit Criteria

Automation dianggap selesai jika:

1. Semua test case Prioritas 1 sudah dijalankan otomatis.
2. Minimal 90% Prioritas 1 berstatus Passed.
3. Tidak ada bug critical pada input, sync, search, management, audit, dan export.
4. Semua Prioritas 2 minimal memiliki bukti semi-otomatis atau checklist manual.
5. Semua Prioritas 3 memiliki checklist exploratory.
6. Semua bug Failed memiliki evidence yang jelas.

---

# 13. Bug Severity

Gunakan klasifikasi berikut:

| Severity | Definisi |
|---|---|
| Critical | Aplikasi crash, data hilang, sync merusak data, keamanan bocor |
| High | Fitur utama gagal, validasi penting tidak berjalan |
| Medium | Fitur berjalan tetapi hasil salah sebagian |
| Low | Masalah tampilan, typo, pesan kurang jelas |

---

# 14. Urutan Eksekusi yang Disarankan

1. Setup database dan akun dummy.
2. Jalankan unit test validasi input.
3. Jalankan UI test input manual.
4. Jalankan test Room DB offline.
5. Jalankan test sync Supabase.
6. Jalankan search dan filter.
7. Jalankan manajemen arsip.
8. Jalankan audit log.
9. Jalankan export Excel dan PDF.
10. Jalankan OCR semi-automation.
11. Jalankan manual exploratory.
12. Generate laporan akhir.

---

# 15. Larangan untuk AI Agent

AI agent tidak boleh:

1. Menggunakan data produksi asli.
2. Menghapus data selain data dummy testing.
3. Mengabaikan failed test tanpa evidence.
4. Menandai Passed jika assertion belum diverifikasi.
5. Menguji OCR hanya dengan satu jenis gambar.
6. Menganggap PDF valid hanya karena file terbentuk.
7. Menganggap sync berhasil tanpa validasi status lokal dan server.
8. Mengabaikan skenario permission denied.
9. Mengabaikan skenario double tap.
10. Mengabaikan skenario storage penuh.
