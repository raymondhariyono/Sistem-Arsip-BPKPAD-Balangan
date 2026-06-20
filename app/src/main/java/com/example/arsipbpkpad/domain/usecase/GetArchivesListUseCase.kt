package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for fetching a list of archive documents.
 */
class GetArchivesListUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    operator fun invoke(query: String? = null, years: List<Int> = emptyList()): Flow<DomainResult<List<ArchiveDocument>>> {
        return repository.getArchivesList(query, years)
    }
}
