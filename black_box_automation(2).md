# Test Cases Black-Box yang Disesuaikan dengan Repository Aplikasi Mobile BPKPAD Balangan

## Kesimpulan Kesesuaian

Berdasarkan struktur repository aplikasi mobile:

- Aplikasi menggunakan Kotlin, Jetpack Compose, MVVM/Clean Architecture.
- Login menggunakan Supabase Auth melalui `LoginViewModel`.
- Form input arsip menggunakan `RapidInputScreen` dan `RapidInputViewModel`.
- Penyimpanan lokal menggunakan Room DB.
- Sinkronisasi menggunakan Supabase PostgREST.
- OCR menggunakan CameraX + ML Kit, lalu hasil diparsing ke metadata.
- Export yang tampak tersedia adalah Excel `.xlsx`.
- Staging dokumen tersedia secara lokal sebelum bulk upload.
- Audit log disebut ditangani oleh Supabase trigger, bukan langsung dari kode Android.
- RBAC Arsiparis/Kepala Subbag belum tampak jelas di kode aplikasi.
- Ekspor PDF belum tampak jelas di kode aplikasi.
- Filter semester/subbagian belum tampak sebagai filter utama. Filter yang terlihat dominan adalah tahun dan keyword pencarian nomor dokumen.

Karena itu, sebagian test case lama perlu disesuaikan agar tidak menguji fitur yang belum ada di aplikasi saat ini.

---

# A. Test Cases Login & Session

| Test Case ID | Status Kesesuaian | Teknik Black Box | Prioritas Otomatisasi | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---|---|---:|---|---|---|---|
| LGN_001 | Sesuai, tetapi istilah role disesuaikan menjadi admin/user valid | Positive Testing, Use Case | 1 | Aplikasi terbuka di halaman login, internet aktif | Email valid, password valid | 1. Isi email valid. 2. Isi password valid. 3. Tekan Log In. | Login berhasil dan user diarahkan ke Home Dashboard. |
| LGN_002 | Belum sesuai jika menguji Kepala Subbag, karena RBAC belum tampak di kode | RBAC Testing | 2 setelah RBAC dibuat | Role Kepala Subbag sudah tersedia di backend dan aplikasi membaca role | Akun Kepala Subbag valid | 1. Login sebagai Kepala Subbag. 2. Amati menu bawah dan fitur pengelolaan. | Kepala Subbag hanya melihat dashboard/laporan, menu tambah/pengelolaan tidak tampil. Saat ini kemungkinan belum bisa otomatis karena logic role belum ada. |
| LGN_003 | Sesuai | Negative Testing, Equivalence Partitioning | 1 | Internet aktif, halaman login terbuka | Email salah/password salah | 1. Isi email atau password salah. 2. Tekan Log In. | Login ditolak, muncul pesan “Email atau password yang Anda masukkan salah.” |
| LGN_004 | Sesuai | Network Failure Testing | 1 | Halaman login terbuka, internet dimatikan | Email dan password valid | 1. Matikan koneksi. 2. Isi kredensial valid. 3. Tekan Log In. | Login gagal, muncul pesan koneksi terputus/periksa jaringan. |
| LGN_005 | Perlu tambahan implementasi session guard | State Transition, Security Negative Test | 2 | User sudah login, token disimulasikan expired | Session/token expired | 1. Login. 2. Simulasikan token expired. 3. Pindahkan aplikasi ke background. 4. Buka lagi aplikasi. 5. Akses fitur data. | Sistem harus meminta login ulang atau menolak akses data. Saat ini belum tampak session expiry guard eksplisit. |
| LGN_006 | Tambahan yang disarankan | Negative Testing, EP | 1 | Halaman login terbuka | Email kosong/password kosong | 1. Kosongkan email atau password. 2. Tekan Log In. | Sistem menolak login dan menampilkan pesan “Email dan password tidak boleh kosong.” |
| LGN_007 | Tambahan yang disarankan | UI State Testing | 1 | Halaman login terbuka, internet aktif/lambat | Kredensial valid | 1. Isi kredensial. 2. Tekan Log In. 3. Amati tombol login saat loading. | Tombol login disabled saat loading dan progress indicator tampil. |

