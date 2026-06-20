package com.example.arsipbpkpad.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.usecase.GetAnalyticsUseCase
import com.example.arsipbpkpad.domain.usecase.GetAnalyticsRangeUseCase
import com.example.arsipbpkpad.domain.usecase.GetArchivedYearsUseCase
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
    private val getAnalyticsRangeUseCase: GetAnalyticsRangeUseCase,
    private val getArchivedYearsUseCase: GetArchivedYearsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        observeAvailableYears()
        loadPast10YearsAnalytics()
        // Load initial data for the default selected year
        _uiState.value.selectedYear?.let { loadAnalytics(it) }
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

    private fun loadPast10YearsAnalytics() {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val startYear = currentYear - 9
        viewModelScope.launch {
            getAnalyticsRangeUseCase(startYear, currentYear).collect { result ->
                if (result is DomainResult.Success) {
                    _uiState.update { it.copy(
                        past10YearsBudgets = result.data.budgetByClassification
                    ) }
                }
            }
        }
    }

    fun onYearSelected(year: Int) {
        val newYear = if (_uiState.value.selectedYear == year) null else year
        _uiState.update { it.copy(selectedYear = newYear) }
        if (newYear != null) {
            loadAnalytics(newYear)
        } else {
            _uiState.update { it.copy(totalBudget = 0.0, budgetByClassification = emptyMap()) }
        }
    }

    fun onConfirmFilter() {
        val year = _uiState.value.selectedYear ?: return
        _uiState.update { it.copy(isFilterConfirmed = true) }
        loadAnalytics(year)
    }

    fun onResetFilter() {
        fetchJob?.cancel()
        _uiState.update { it.copy(isFilterConfirmed = false) }
    }

    private fun loadAnalytics(year: Int) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getAnalyticsUseCase(year).collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            totalBudget = result.data.totalBudget,
                            budgetByClassification = result.data.budgetByClassification,
                            errorMessage = null
                        ) }
                    }
                    is DomainResult.Error -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        ) }
                    }
                }
            }
        }
    }
}
