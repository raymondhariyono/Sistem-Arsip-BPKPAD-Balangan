package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.AnalyticsData
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * UseCase for fetching analytics data for a specific year.
 */
class GetAnalyticsUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    operator fun invoke(year: Int): Flow<DomainResult<AnalyticsData>> {
        return repository.getTotalBudgetByYear(year)
            .map { result ->
                when (result) {
                    is DomainResult.Success -> {
                        DomainResult.Success(AnalyticsData(result.data))
                    }
                    is DomainResult.Error -> DomainResult.Error(result.message, result.cause)
                }
            }
    }
}
