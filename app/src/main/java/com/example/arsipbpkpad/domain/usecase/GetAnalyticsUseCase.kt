package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.AnalyticsData
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAnalyticsUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    operator fun invoke(year: Int): Flow<ResultState<AnalyticsData>> {
        return repository.getTotalBudgetByYear(year)
            .map { result ->
                when (result) {
                    is ResultState.Success -> {
                        ResultState.Success(AnalyticsData(result.data))
                    }
                    is ResultState.Loading -> ResultState.Loading
                    is ResultState.Error -> ResultState.Error(result.message)
                    is ResultState.Idle -> ResultState.Idle
                }
            }
    }
}
