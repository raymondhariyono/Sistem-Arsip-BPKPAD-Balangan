package com.example.arsipbpkpad.presentation.storage

import com.example.arsipbpkpad.domain.model.BoxDetails
import com.example.arsipbpkpad.domain.model.Room
import com.example.arsipbpkpad.domain.model.Shelf
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import com.example.arsipbpkpad.utils.ResultState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BoxManagementViewModelTest {

    private val repository = mockk<StorageLocationRepository>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: BoxManagementViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock initial loads to avoid hangs or unintended behaviors
        every { repository.getRooms() } returns flowOf(ResultState.Success(emptyList()))
        every { repository.getAllBoxes() } returns flowOf(ResultState.Success(emptyList()))
        
        viewModel = BoxManagementViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LOC_008 - filter box requires shelf`() = runTest {
        viewModel.setFilterRoom(Room("R1", "Gedung A"))
        viewModel.setFilterShelf(null)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.boxes is ResultState.Idle)
    }

    @Test
    fun `LOC_009 - filter box by shelf`() = runTest {
        val shelf1 = Shelf("S1", "Rak 1", "R1")
        val box1 = BoxDetails("B1", "Box 1", "S1", "Rak 1", "R1", "Gedung A")
        val box2 = BoxDetails("B2", "Box 2", "S2", "Rak 2", "R1", "Gedung A")
        
        // Mock loadBoxes response
        every { repository.getAllBoxes() } returns flowOf(ResultState.Success(listOf(box1, box2)))
        
        // Trigger load manually since it's already triggered in init with empty list
        viewModel.loadBoxes()
        advanceUntilIdle()
        
        // Set room first (triggers shelf loading)
        viewModel.setFilterRoom(Room("R1", "Gedung A"))
        // Then set shelf
        viewModel.setFilterShelf(shelf1)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.boxes is ResultState.Success)
        val filtered = (state.boxes as ResultState.Success).data
        assertEquals(1, filtered.size)
        assertEquals("B1", filtered[0].id)
    }
}
