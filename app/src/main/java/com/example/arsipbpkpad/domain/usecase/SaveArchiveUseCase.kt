package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import javax.inject.Inject

/**
 * UseCase for saving an archive document.
 */
class SaveArchiveUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(archive: ArchiveDocument): DomainResult<Boolean> {
        return repository.saveArchive(archive)
    }
}
