package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import javax.inject.Inject

class SaveArchiveUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(archive: ArchiveDocument): ResultState<Unit> {
        return repository.saveArchive(archive)
    }
}
