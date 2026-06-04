package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import javax.inject.Inject

class DeleteArchiveUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(id: String): ResultState<Unit> {
        return repository.deleteArchive(id)
    }
}
