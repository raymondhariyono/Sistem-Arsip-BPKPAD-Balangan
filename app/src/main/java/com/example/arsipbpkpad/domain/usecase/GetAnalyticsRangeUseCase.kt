package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.AnalyticsData
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for fetching analytics data for a specific year range.
 */
class GetAnalyticsRangeUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    operator fun invoke(startYear: Int, endYear: Int): Flow<DomainResult<AnalyticsData>> {
        return repository.getAnalyticsDataForRange(startYear, endYear)
    }
}
