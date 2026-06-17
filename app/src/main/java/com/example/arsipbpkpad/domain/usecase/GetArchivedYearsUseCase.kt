package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArchivedYearsUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    operator fun invoke(): Flow<List<Int>> {
        return repository.getArchivedYears()
    }
}
