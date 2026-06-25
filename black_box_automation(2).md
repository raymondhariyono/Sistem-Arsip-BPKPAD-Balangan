# Analisis Test Case & Scope Automasi
Sistem Arsip BPKPAD Balangan (Revisi ERD)

---

## 1. Ringkasan Perubahan Sistem (ERD Baru)

Perubahan utama sistem mencakup:

- Penambahan entitas **location (gudang, rak, box)**
- Implementasi **staging table workflow**
- Penambahan **document placement history**
- Penguatan relasi foreign key antar arsip, user, dan lokasi
- Audit trail berbasis actor dan event logging
- Pemisahan data sementara vs data final

---

## 2. Dampak Perubahan terhadap Testing

Perubahan ERD menyebabkan:
- Penambahan test scenario pada workflow staging
- Penambahan validasi relasi lokasi fisik
- Penambahan histori mutasi dokumen
- Peningkatan kebutuhan integration testing

---

## 3. Klasifikasi Automasi Testing

### 3.1 Fully Automatable Test Cases

Test case yang dapat sepenuhnya diotomatisasi:

- Authentication (Login valid/invalid)
- Role-based access control
- CRUD Arsip (Create, Read, Update)
- Duplicate validation
- Search & filter engine
- Detail view validation
- Audit log verification (DB-level)
- Foreign key integrity check
- Staging CRUD operations
- Batch commit staging
- Location assignment validation
- Document placement history verification

---

### 3.2 Partially Automatable Test Cases

Test case yang membutuhkan kombinasi automation + manual validation:

- OCR scanning output accuracy
- OCR metadata parsing (confidence-based validation)
- UI rendering correctness (visual confirmation)
- Complex staging workflow validation
- Multi-filter search accuracy (UI + DB comparison)

---

### 3.3 Manual Only Test Cases

Test case yang tidak ideal untuk automation:

- Usability testing UI/UX
- Visual layout consistency
- User perception of OCR accuracy
- Human validation of scanned document readability
- Administrative workflow approval behavior

---

## 4. Automated Test Case Subset (Extracted)

Berikut test case yang direkomendasikan untuk automation:

| No | Module | Test Case | Automation Type |
|----|--------|----------|----------------|
| 1 | Auth | Login valid/invalid | Full |
| 2 | Auth | Role access validation | Full |
| 3 | Arsip | Create arsip valid | Full |
| 4 | Arsip | Duplicate validation | Full |
| 5 | Arsip | Update metadata | Full |
| 6 | Arsip | Search by keyword | Full |
| 7 | Arsip | Filter multi-parameter | Full |
| 8 | Staging | Insert staging data | Full |
| 9 | Staging | Commit batch | Full |
| 10 | Staging | Delete staging data | Full |
| 11 | Location | Assign location | Full |
| 12 | Location | Update location | Full |
| 13 | Placement | Insert history record | Full |
| 14 | Placement | Validate history integrity | Full |
| 15 | Audit | Insert log verification | Full |
| 16 | Audit | Update log verification | Full |
| 17 | Audit | Delete log verification | Full |
| 18 | DB | Foreign key integrity check | Full |

---

## 5. Automation Strategy

Automation disarankan menggunakan pendekatan:

- UI Automation: Selenium / Playwright
- API Testing: Postman / REST Assured
- Database Validation: SQL script assertions
- OCR Mock Testing: pre-defined image dataset
- Logging Verification: direct DB query

---

## 6. Non-Automatable Gap Handling

Untuk test case manual:
- Gunakan checklist evaluasi human reviewer
- Gunakan screenshot evidence
- Gunakan rating scale (1–5) untuk OCR & UI clarity

---

## 7. Kesimpulan Analitis

Dengan adanya perubahan ERD, tingkat automasi meningkat signifikan pada:

- data-driven testing
- relational integrity validation
- workflow staging control

Namun tetap terdapat gap pada:
- visual validation
- OCR semantic accuracy
- usability assessment