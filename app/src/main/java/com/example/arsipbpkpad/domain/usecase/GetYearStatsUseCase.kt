package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.YearStats
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for fetching statistics per year.
 */
class GetYearStatsUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    operator fun invoke(): Flow<List<YearStats>> {
        return repository.getYearStats()
    }
}
