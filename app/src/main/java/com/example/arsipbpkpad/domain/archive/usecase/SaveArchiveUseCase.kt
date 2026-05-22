package com.example.arsipbpkpad.domain.archive.usecase

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.archive.model.ArchiveDocument
import com.example.arsipbpkpad.domain.archive.repository.ArchiveRepository


class SaveArchiveUseCase  constructor(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(archive: ArchiveDocument): ResultState<Unit> {
        return repository.saveArchive(archive)
    }
}