---

# B. Test Cases RBAC yang Disarankan Setelah Implementasi Role

Test case berikut baru benar-benar valid jika aplikasi sudah:
1. Mengambil role user dari Supabase, misalnya `profiles.role`.
2. Menyimpan role pada session/app state.
3. Menggunakan role untuk mengatur navigation/menu/action.
4. Membatasi akses route secara internal, bukan hanya menyembunyikan tombol.

| Test Case ID | Teknik Black Box | Prioritas | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---|---:|---|---|---|---|
| RBAC_001 | Authorization Testing | 1 | Akun Arsiparis tersedia | Role `ARSIPARIS` | 1. Login sebagai Arsiparis. 2. Buka Home. 3. Cek menu Archive, Add/Staging, Scan, Edit, Delete. | Arsiparis dapat mengakses fitur pengelolaan arsip. |
| RBAC_002 | Authorization Testing | 1 | Akun Kepala Subbag tersedia | Role `KEPALA_SUBBAG` | 1. Login sebagai Kepala Subbag. 2. Buka Home. 3. Cek menu bawah. | Kepala Subbag tidak melihat menu Add/Staging/Scan/Edit/Delete. |
| RBAC_003 | Negative Authorization Testing | 1 | Kepala Subbag login | Deep link/route `rapid_input`, `scan`, `edit_archive` | 1. Login Kepala Subbag. 2. Paksa akses route pengelolaan. | Akses ditolak atau diarahkan ke dashboard laporan. |
| RBAC_004 | Authorization Testing | 1 | Arsiparis login | Route analytics | 1. Login Arsiparis. 2. Buka analytics. | Jika aturan sistem membatasi analytics hanya Kepala Subbag, akses ditolak. Jika boleh, tampil normal sesuai aturan bisnis. |
| RBAC_005 | Session Role Consistency | 2 | User login, app background/foreground | Role user valid | 1. Login. 2. Pindah app ke background. 3. Buka kembali. | Role tetap konsisten, menu tidak berubah salah. |

---

# C. Test Cases Input Manual / Rapid Input yang Sesuai Repository

