# Detail Test Cases Aplikasi Arsip Keuangan Mobile

Dokumen ini berisi detail test cases berdasarkan prioritas otomatisasi 1–3.  
Format mencakup: **Test Case ID, Prioritas, Teknik Black Box, Pre-condition, Test Data, Test Steps, Expected Result**.

## Catatan Umum

- Role utama: **Arsiparis**.
- Role laporan/ekspor: **Kepala Subbag**.
- Database lokal: **Room DB**.
- Database server: **Supabase**.
- OCR menggunakan gambar arsip sebagai input.
- OFF_003 dan OFF_005 memiliki deskripsi yang sama. Dalam dokumen ini:
  - **OFF_003** digunakan untuk pencarian offline berdasarkan keyword.
  - **OFF_005** digunakan untuk pencarian offline berdasarkan filter/cache lokal.

---

# PRIORITAS 1 — Wajib Diotomatisasi

## A. Input Manual Dokumen Keuangan

| Test Case ID | Prioritas | Teknik Black Box | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---:|---|---|---|---|---|
| INP_001 | 1 | Use Case, Equivalence Partitioning | Arsiparis login, halaman input manual tersedia | No dokumen: `DOC-001`, Tahun: `2026`, Subbagian: `Keuangan`, Nominal: `1000000`, Lokasi: `Gedung A/Ruang 1/Rak 2/Box 3` | 1. Buka menu input dokumen. 2. Isi semua field wajib dengan data valid. 3. Tekan Simpan. | Data berhasil disimpan, muncul pesan sukses, data tampil pada daftar arsip. |
| INP_002 | 1 | Equivalence Partitioning, Negative Testing | Arsiparis login, halaman input manual tersedia | Field nomor dokumen kosong, field lain valid | 1. Buka form input. 2. Kosongkan salah satu field wajib. 3. Tekan Simpan. | Sistem menolak penyimpanan dan menampilkan pesan validasi field wajib. |
| INP_003 | 1 | BVA, Negative Testing | Arsiparis login, halaman input manual tersedia | Nominal: `-1`, `0`, `1` | 1. Isi form dengan data valid. 2. Masukkan nominal `-1`. 3. Tekan Simpan. 4. Ulangi dengan `0` dan `1`. | Nominal `-1` dan `0` ditolak, nominal `1` diterima jika aturan minimal adalah lebih dari 0. |
| INP_004 | 1 | BVA, Security Input Validation | Arsiparis login, halaman input manual tersedia | Nominal: `1000`, `1a000`, `1000!`, `Rp1000` | 1. Buka field nominal. 2. Masukkan data uji satu per satu. 3. Amati hasil transformasi input. | Field nominal hanya menerima angka atau memfilter karakter non-numerik sesuai aturan CurrencyVisualTransformation. |
| INP_005 | 1 | State Transition, Mobile Configuration Test | Arsiparis login, form sudah berisi sebagian data | Data form parsial: No dokumen `DOC-ROTATE-001`, nominal `500000` | 1. Isi sebagian form. 2. Rotasi layar portrait ke landscape. 3. Kembali ke portrait. | Data yang sudah diketik tetap tersimpan di state form, tidak hilang, aplikasi tidak crash. |
| INP_006 | 1 | Equivalence Partitioning, BVA, Negative Testing | Arsiparis login, halaman input manual tersedia | Nominal: `abc`, `10abc`, `10.000`, `-500`, `@5000` | 1. Buka form input. 2. Isi field nominal dengan data tidak valid. 3. Tekan Simpan. | Sistem menolak nominal berisi huruf/simbol tidak valid dan menampilkan pesan error. |

---

## B. Offline, Room DB, dan Sinkronisasi

