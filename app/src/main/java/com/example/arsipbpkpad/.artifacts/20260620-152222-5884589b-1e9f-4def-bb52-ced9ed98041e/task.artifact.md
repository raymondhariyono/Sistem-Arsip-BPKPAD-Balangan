# Task Management

- [/] TASK 1: Excel-Like Archive List UI
    - [ ] Create Table Header in `ArchiveResultList`
    - [ ] Refactor `ArchiveListItemCard` to Table Row format
    - [ ] Implement zebra striping and vertical dividers
- [/] TASK 2: Remove Edit Functionality
    - [ ] Remove `onEditClick` from `ArchiveDetailScreen`
    - [ ] Remove `onEditClick` from `ArchiveDetailContent` and `ArchiveDetailMainList`
    - [ ] Delete Edit button from `DetailActionButtons`
- [/] TASK 3: Fix "Informasi Arsip Fisik" Mapping Bug
    - [ ] Add `@Serializable` to `ArchiveMetadata` domain model
    - [ ] Add `@SerialName` annotations to `ArchiveMetadataDto`
    - [ ] Verify mapping in `ArchiveMapper.kt`