| Test Case ID | Status Kesesuaian | Teknik Black Box | Prioritas | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---|---|---:|---|---|---|---|
| INP_001 | Sesuai, tetapi alur dimulai dari box/staging | Use Case, EP | 1 | User login, masuk Staging/Rapid Input | Gudang A, Rak 1, Box 1, Tahun 2026, No Dokumen SP2D-001, Uraian valid | 1. Buat/isi konteks box. 2. Isi data dokumen valid. 3. Tambah ke staging. | Dokumen berhasil masuk staging lokal. |
| INP_002 | Sesuai | Negative Testing, EP | 1 | User berada di Rapid Input | Nomor dokumen kosong atau uraian kosong | 1. Isi box valid. 2. Kosongkan nomor dokumen/uraian. 3. Tekan tambah ke staging. | Sistem menampilkan validasi field wajib. |
| INP_003 | Perlu disesuaikan: nominal tidak divalidasi wajib > 0 di kode saat ini | BVA | 2 | Rapid Input terbuka | Nominal `-1`, `0`, `1` | 1. Input nominal batas. 2. Tambah ke staging. | Rekomendasi expected: `-1` dan `0` ditolak. Saat ini kode memakai `toDoubleOrNull`, sehingga perlu validasi tambahan jika aturan bisnis melarang nol/negatif. |
| INP_004 | Sesuai sebagian | Input Filtering, BVA | 1 | Rapid Input terbuka | Nominal `1a000`, `Rp1000`, `1000!` | 1. Isi nominal dengan karakter campuran. 2. Amati field. | Karakter non-numerik harus difilter oleh input/currency transformation atau tidak boleh tersimpan sebagai nominal valid. |
| INP_005 | Sesuai | State Retention | 1 | Form sudah terisi sebagian | No Dokumen `ROTATE-001`, Uraian `uji rotasi` | 1. Isi form. 2. Rotasi layar. 3. Cek field. | State form tidak hilang. |
| INP_006 | Sesuai sebagian | Negative Testing | 1 | Rapid Input terbuka | Nominal `abc`, `10abc`, `@5000` | 1. Isi nominal invalid. 2. Tambah ke staging. | Sistem tidak boleh menyimpan nominal invalid sebagai angka. Idealnya tampil pesan validasi. |
| INP_007 | Tambahan sesuai repository | BVA, EP | 1 | Rapid Input terbuka | Copy type ORIGINAL, copy count diubah | 1. Pilih ORIGINAL. 2. Cek field jumlah salinan. | Copy count terkunci/default `1`. |
| INP_008 | Tambahan sesuai repository | BVA | 1 | Rapid Input terbuka | Copy type COPY, copy count `0`, `1`, `2` | 1. Pilih COPY. 2. Isi copy count. 3. Tambah ke staging. | Copy count `0` ditolak, `1` dan `2` diterima. |
| INP_009 | Tambahan sesuai repository | Duplicate Testing | 1 | Data dokumen dengan nomor sama sudah ada | Nomor dokumen sama, copy type sama | 1. Input dokumen dengan nomor dan copy type sama. 2. Tambah ke staging/upload. | Sistem menolak duplikasi nomor dokumen dengan copy type yang sama. |
| INP_010 | Tambahan sesuai repository | Duplicate Rule Testing | 1 | Data dokumen nomor sama sudah ada sebagai ORIGINAL | Nomor sama, copy type COPY | 1. Input nomor dokumen sama tetapi copy type berbeda. | Sistem mengizinkan atau memberi warning sesuai aturan bahwa duplikasi boleh jika status original/copy berbeda. |
| INP_011 | Tambahan sesuai repository | Decision Table | 1 | Rapid Input terbuka | SP2D + Auto Bundle aktif + No SPM valid + SPJ description valid | 1. Pilih SP2D. 2. Aktifkan Auto Bundle. 3. Isi nomor SPM dan deskripsi SPJ. 4. Tambah ke staging. | Sistem membuat dokumen bundle SP2D, SPM, dan SPJ dalam satu bundle/staging. |
| INP_012 | Tambahan sesuai repository | Negative Testing | 1 | Rapid Input terbuka | Auto Bundle aktif, nomor SPM kosong | 1. Aktifkan Auto Bundle. 2. Kosongkan nomor SPM. 3. Tambah ke staging. | Sistem menolak dan menampilkan validasi nomor SPM wajib. |
| BOX_001 | Tambahan sesuai repository | EP | 1 | User masuk create box/staging | Gudang/Ruang, Rak, Box, Tahun valid | 1. Isi konteks box lengkap. 2. Konfirmasi. | Box session terbentuk dan user masuk Rapid Input. |
| BOX_002 | Tambahan sesuai repository | Negative Testing | 1 | User masuk create box/staging | Gudang kosong/Rak kosong/Box kosong | 1. Kosongkan salah satu field box. 2. Konfirmasi. | Sistem menampilkan validasi field lokasi wajib. |
| BOX_003 | Tambahan sesuai repository | BVA | 1 | User masuk create box/staging | Tahun `26`, `2026`, `abcd` | 1. Isi tahun tidak valid dan valid. 2. Konfirmasi. | Tahun harus 4 digit valid. |

---

# D. Test Cases OCR yang Sesuai Repository