| Test Case ID | Prioritas | Teknik Black Box | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---:|---|---|---|---|---|
| OFF_001 | 1 | State Transition, Offline Scenario | Arsiparis sudah login, koneksi internet dimatikan | Data arsip valid `OFF-DOC-001` | 1. Matikan internet. 2. Input data arsip valid. 3. Tekan Simpan. | Data tersimpan di Room DB/lokal, status ditandai pending sync. |
| OFF_002 | 1 | State Transition, Decision Table | Ada data pending sync di Room DB, internet awalnya mati | Data pending `OFF-DOC-001` | 1. Aktifkan internet. 2. Tunggu background sync. 3. Cek status data. | Data tersinkron ke Supabase, status pending berubah menjadi synced. |
| OFF_003 | 1 | Equivalence Partitioning, Offline Search | Arsiparis login, cache Room berisi data, internet mati | Keyword: `SPJ`, `DOC`, `tidakada` | 1. Matikan internet. 2. Buka menu pencarian. 3. Masukkan keyword. | Pencarian menggunakan Room DB dan menampilkan data yang sesuai. Keyword tidak cocok menampilkan empty state. |
| OFF_004 | 1 | State Transition, Offline Edit | Arsiparis login, data arsip tersedia di Room DB, internet mati | Edit nominal dari `1000000` ke `1200000` | 1. Matikan internet. 2. Buka detail arsip. 3. Ubah metadata. 4. Simpan. | Perubahan tersimpan lokal dan diberi status pending update/sync. |
| OFF_005 | 1 | Equivalence Partitioning, Offline Filter | Arsiparis login, cache lokal tersedia, internet mati | Filter tahun `2026`, subbagian `Keuangan` | 1. Matikan internet. 2. Buka filter pencarian. 3. Pilih tahun dan subbagian. 4. Terapkan filter. | Data dari Room DB tampil sesuai filter lokal. |
| OFF_006 | 1 | Equivalence Partitioning, Negative Offline Scenario | User belum login, internet mati | Username/password valid | 1. Logout dari aplikasi. 2. Matikan internet. 3. Input kredensial valid. 4. Tekan Login. | Sistem menolak login offline dan menampilkan pesan membutuhkan koneksi internet. |
| OFF_007 | 1 | BVA, Error Guessing | Storage perangkat disimulasikan penuh | Data arsip valid | 1. Simulasikan storage penuh. 2. Input data. 3. Tekan Simpan. | Sistem menolak penyimpanan lokal dan menampilkan pesan storage penuh tanpa crash. |
| OFF_008 | 1 | Decision Table, Conflict Handling | Ada data lokal dan data server dengan nomor dokumen sama | Nomor dokumen `DUP-001` | 1. Buat data lokal dengan nomor `DUP-001`. 2. Siapkan data server dengan nomor sama. 3. Jalankan sync. | Sistem mendeteksi konflik nomor dokumen dan menerapkan aturan konflik, misalnya menolak, merge, atau meminta resolusi. |
| OFF_009 | 1 | Error Guessing, State Transition | Arsiparis login, koneksi lambat atau offline | Data valid `QUEUE-001` | 1. Isi form valid. 2. Tekan tombol Simpan cepat berulang. 3. Cek queue lokal. | Queue tidak duplikat, hanya satu data tersimpan atau tombol terkunci setelah tap pertama. |

---

## C. Search dan Filtering

| Test Case ID | Prioritas | Teknik Black Box | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---:|---|---|---|---|---|
| SCH_001 | 1 | Performance, Equivalence Partitioning | Data arsip tersedia minimal 100 data | Keyword valid `SPJ` | 1. Buka menu pencarian. 2. Masukkan keyword. 3. Catat waktu respons. | Data sesuai keyword tampil dalam waktu kurang dari 2 detik. |
| SCH_002 | 1 | Pairwise, Decision Table | Data arsip tersedia dengan variasi tahun dan subbagian | Tahun `2026`, Subbagian `Keuangan` | 1. Buka filter. 2. Pilih tahun dan subbagian. 3. Terapkan filter. | Hanya data yang sesuai kombinasi filter tampil. |
| SCH_003 | 1 | State Transition, Offline Scenario | Cache Room tersedia, internet mati | Keyword `SPJ` | 1. Matikan internet. 2. Jalankan pencarian. | Sistem mencari dari database lokal dan memberi indikator mode offline jika tersedia. |
| SCH_004 | 1 | Error Guessing | Server pencarian disimulasikan error 500 | Keyword `SPJ` | 1. Aktifkan mode online. 2. Simulasikan server error. 3. Jalankan pencarian. | Sistem menampilkan pesan gagal memuat data, tidak crash, dan dapat retry. |
| SCH_005 | 1 | Security Black Box, Error Guessing | Arsiparis login, search box tersedia | `' OR '1'='1`, `DROP TABLE arsip;`, `%_%` | 1. Masukkan payload ke kolom pencarian. 2. Tekan Cari. | Sistem tidak mengeksekusi injeksi, data tetap aman, hasil ditolak atau ditampilkan secara aman. |
| SCH_006 | 1 | Equivalence Partitioning | Data arsip tersedia | Keyword `zzzznotfound` | 1. Masukkan keyword yang tidak cocok. 2. Tekan Cari. | Sistem menampilkan empty state/pesan data tidak ditemukan. |

