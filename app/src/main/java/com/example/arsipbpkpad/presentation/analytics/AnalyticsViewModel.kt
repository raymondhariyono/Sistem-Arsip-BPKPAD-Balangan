package com.example.arsipbpkpad.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.usecase.GetAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
    private val getArchivedYearsUseCase: com.example.arsipbpkpad.domain.usecase.GetArchivedYearsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        observeAvailableYears()
    }

    private fun observeAvailableYears() {
        viewModelScope.launch {
            getArchivedYearsUseCase().collect { dbYears ->
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                val baseYears = (2015..currentYear).toSet()
                val combined = (baseYears + dbYears).sortedDescending()
                _uiState.update { it.copy(availableYears = combined) }
            }
        }
    }

    fun onYearSelected(year: Int) {
        _uiState.update { 
            it.copy(selectedYear = if (it.selectedYear == year) null else year) 
        }
    }

    fun onConfirmFilter() {
        val year = _uiState.value.selectedYear ?: return
        _uiState.update { it.copy(isFilterConfirmed = true) }
        loadAnalytics(year)
    }

    fun onResetFilter() {
        fetchJob?.cancel()
        _uiState.update { it.copy(isFilterConfirmed = false, totalBudget = 0.0) }
    }

    private fun loadAnalytics(year: Int) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            getAnalyticsUseCase(year).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ResultState.Success -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            totalBudget = result.data.totalBudget,
                            errorMessage = null
                        ) }
                    }
                    is ResultState.Error -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        ) }
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }
}
