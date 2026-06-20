package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import javax.inject.Inject

/**
 * UseCase for deleting an archive document.
 */
class DeleteArchiveUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(id: String): DomainResult<Unit> {
        return repository.deleteArchive(id)
    }
}
