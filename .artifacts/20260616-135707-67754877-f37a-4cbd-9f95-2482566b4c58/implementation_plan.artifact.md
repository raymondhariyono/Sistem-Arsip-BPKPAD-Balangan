# Refactoring: Remove `thirdParty` and Refine Auto-Bundle Document Numbers

This plan outlines the steps to remove the redundant `thirdParty` field from the codebase and implement refined document numbering logic for the Auto-Bundle flow in `RapidInputScreen`.

## User Review Required

- **OCR Data Mapping**: The `thirdParty` detection from OCR (`metadata.subject`) will no longer be mapped to a dedicated field. It is assumed this info is already present in the `description` or will be manually added if needed.
- **SPP Document Number**: For the Auto-Bundle flow, SPP document number will be set to `null` as requested.

## Proposed Changes

### Domain & Data Layer
Clean up the data models and repositories to remove `thirdParty`.

#### [ArchiveEntity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/arsipBPKPAD/app/src/main/java/com/example/arsipbpkpad/data/local/entity/ArchiveEntity.kt)
- Remove `val thirdParty: String?` property.

#### [StagingArchiveEntity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/arsipBPKPAD/app/src/main/java/com/example/arsipbpkpad/data/local/entity/StagingArchiveEntity.kt)
- Remove `val thirdParty: String?` property.

#### [ArchiveDao.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/arsipBPKPAD/app/src/main/java/com/example/arsipbpkpad/data/local/dao/ArchiveDao.kt)
- Update `getArchives` and `getArchivesList` queries to remove search filtering by `thirdParty`.

#### [DatabaseModule.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/arsipBPKPAD/app/src/main/java/com/example/arsipbpkpad/di/DatabaseModule.kt)
- Update `getDummyArchives()` to remove `thirdParty` argument from `ArchiveEntity` constructors.

#### [StagingMapper.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/arsipBPKPAD/app/src/main/java/com/example/arsipbpkpad/data/mapper/StagingMapper.kt)
- Ensure `toDomain` and `toStagingEntity` (after `StagingArchiveEntity` change) are correctly mapped without `thirdParty`.

---

### ViewModel & Presentation Layer
Update the state and logic for `RapidInputScreen`.

#### [RapidInputViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/arsipBPKPAD/app/src/main/java/com/example/arsipbpkpad/presentation/archive/add/manual/RapidInputViewModel.kt)
- **UI State**: Remove `thirdParty`, add `spmDocumentNumber`.
- **UI Event**: Remove `OnThirdPartyChange`, add `OnSpmDocNumberChange`.
- **OCR Logic**: Remove `thirdParty` mapping from `metadata.subject`.
- **Auto-Bundle Logic**:
    - Validate `spmDocumentNumber` when `isAutoBundleEnabled` is true.
    - Update `addToStaging()` to use separate numbers:
        - SP2D -> `documentNumber`
        - SPM -> `spmDocumentNumber`
        - SPP/SPJ -> `null`
- **Clean up**: Remove `thirdParty` from all helper methods and state updates.

#### [RapidInputScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/arsipBPKPAD/app/src/main/java/com/example/arsipbpkpad/presentation/archive/add/manual/RapidInputScreen.kt)
- **Form Fields**:
    - Remove "Pihak Ketiga / Vendor" field.
    - If `isAutoBundleEnabled`, change label of primary document field to "Nomor Dokumen SP2D".
    - Add "Nomor Dokumen SPM" field below it when `isAutoBundleEnabled` is true.
- **Staging List**: Remove `thirdParty` from the supporting text in `StagedItemRow`.

---

### Other UI Components

#### [ArchiveListScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/arsipBPKPAD/app/src/main/java/com/example/arsipbpkpad/presentation/archive/list/ArchiveListScreen.kt)
- Update `ArchiveListScreenPreview` to remove `thirdParty` from mock `ArchiveDocument`.

## Verification Plan

### Automated Tests
- Run `analyze_file` on all modified files to ensure no unresolved references or type mismatches remain.
- I will attempt to run a build if possible to verify compilation.

### Manual Verification
- **Rapid Input Form**:
    1. Open Rapid Input.
    2. Verify "Pihak Ketiga" field is gone.
    3. Toggle "Auto-Bundle".
    4. Verify label changes to "Nomor Dokumen SP2D".
    5. Verify "Nomor Dokumen SPM" field appears.
    6. Enter values for both and click "Tambah ke Staging".
    7. Verify generated documents in staging list:
        - SP2D has the first number.
        - SPM has the second number.
        - SPP/SPJ have no number.
- **Search**: Verify that searching in the archive list still works for document numbers and doesn't crash now that `thirdParty` search is removed from Dao.