---

## D. Manajemen Arsip

| Test Case ID | Prioritas | Teknik Black Box | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---:|---|---|---|---|---|
| MNG_001 | 1 | Use Case, Equivalence Partitioning | Arsiparis login, data arsip tersedia | Ubah subbagian ke `Umum`, nominal `1500000` | 1. Buka detail arsip. 2. Tekan Edit. 3. Ubah metadata valid. 4. Simpan. | Metadata berhasil diperbarui dan perubahan tampil di detail arsip. |
| MNG_002 | 1 | Use Case, Decision Testing | Arsiparis login, data arsip tersedia | Data `DOC-DELETE-001` | 1. Buka detail arsip. 2. Tekan Hapus. 3. Pilih batal. 4. Ulangi dan pilih setuju. | Jika batal, data tetap ada. Jika setuju, data terhapus atau ditandai deleted sesuai desain sistem. |
| MNG_003 | 1 | Equivalence Partitioning, Negative Testing | Arsiparis login, data arsip tersedia | Nomor dokumen kosong | 1. Buka edit arsip. 2. Kosongkan nomor dokumen. 3. Simpan. | Sistem menolak update dan menampilkan validasi nomor dokumen wajib. |
| MNG_004 | 1 | BVA, Negative Testing | Arsiparis login, data arsip tersedia | Nominal `-1`, `0`, `1`, nilai maksimum | 1. Buka edit arsip. 2. Masukkan nominal batas. 3. Simpan. | Nilai negatif dan nol ditolak, nilai valid diterima sesuai aturan bisnis. |
| MNG_005 | 1 | Error Guessing | Arsiparis login, server update disimulasikan error | Data edit valid | 1. Buka edit arsip. 2. Ubah data valid. 3. Simulasikan server error. 4. Simpan. | Sistem menampilkan pesan gagal update dan tidak merusak data lama. |

---

## E. Audit Log

| Test Case ID | Prioritas | Teknik Black Box | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---:|---|---|---|---|---|
| AUD_001 | 1 | Integration, Use Case | Arsiparis login, audit log aktif | Data baru `AUD-CREATE-001` | 1. Tambah data arsip baru. 2. Buka audit log. | Aktivitas Create tercatat dengan user, waktu, dan data terkait. |
| AUD_002 | 1 | Integration, Use Case | Arsiparis login, data arsip tersedia | Ubah nominal `1000000` ke `1100000` | 1. Edit data arsip. 2. Simpan. 3. Buka audit log. | Aktivitas Update tercatat dengan user, waktu, data lama, dan data baru jika tersedia. |
| AUD_003 | 1 | Integration, Use Case | Arsiparis login, data arsip tersedia | Data `AUD-DELETE-001` | 1. Hapus data arsip. 2. Buka audit log. | Aktivitas Delete tercatat dengan user, waktu, dan ID dokumen yang dihapus. |

---

## F. Export dan Report

