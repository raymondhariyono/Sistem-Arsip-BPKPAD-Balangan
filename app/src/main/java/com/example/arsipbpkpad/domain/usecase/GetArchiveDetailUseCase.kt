package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArchiveDetailUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    operator fun invoke(id: String): Flow<ResultState<ArchiveDocument>> {
        return repository.getArchiveDetail(id)
    }
}
