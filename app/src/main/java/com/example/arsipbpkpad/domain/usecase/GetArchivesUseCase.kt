package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArchivesUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    operator fun invoke(query: String? = null): Flow<ResultState<List<ArchiveDocument>>> {
        return repository.getArchives(query)
    }
}
