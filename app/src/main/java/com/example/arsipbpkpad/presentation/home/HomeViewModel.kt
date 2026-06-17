package com.example.arsipbpkpad.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.usecase.GetArchivesListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getArchivesListUseCase: GetArchivesListUseCase,
    private val stagingRepository: StagingRepository,
    private val getArchivedYearsUseCase: com.example.arsipbpkpad.domain.usecase.GetArchivedYearsUseCase,
    private val getYearStatsUseCase: com.example.arsipbpkpad.domain.usecase.GetYearStatsUseCase
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
            getArchivesListUseCase().collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ResultState.Success -> {
                        val archives = result.data
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                totalDocuments = archives.size.toString(),
                                recentItems = archives.take(5).map {
                                    RecentArchive(
                                        id = it.id,
                                        title = it.documentNumber ?: "-",
                                        type = it.type.name,
                                        isAvailable = it.status == DocStatus.AVAILABLE
                                    )
                                }
                            )
                        }
                    }
                    is ResultState.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                    is ResultState.Idle -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }
}
