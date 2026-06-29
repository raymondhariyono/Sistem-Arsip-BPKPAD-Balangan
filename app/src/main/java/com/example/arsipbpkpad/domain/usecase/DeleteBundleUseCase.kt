package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import javax.inject.Inject

/**
 * UseCase for deleting an entire transaction bundle and its archives.
 */
class DeleteBundleUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(bundleId: String): DomainResult<Unit> {
        return repository.deleteEntireBundle(bundleId)
    }
}
