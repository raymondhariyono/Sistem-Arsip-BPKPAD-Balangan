package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for fetching details of a specific archive document.
 */
class GetArchiveDetailUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    operator fun invoke(id: String): Flow<DomainResult<ArchiveDocument>> {
        return repository.getArchiveDetail(id)
    }
}
