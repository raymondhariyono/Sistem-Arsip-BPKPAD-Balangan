package com.example.arsipbpkpad.domain.archive.usecase

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.archive.model.ArchiveDocument
import com.example.arsipbpkpad.domain.archive.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetArchivesUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    operator fun invoke(): Flow<ResultState<List<ArchiveDocument>>> {
        return repository.getArchives()
    }
}
