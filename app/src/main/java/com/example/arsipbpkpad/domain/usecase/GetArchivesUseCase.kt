package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for observing archive documents.
 * Returns a flow of lists to maintain framework independence.
 */
class GetArchivesUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    operator fun invoke(query: String? = null, years: List<Int> = emptyList()): Flow<List<ArchiveDocument>> {
        return repository.getArchivesFlow(query, years)
    }
}