| Test Case ID | Prioritas | Teknik Black Box | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---:|---|---|---|---|---|
| EXP_001 | 1 | Decision Table, Pairwise | Kepala Subbag login, data laporan tersedia | Semester `1`, Tahun `2026`, Subbagian `Keuangan` | 1. Buka halaman ekspor/filter. 2. Pilih semester, tahun, subbagian. 3. Terapkan filter. | Data yang tampil sesuai filter. |
| EXP_002 | 1 | Equivalence Partitioning, Negative Testing | Kepala Subbag login | Semester kosong/tidak valid, tahun tidak valid | 1. Buka filter. 2. Isi parameter tidak valid. 3. Terapkan. | Sistem menolak filter tidak valid dan menampilkan pesan validasi. |
| EXP_003 | 1 | BVA, Negative Testing | Kepala Subbag login | Tanggal awal `2026-12-31`, tanggal akhir `2026-01-01` | 1. Buka filter rentang tanggal. 2. Isi tanggal awal lebih besar dari akhir. 3. Terapkan. | Sistem menolak rentang tanggal dan menampilkan pesan error. |
| EXP_004 | 1 | Decision Table, Pairwise | Data tersedia | Tahun `2026`, Subbagian `Keuangan` | 1. Terapkan filter tahun dan subbagian. 2. Tekan Ekspor Excel. | File Excel berisi data sesuai filter. |
| EXP_005 | 1 | Equivalence Partitioning, Negative Testing | Tidak ada data untuk filter tertentu | Tahun `1999`, Subbagian `TidakAda` | 1. Terapkan filter yang menghasilkan nol data. 2. Tekan Ekspor. | Sistem menampilkan pesan tidak ada data atau menghasilkan file kosong sesuai aturan. |
| EXP_006 | 1 | BVA, Error Guessing | Storage perangkat penuh | Data laporan valid | 1. Simulasikan storage penuh. 2. Tekan Ekspor Excel. | Ekspor gagal dengan pesan storage penuh, aplikasi tidak crash. |
| EXP_007 | 1 | Error Guessing, Permission Testing | Permission storage ditolak | Data laporan valid | 1. Tolak izin penyimpanan. 2. Tekan Ekspor. | Sistem meminta izin atau menampilkan pesan akses ditolak. |
| EXP_008 | 1 | Use Case, File Output Test | Data laporan tersedia, storage cukup | Filter tahun `2026` | 1. Terapkan filter. 2. Tekan Ekspor Excel. 3. Buka file hasil. | File `.xlsx` berhasil dibuat di penyimpanan internal dan dapat dibuka. |
| EXP_009 | 1 | Use Case, File Output Test | Data detail dokumen tersedia | Dokumen `DOC-PDF-001` | 1. Buka detail dokumen. 2. Tekan Cetak/Ekspor PDF. 3. Buka file PDF. | PDF berhasil dibuat, struktur data benar, siap cetak. |
| EXP_010 | 1 | BVA, Error Guessing | File dengan nama sama sudah ada | Nama file `arsip_2026.xlsx` | 1. Buat file ekspor pertama. 2. Ulangi ekspor dengan nama sama. | Sistem menangani duplikasi nama file, misalnya overwrite dengan konfirmasi atau membuat nama baru. |
| EXP_011 | 1 | Error Guessing, State Transition | Data ekspor tersedia | Double tap tombol ekspor | 1. Tekan tombol ekspor dua kali sangat cepat. 2. Cek jumlah file/output. | Hanya satu proses ekspor berjalan, tidak ada file duplikat. |

---

# PRIORITAS 2 — Semi-Otomatis

| Test Case ID | Prioritas | Teknik Black Box | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---:|---|---|---|---|---|
| OCR_001 | 2 | Use Case, Performance | Arsiparis login, kamera tersedia | Foto arsip jernih | 1. Buka fitur OCR kamera. 2. Ambil foto arsip jernih. 3. Jalankan OCR. | Teks mentah berhasil diekstrak dan terbaca jelas. |
| OCR_002 | 2 | Use Case | Arsiparis login, galeri tersedia | Gambar arsip jernih dari galeri | 1. Buka OCR dari galeri. 2. Pilih gambar. 3. Jalankan OCR. | Teks mentah berhasil diekstrak dari gambar galeri. |
| OCR_003 | 2 | State Transition | Hasil OCR tersedia | Teks OCR valid | 1. Jalankan OCR. 2. Lanjutkan ke halaman staging. | Data hasil OCR masuk halaman staging, belum langsung masuk database produksi. |
| OCR_004 | 2 | State Transition | Data staging tersedia | Data staging valid | 1. Buka staging. 2. Tekan Tambahkan sebagai Arsip. | Data staging berhasil menjadi arsip baru. |
| OCR_005 | 2 | Error Guessing, Permission Testing | Permission kamera belum diberikan | Permission kamera ditolak | 1. Buka fitur kamera. 2. Tolak izin kamera. | Sistem menampilkan pesan izin kamera dibutuhkan, tidak crash. |
| OCR_006 | 2 | Error Guessing, Negative Testing | Arsiparis login | Gambar blank, gelap, buram | 1. Jalankan OCR pada gambar blank. 2. Ulangi untuk gambar gelap dan buram. | Sistem menampilkan pesan gagal/teks tidak terbaca secara logis, tidak crash. |
| OCR_007 | 2 | Equivalence Partitioning | Hasil OCR kosong | Gambar tanpa teks | 1. Jalankan OCR pada gambar tanpa teks. 2. Masuk ke form input. | Field form kosong atau ditandai perlu diisi manual. |
| OCR_008 | 2 | Equivalence Partitioning | Hasil OCR tersedia tetapi format tidak sesuai | Teks bebas tanpa nomor/tanggal/nominal | 1. Jalankan OCR. 2. Sistem mencoba mapping ke field. | Sistem tidak salah mapping dan memberi pesan/verifikasi manual. |
| OCR_009 | 2 | BVA, Error Guessing | Arsiparis login, galeri tersedia | File terlalu besar, `.bmp`, `.heic`, file rusak | 1. Pilih file tidak didukung atau terlalu besar. 2. Jalankan OCR. | Sistem menolak file dengan pesan yang jelas. |
| OCR_010 | 2 | State Transition, Mobile Robustness | OCR sedang berjalan | Proses OCR aktif | 1. Jalankan OCR. 2. Pindahkan aplikasi ke background. 3. Buka kembali aplikasi. | Aplikasi tidak crash dan status proses ditangani dengan jelas. |
| OCR_011 | 2 | Use Case, State Transition | Data staging tersedia | Teks OCR sebagian salah | 1. Buka staging. 2. Koreksi field salah. 3. Simpan ke database produksi. | Data terkoreksi berhasil disimpan sebagai arsip final. |
| TRK_001 | 2 | Use Case, Visual Validation | Data lokasi fisik tersedia | Gedung A, Ruang 1, Rak 2, Box 3 | 1. Buka detail arsip. 2. Lihat bagian lokasi fisik. | Lokasi fisik tampil lengkap dan presisi. |
| REP_001 | 2 | Use Case, Visual Validation | Kepala Subbag login, data statistik tersedia | Data arsip beberapa periode | 1. Buka dashboard rekapitulasi. 2. Amati grafik statistik. | Grafik tampil sesuai data dan mudah dibaca. |