| Test Case ID | Status Kesesuaian | Teknik Black Box | Prioritas | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---|---|---:|---|---|---|---|
| OCR_001 | Sesuai | Use Case, Performance | 2 | Kamera tersedia, permission granted | Foto dokumen jernih | 1. Buka Scan. 2. Ambil foto. 3. Tunggu OCR dan AI parser. | Hasil OCR diparsing menjadi metadata dan dikirim ke Rapid Input. |
| OCR_002 | Belum sesuai jika dari galeri, karena kode ScanScreen yang terlihat hanya kamera | Use Case | 3 setelah fitur galeri dibuat | Galeri picker tersedia | Gambar dari galeri | 1. Pilih gambar dari galeri. 2. Jalankan OCR. | Saat ini perlu ditambahkan fitur galeri dulu. |
| OCR_003 | Sesuai konsep, tetapi staging terjadi setelah hasil masuk Rapid Input | State Transition | 2 | OCR sukses dan kembali ke form | Metadata OCR valid | 1. Jalankan OCR. 2. Cek field Rapid Input. 3. Tekan tambah ke staging. | Field terisi otomatis, lalu data masuk staging setelah user konfirmasi. |
| OCR_004 | Sesuai | State Transition | 2 | Data staging tersedia | Data hasil OCR sudah dikoreksi | 1. Upload/confirm staging box. | Data staging masuk archive utama. |
| OCR_005 | Sesuai | Permission Testing | 1 | Permission kamera belum diberikan | Permission denied | 1. Buka Scan. 2. Tolak izin kamera. | Muncul pesan kamera diperlukan, aplikasi tidak crash. |
| OCR_006 | Sesuai | Negative Testing | 2 | Kamera/fixture OCR tersedia | Gambar blank/gelap/buram | 1. Jalankan OCR pada gambar buruk. | Sistem menampilkan error/no text detected dan tidak crash. |
| OCR_007 | Sesuai | EP | 2 | OCR berjalan | Gambar tanpa teks | 1. Jalankan OCR. | Error atau empty result ditangani jelas. |
| OCR_008 | Sesuai | EP | 2 | OCR menghasilkan teks bebas | Teks tanpa nomor/tahun/nominal | 1. Jalankan OCR. 2. Parsing metadata. | Field yang tidak dikenali tetap kosong/harus diverifikasi manual. |
| OCR_009 | Sebagian sesuai | BVA/Error Guessing | 3 | Input image via URI tersedia | File rusak/format tidak didukung | 1. Kirim URI/file invalid ke OCR repository. | Sistem mengembalikan error, tidak crash. |
| OCR_010 | Sesuai untuk real device | State Transition | 2 | OCR sedang berjalan | Proses OCR aktif | 1. Ambil foto. 2. Background app saat loading. 3. Buka lagi. | Aplikasi tidak crash, state loading/sukses/error tertangani. |
| OCR_011 | Sesuai | Use Case | 2 | Hasil OCR masuk Rapid Input | Field sebagian salah | 1. Koreksi field. 2. Tambah ke staging. 3. Upload. | Data terkoreksi tersimpan. |

---

# E. Test Cases Offline, Room, Sync yang Sesuai Repository

| Test Case ID | Status Kesesuaian | Teknik Black Box | Prioritas | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---|---|---:|---|---|---|---|
| OFF_001 | Sesuai | Offline Scenario | 1 | User sudah login, internet mati | Dokumen valid | 1. Matikan internet. 2. Simpan arsip/upload staging. | Data tersimpan di Room dengan syncStatus `DRAFT`. |
| OFF_002 | Sesuai | State Transition | 1 | Ada data `DRAFT`, internet aktif kembali | Data pending | 1. Aktifkan internet. 2. Trigger sync/list refresh. | Data terkirim ke Supabase dan syncStatus menjadi `SYNCED`. |
| OFF_003 | Sesuai | Offline Search | 1 | Cache Room berisi data, internet mati | Keyword nomor dokumen | 1. Matikan internet. 2. Cari dokumen. | Data lokal tampil sesuai keyword. |
| OFF_004 | Sesuai sebagian | Offline Edit | 2 | Data ada di Room, internet mati | Edit metadata | 1. Edit dokumen. 2. Simpan. | Perubahan tersimpan lokal/pending sync. Perlu dipastikan flow edit menggunakan saveArchives dan status DRAFT. |
| OFF_005 | Disesuaikan: filter offline berdasarkan tahun, bukan semester/subbagian | Offline Filter | 1 | Cache Room tersedia | Tahun 2026 | 1. Matikan internet. 2. Filter tahun. | Data lokal sesuai tahun tampil. |
| OFF_006 | Sesuai | Negative Offline Login | 1 | User belum login | Kredensial valid, internet mati | 1. Matikan internet. 2. Login. | Login gagal karena Supabase Auth butuh koneksi. |
| OFF_007 | Sulit otomatis di level app; bisa instrumentasi storage failure | Error Guessing | 3 | Storage penuh disimulasikan | Data valid | 1. Simulasikan storage penuh. 2. Simpan. | Error jelas dan tidak crash. |
| OFF_008 | Sesuai sebagian | Conflict Testing | 2 | Data lokal dan remote punya nomor sama | Nomor dokumen sama | 1. Buat konflik. 2. Sync. | Sistem tidak membuat data korup. Perlu aturan konflik eksplisit. |
| OFF_009 | Sesuai | Double Tap/Queue | 1 | Form valid | Double tap Simpan/Tambah | 1. Tekan tombol tambah/simpan berulang cepat. | Tidak terjadi duplikasi data/queue. |

