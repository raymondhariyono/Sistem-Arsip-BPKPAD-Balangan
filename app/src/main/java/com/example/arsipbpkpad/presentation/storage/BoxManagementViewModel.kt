package com.example.arsipbpkpad.presentation.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.domain.model.BoxDetails
import com.example.arsipbpkpad.domain.model.Room
import com.example.arsipbpkpad.domain.model.Shelf
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import com.example.arsipbpkpad.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BoxManagementUiState(
    val boxes: ResultState<List<BoxDetails>> = ResultState.Idle,
    val rooms: List<Room> = emptyList(),
    val shelves: List<Shelf> = emptyList(), // For Add Form
    val filterShelves: List<Shelf> = emptyList(), // For Main Filters
    val selectedFilterRoom: Room? = null,
    val selectedFilterShelf: Shelf? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val boxNameError: String? = null,
    val isSaveSuccess: Boolean = false
)

@HiltViewModel
class BoxManagementViewModel @Inject constructor(
    private val repository: StorageLocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoxManagementUiState())
    val uiState: StateFlow<BoxManagementUiState> = _uiState.asStateFlow()

    private var allBoxesList: List<BoxDetails> = emptyList()

    init {
        loadBoxes()
        loadRooms()
    }

    fun loadBoxes() {
        viewModelScope.launch {
            repository.getAllBoxes().collect { state ->
                if (state is ResultState.Success) {
                    allBoxesList = state.data
                    applyFilters()
                } else if (state is ResultState.Error) {
                    _uiState.update { it.copy(boxes = state) }
                }
            }
        }
    }

    fun loadRooms() {
        viewModelScope.launch {
            repository.getRooms().collect { state ->
                if (state is ResultState.Success) {
                    _uiState.update { it.copy(rooms = state.data) }
                }
            }
        }
    }

    fun loadShelves(roomId: String, isForFilter: Boolean = false) {
        viewModelScope.launch {
            repository.getShelvesByRoom(roomId).collect { state ->
                if (state is ResultState.Success) {
                    if (isForFilter) {
                        _uiState.update { it.copy(filterShelves = state.data) }
                    } else {
                        _uiState.update { it.copy(shelves = state.data) }
                    }
                }
            }
        }
    }

    fun setFilterRoom(room: Room?) {
        _uiState.update { it.copy(
            selectedFilterRoom = room,
            selectedFilterShelf = null,
            filterShelves = emptyList(),
            boxes = ResultState.Idle
        ) }
        if (room != null) {
            loadShelves(room.id, isForFilter = true)
        }
    }

    fun setFilterShelf(shelf: Shelf?) {
        _uiState.update { it.copy(selectedFilterShelf = shelf) }
        applyFilters()
    }

    private fun applyFilters() {
        val selectedShelf = _uiState.value.selectedFilterShelf
        if (selectedShelf == null) {
            _uiState.update { it.copy(boxes = ResultState.Idle) }
            return
        }

        val filtered = allBoxesList.filter { it.shelfId == selectedShelf.id }
        _uiState.update { it.copy(boxes = ResultState.Success(filtered)) }
    }

    fun clearErrors() {
        _uiState.update { it.copy(boxNameError = null, error = null) }
    }
}
