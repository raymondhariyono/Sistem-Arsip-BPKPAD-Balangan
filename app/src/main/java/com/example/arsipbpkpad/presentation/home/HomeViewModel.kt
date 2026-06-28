package com.example.arsipbpkpad.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.domain.model.DocumentTypeDefaults
import com.example.arsipbpkpad.domain.model.DocumentTypeDefaults.normalizeDocumentType
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.usecase.GetArchivedYearsUseCase
import com.example.arsipbpkpad.domain.usecase.GetArchivesListUseCase
import com.example.arsipbpkpad.domain.usecase.GetYearStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home Screen.
 * Manages dashboard state and unidirectional data flow.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getArchivesListUseCase: GetArchivesListUseCase,
    private val stagingRepository: StagingRepository,
    private val getArchivedYearsUseCase: GetArchivedYearsUseCase,
    private val getYearStatsUseCase: GetYearStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        observeStagingData()
        observeAvailableYears()
        observeYearStats()
    }

    private fun observeYearStats() {
        viewModelScope.launch {
            getYearStatsUseCase().collect { stats ->
                _uiState.update { it.copy(yearStats = stats) }
            }
        }
    }

    private fun observeStagingData() {
        viewModelScope.launch {
            stagingRepository.getAllStagedBoxes().collect { boxes ->
                _uiState.update { it.copy(activeStagingBoxes = boxes) }
            }
        }
    }

    private fun observeAvailableYears() {
        viewModelScope.launch {
            getArchivedYearsUseCase().collect { years ->
                _uiState.update { it.copy(availableYears = years) }
            }
        }
    }

    fun onToggleYearSelection(show: Boolean) {
        _uiState.update { it.copy(showYearSelection = show) }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getArchivesListUseCase().collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        val archives = result.data
                        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                        
                        val primaryTypes = setOf(
                            DocumentTypeDefaults.SP2D,
                            DocumentTypeDefaults.SPM,
                            DocumentTypeDefaults.SPJ
                        )

                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                totalDocuments = archives.size.toString(),
                                expiredDocuments = archives.count { (currentYear - it.year) > 10 }.toString(),
                                sp2dCount = archives.count { normalizeDocumentType(it.type) == DocumentTypeDefaults.SP2D }.toString(),
                                spmCount = archives.count { normalizeDocumentType(it.type) == DocumentTypeDefaults.SPM }.toString(),
                                spjCount = archives.count { normalizeDocumentType(it.type) == DocumentTypeDefaults.SPJ }.toString(),
                                otherTypeCount = archives.count { normalizeDocumentType(it.type) !in primaryTypes }.toString(),
                                recentItems = archives.take(5)
                            )
                        }
                    }
                    is DomainResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }
}