---

# F. Test Cases Search, Filter, Import/Export Excel

| Test Case ID | Status Kesesuaian | Teknik Black Box | Prioritas | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---|---|---:|---|---|---|---|
| SCH_001 | Sesuai | Performance | 1 | Room berisi banyak data | Keyword nomor dokumen | 1. Masuk Archive List. 2. Pilih tahun. 3. Cari keyword. | Hasil tampil < 2 detik untuk dataset uji. |
| SCH_002 | Disesuaikan: filter gabungan tahun + keyword, bukan tahun + subbagian | Pairwise/Decision Table | 1 | Data tahun bervariasi tersedia | Tahun 2026 + keyword `SP2D` | 1. Pilih tahun. 2. Cari keyword. | Data sesuai tahun dan keyword tampil. |
| SCH_003 | Sesuai | Offline Search | 1 | Cache Room tersedia, internet mati | Keyword valid | 1. Matikan internet. 2. Search. | Search lokal tetap berjalan. |
| SCH_004 | Sesuai sebagian | Error Guessing | 2 | Remote sync/search error disimulasikan | Server error | 1. Simulasikan error Supabase. 2. Refresh/sync. | Error ditampilkan dan data lokal tetap aman. |
| SCH_005 | Sesuai | Security Input | 1 | Search box tersedia | `' OR '1'='1`, `%_%`, `DROP TABLE` | 1. Masukkan payload. 2. Search. | Tidak ada injeksi SQL karena query Room memakai parameter binding; aplikasi tidak crash. |
| SCH_006 | Sesuai | EP | 1 | Data tersedia | Keyword tidak cocok | 1. Search keyword random. | Empty state/tidak ada data tampil. |
| IMP_001 | Tambahan sesuai repository | Use Case | 1 | File picker tersedia | File `.xlsx` valid sesuai format | 1. Klik import. 2. Pilih file xlsx. | Data dari Excel berhasil masuk aplikasi. |
| IMP_002 | Tambahan sesuai repository | Negative Testing | 1 | File picker tersedia | File bukan xlsx/file rusak | 1. Pilih file tidak valid. | Import gagal dengan pesan error. |
| EXP_001 | Disesuaikan: export berdasarkan tahun yang dipilih | Use Case | 1 | Data arsip tersedia | Tahun 2026 | 1. Pilih tahun. 2. Klik export. 3. Buat dokumen xlsx. | File Excel berhasil dibuat. |
| EXP_002 | Disesuaikan: filter invalid semester/subbagian belum relevan | Negative Testing | 3 | Filter semester/subbagian belum ada | - | Tidak dijalankan sampai fitur tersedia. | - |
| EXP_003 | Belum relevan jika rentang tanggal belum tersedia | BVA | 3 | Date range filter belum ada | Tanggal awal > akhir | Tidak dijalankan sampai fitur tersedia. | - |
| EXP_004 | Disesuaikan: export dengan filter tahun | Use Case | 1 | Data tahun 2026 tersedia | Tahun 2026 | 1. Filter tahun. 2. Export Excel. | Excel hanya berisi data tahun terpilih. |
| EXP_005 | Sesuai | EP | 1 | Tahun tanpa data | Tahun 1999 | 1. Pilih tahun tanpa data. 2. Export. | Sistem menampilkan data kosong atau file tanpa baris data sesuai aturan. |
| EXP_006 | Semi-manual | Error Guessing | 3 | Storage failure disimulasikan | Storage penuh | 1. Export. | Error ditampilkan, tidak crash. |
| EXP_007 | Disesuaikan: CreateDocument tidak selalu butuh permission storage langsung | Permission Testing | 3 | Document provider/permission ditolak | User cancel picker | 1. Klik export. 2. Batalkan pemilihan lokasi. | Tidak ada file dibuat dan aplikasi tetap stabil. |
| EXP_008 | Sesuai | File Output Testing | 1 | Data tersedia | Output `.xlsx` | 1. Export Excel. 2. Baca file. | Header dan isi file sesuai format ExcelService. |
| EXP_009 | Belum sesuai jika PDF belum ada | File Output Testing | 3 setelah fitur PDF dibuat | Fitur PDF tersedia | Detail dokumen | 1. Export PDF. | Saat ini perlu implementasi PDF dulu. |
| EXP_010 | Tidak terlalu relevan karena nama file memakai timestamp | Error Guessing | 3 | Export tersedia | Nama file sama | 1. Paksa nama sama jika memungkinkan. | Sistem menangani duplikasi dari document provider. |
| EXP_011 | Sesuai | Double Tap | 1 | Export tersedia | Double tap export | 1. Tekan export cepat berulang. | Tidak terjadi proses ganda/indikator loading mencegah duplikasi. |