---

# PRIORITAS 3 — Manual Exploratory

| Test Case ID | Prioritas | Teknik Black Box | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---:|---|---|---|---|---|
| TRK_002 | 3 | Use Case | Arsiparis login, data lokasi tersedia | Lokasi lama: Rak 1, lokasi baru: Rak 2 | 1. Buka detail lokasi arsip. 2. Tekan Edit Lokasi. 3. Ubah lokasi. 4. Simpan. | Lokasi penempatan arsip berhasil berubah. |
| TRK_003 | 3 | BVA, Error Guessing | Box memiliki kapasitas maksimum | Box penuh | 1. Pilih box yang sudah penuh. 2. Tambahkan arsip ke box tersebut. | Sistem menolak atau memberi peringatan box penuh. |
| TRK_004 | 3 | Equivalence Partitioning | Arsiparis login | Gedung/Ruang/Rak/Box kosong | 1. Buka form lokasi. 2. Kosongkan lokasi. 3. Simpan. | Sistem menolak penyimpanan atau menandai lokasi belum lengkap. |
| TRK_005 | 3 | Use Case | Arsiparis login, arsip tersedia | Lampiran `.jpg` atau `.pdf` | 1. Buka detail arsip. 2. Tekan Tambah Lampiran. 3. Pilih file. 4. Simpan. | Lampiran berhasil ditambahkan ke arsip. |
| TRK_006 | 3 | State Transition | Arsip berpindah lokasi | Lokasi awal dan lokasi baru | 1. Ubah lokasi arsip. 2. Buka riwayat perpindahan. | Riwayat perpindahan lokasi tercatat dengan waktu dan user. |
| OCR_VISUAL_001 | 3 | Exploratory Testing | Real device tersedia | Foto arsip dari sudut miring, bayangan, cahaya rendah | 1. Jalankan OCR dengan variasi kondisi nyata. 2. Bandingkan hasil teks. | Sistem tetap stabil dan memberi hasil/pesan yang dapat dipahami pengguna. |
| PDF_VISUAL_001 | 3 | Exploratory Testing | File PDF hasil ekspor tersedia | PDF detail arsip | 1. Buka PDF di viewer. 2. Cek margin, tabel, teks, dan keterbacaan. | PDF rapi, tidak terpotong, dan layak cetak. |
| DASH_VISUAL_001 | 3 | Exploratory Testing | Dashboard tersedia | Data grafik banyak/sedikit | 1. Buka dashboard. 2. Uji tampilan pada layar kecil/besar. | Grafik tetap terbaca dan layout tidak rusak. |

---

# Ringkasan Teknik Black Box per Area

| Area | Teknik Utama |
|---|---|
| Input Manual | Equivalence Partitioning, BVA, Negative Testing |
| OCR | Use Case, State Transition, Error Guessing |
| Offline & Sync | State Transition, Decision Table, Conflict Testing |
| Search & Filter | Equivalence Partitioning, Pairwise, Security Testing |
| Tracking Lokasi | Use Case, State Transition, BVA |
| Manajemen Arsip | Equivalence Partitioning, BVA, Error Guessing |
| Audit Log | Integration, Use Case |
| Report & Export | Decision Table, BVA, File Output Testing |