---

# G. Test Cases Analytics / Report

| Test Case ID | Status Kesesuaian | Teknik Black Box | Prioritas | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---|---|---:|---|---|---|---|
| REP_001 | Sesuai | Use Case, Visual Validation | 2 | Data arsip tersedia | Data nominal/tahun bervariasi | 1. Buka Analytics. 2. Pilih tahun/range jika tersedia. | Statistik total dan klasifikasi tampil sesuai data Room. |
| REP_002 | Tambahan sesuai repository | BVA | 1 | Analytics tersedia | Tahun tanpa data | 1. Pilih tahun tanpa data. | Dashboard menampilkan nol/empty state tanpa crash. |
| REP_003 | Tambahan sesuai repository | Data Consistency | 1 | Data arsip tersedia | Arsip nominal 1000, 2000 | 1. Buka analytics tahun terkait. | Total budget sesuai agregasi nominal. |

---

# H. Test Cases Audit Log

| Test Case ID | Status Kesesuaian | Teknik Black Box | Prioritas | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---|---|---:|---|---|---|---|
| AUD_001 | Sesuai sebagai backend integration, bukan UI Android langsung | Integration Black Box | 2 | Supabase trigger activity_logs aktif | Create data arsip | 1. Create arsip. 2. Query activity_logs di Supabase staging. | Log create tercatat. |
| AUD_002 | Sesuai sebagai backend integration | Integration Black Box | 2 | Trigger aktif | Update arsip | 1. Update arsip. 2. Query activity_logs. | Log update tercatat. |
| AUD_003 | Sesuai sebagai backend integration | Integration Black Box | 2 | Trigger aktif | Delete arsip | 1. Delete arsip. 2. Query activity_logs. | Log delete tercatat. |

---

# I. Test Cases Tracking Lokasi

| Test Case ID | Status Kesesuaian | Teknik Black Box | Prioritas | Pre-condition | Test Data | Test Steps | Expected Result |
|---|---|---|---:|---|---|---|---|
| TRK_001 | Sesuai sebagai metadata lokasi, bukan visual map | Use Case | 1 | Data arsip punya warehouse/rack/box | Gudang A, Rak 1, Box 1 | 1. Buka detail arsip. | Lokasi tampil lengkap dari metadata. |
| TRK_002 | Sesuai sebagian | Use Case | 2 | Data arsip tersedia | Ubah rack/box | 1. Edit arsip. 2. Ubah lokasi. 3. Simpan. | Lokasi berubah pada detail. |
| TRK_003 | Belum sesuai jika kapasitas box belum ada di model | BVA | 3 setelah fitur kapasitas dibuat | Kapasitas box tersedia | Box penuh | 1. Tambah arsip ke box penuh. | Ditolak. Saat ini belum tampak field kapasitas. |
| TRK_004 | Sesuai | Negative Testing | 1 | Create box terbuka | Lokasi kosong | 1. Kosongkan Gudang/Rak/Box. 2. Konfirmasi. | Validasi lokasi wajib muncul. |
| TRK_005 | Belum tampak sebagai fitur lampiran umum; yang ada upload cover image | Use Case | 3 setelah fitur lampiran dibuat | Lampiran file tersedia | PDF/JPG | 1. Tambah lampiran. | Saat ini perlu implementasi fitur lampiran. |
| TRK_006 | Belum tampak jika riwayat perpindahan belum di UI/kode | State Transition | 3 setelah fitur history dibuat | History placement tersedia | Lokasi lama/baru | 1. Pindah lokasi. 2. Buka riwayat. | Riwayat tercatat. Saat ini belum tampak implementasi UI. |

---

# J. Daftar Final Test Cases yang Dapat Diotomatisasi untuk Black-Box Testing

## Prioritas 1 — Otomatisasi Utama

| Modul | Test Case ID |
|---|---|
| Login | LGN_001, LGN_003, LGN_004, LGN_006, LGN_007 |
| Input/Staging | INP_001, INP_002, INP_004, INP_005, INP_006, INP_007, INP_008, INP_009, INP_010, INP_011, INP_012 |
| Box Context | BOX_001, BOX_002, BOX_003 |
| OCR Permission | OCR_005 |
| Offline/Sync | OFF_001, OFF_002, OFF_003, OFF_005, OFF_006, OFF_009 |
| Search | SCH_001, SCH_002, SCH_003, SCH_005, SCH_006 |
| Import/Export Excel | IMP_001, IMP_002, EXP_001, EXP_004, EXP_005, EXP_008, EXP_011 |
| Analytics | REP_002, REP_003 |
| Tracking Lokasi | TRK_001, TRK_004 |

## Prioritas 2 — Semi-Otomatis / Integration

| Modul | Test Case ID |
|---|---|
| Login/Session | LGN_005 |
| RBAC setelah implementasi role | RBAC_001, RBAC_002, RBAC_003, RBAC_004, RBAC_005 |
| OCR | OCR_001, OCR_003, OCR_004, OCR_006, OCR_007, OCR_008, OCR_010, OCR_011 |
| Offline/Edit/Conflict | OFF_004, OFF_008 |
| Server Error | SCH_004 |
| Report Visual | REP_001 |
| Audit Log Backend | AUD_001, AUD_002, AUD_003 |
| Tracking Edit Lokasi | TRK_002 |

## Prioritas 3 — Manual / Setelah Fitur Tersedia

| Modul | Test Case ID |
|---|---|
| OCR Galeri/File | OCR_002, OCR_009 |
| Export Advanced | EXP_002, EXP_003, EXP_006, EXP_007, EXP_009, EXP_010 |
| Tracking Advanced | TRK_003, TRK_005, TRK_006 |
| PDF Visual | PDF_VISUAL_001 |
| Dashboard Visual | DASH_VISUAL_001 |

---

# K. Test Cases Lama yang Sebaiknya Direvisi atau Ditunda

| Test Case Lama | Keputusan | Alasan |
|---|---|---|
| LGN_002 versi Kepala Subbag dashboard khusus | Ditunda/revisi | RBAC belum tampak di kode. |
| EXP_001 filter semester/tahun/subbagian | Revisi | Filter yang terlihat adalah tahun dan search query, bukan semester/subbagian. |
| EXP_003 rentang tanggal | Ditunda | Date range belum tampak di fitur list/export. |
| EXP_009 PDF | Ditunda | Implementasi PDF belum tampak di repository. |
| OCR_002 galeri | Ditunda | ScanScreen yang terlihat memakai CameraX, bukan galeri picker. |
| TRK_003 box penuh | Ditunda | Kapasitas box belum tampak di model. |
| TRK_005 lampiran arsip | Ditunda | Lampiran umum belum tampak sebagai fitur; hanya upload cover image. |
| TRK_006 riwayat perpindahan lokasi | Ditunda | UI/logic history placement belum tampak. |
| AUD_001–AUD_003 sebagai UI test Android | Revisi | Audit log ditangani Supabase trigger, cocoknya backend integration test. |
