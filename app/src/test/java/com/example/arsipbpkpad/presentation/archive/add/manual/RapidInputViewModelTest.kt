package com.example.arsipbpkpad.presentation.archive.add.manual

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.example.arsipbpkpad.domain.model.*
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import com.example.arsipbpkpad.domain.usecase.BulkInsertArchivesUseCase
import com.example.arsipbpkpad.domain.usecase.GetArchiveDetailUseCase
import com.example.arsipbpkpad.utils.ResultState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class RapidInputViewModelTest {

    private val stagingRepository = mockk<StagingRepository>(relaxed = true)
    private val archiveRepository = mockk<ArchiveRepository>(relaxed = true)
    private val storageLocationRepository = mockk<StorageLocationRepository>(relaxed = true)
    private val bulkInsertArchivesUseCase = mockk<BulkInsertArchivesUseCase>()
    private val getArchiveDetailUseCase = mockk<GetArchiveDetailUseCase>(relaxed = true)
    private val savedStateHandle = SavedStateHandle()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: RapidInputViewModel

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        
        Dispatchers.setMain(testDispatcher)
        viewModel = RapidInputViewModel(
            stagingRepository,
            archiveRepository,
            storageLocationRepository,
            bulkInsertArchivesUseCase,
            getArchiveDetailUseCase,
            savedStateHandle
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `BOX_001 - load rooms on init`() = runTest {
        val rooms = listOf(Room("R1", "Gedung A"))
        coEvery { storageLocationRepository.getRooms() } returns flowOf(ResultState.Success(rooms))
        
        // Re-init to trigger init block again with mocked response
        viewModel = RapidInputViewModel(
            stagingRepository, archiveRepository, storageLocationRepository,
            bulkInsertArchivesUseCase, getArchiveDetailUseCase, savedStateHandle
        )
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.roomsList is ResultState.Success)
        assertEquals(rooms, (state.roomsList as ResultState.Success).data)
    }

    @Test
    fun `BOX_002 - selecting room loads shelves`() = runTest {
        val room = Room("R1", "Gedung A")
        val shelves = listOf(Shelf("S1", "Rak 1", "R1"))
        coEvery { storageLocationRepository.getShelvesByRoom("R1") } returns flowOf(ResultState.Success(shelves))
        
        viewModel.onEvent(RapidInputUiEvent.OnRoomSelected(room))
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(room, state.selectedRoom)
        assertTrue(state.shelvesList is ResultState.Success)
        assertEquals(shelves, (state.shelvesList as ResultState.Success).data)
    }

    @Test
    fun `BOX_004 - validate room is required`() = runTest {
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxLocationChange("B1"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals("Gudang wajib dipilih", state.validationErrors["warehouse"])
    }

    @Test
    fun `BOX_005 - validate shelf is required`() = runTest {
        val room = Room("R1", "Gedung A")
        viewModel.onEvent(RapidInputUiEvent.OnRoomSelected(room))
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxLocationChange("B1"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals("Nomor rak wajib dipilih", state.validationErrors["rack"])
    }

    @Test
    fun `BOX_006 - validate box name is required`() = runTest {
        val room = Room("R1", "Gedung A")
        val shelf = Shelf("S1", "Rak 1", "R1")
        viewModel.onEvent(RapidInputUiEvent.OnRoomSelected(room))
        viewModel.onEvent(RapidInputUiEvent.OnShelfSelected(shelf))
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxLocationChange("")) // Empty
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals("Nama Box wajib diisi", state.validationErrors["box"])
    }

    @Test
    fun `BOX_007 - validate duplicate box on same shelf`() = runTest {
        val room = Room("R1", "Gedung A")
        val shelf = Shelf("S1", "Rak 1", "R1")
        coEvery { storageLocationRepository.checkBoxExists("S1", "B1") } returns true
        
        viewModel.onEvent(RapidInputUiEvent.OnRoomSelected(room))
        viewModel.onEvent(RapidInputUiEvent.OnShelfSelected(shelf))
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxLocationChange("B1"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals("Box dengan nomor ini sudah ada di rak tersebut", state.validationErrors["box"])
    }

    @Test
    fun `BOX_008 - valid box context creates session`() = runTest {
        val room = Room("R1", "Gedung A")
        val shelf = Shelf("S1", "Rak 1", "R1")
        coEvery { storageLocationRepository.checkBoxExists("S1", "B1") } returns false
        
        viewModel.onEvent(RapidInputUiEvent.OnRoomSelected(room))
        viewModel.onEvent(RapidInputUiEvent.OnShelfSelected(shelf))
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxLocationChange("B1"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.isBoxContextSet)
        assertNotNull(state.currentSessionId)
    }

    @Test
    fun `INP_002 - test validation fails when fields are empty`() = runTest {
        setupValidBoxContext()
        
        // Try to add with empty doc number and subject
        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Nomor dokumen wajib diisi", state.validationErrors["docNumber"])
        assertEquals("Uraian dokumen wajib diisi", state.validationErrors["subject"])
    }

    @Test
    fun `INP_003 - test nominal validation`() = runTest {
        setupValidBoxContext()

        viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange("AUTO-DOC-001"))
        viewModel.onEvent(RapidInputUiEvent.OnSubjectChange("Test Subject"))

        // Test nominal -1
        viewModel.onEvent(RapidInputUiEvent.OnNominalChange("-1"))
        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick())
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals("Nominal tidak boleh kurang dari nol", state.validationErrors["nominal"])
    }

    @Test
    fun `INP_006 - test invalid nominal characters`() = runTest {
        setupValidBoxContext()

        viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange("AUTO-DOC-001"))
        viewModel.onEvent(RapidInputUiEvent.OnSubjectChange("Test Subject"))

        // Test nominal "abc"
        viewModel.onEvent(RapidInputUiEvent.OnNominalChange("abc"))
        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick())
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals("Nominal harus berupa angka lebih dari 0", state.validationErrors["nominal"])
    }

    @Test
    fun `INP_010 - duplicate check shows warning`() = runTest {
        setupValidBoxContext()
        
        coEvery { archiveRepository.checkDocumentNumberAndTypeExists("DUP-001", "ORIGINAL") } returns true

        viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange("DUP-001"))
        viewModel.onEvent(RapidInputUiEvent.OnSubjectChange("Test"))
        viewModel.onEvent(RapidInputUiEvent.OnNominalChange("1000"))

        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showDuplicateWarning)
    }

    @Test
    fun `INP_011 - test auto-bundle SP2D, SPM, and SPJ`() = runTest {
        setupValidBoxContext()

        // Select SP2D and enable auto-bundle
        viewModel.onEvent(RapidInputUiEvent.OnDocTypeChange(DocType.SP2D))
        viewModel.onEvent(RapidInputUiEvent.OnAutoBundleToggle(true))
        viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange("SP2D-BUNDLE-001"))
        viewModel.onEvent(RapidInputUiEvent.OnSpmDocNumberChange("SPM-BUNDLE-001"))
        viewModel.onEvent(RapidInputUiEvent.OnSubjectChange("Bundle Subject"))
        viewModel.onEvent(RapidInputUiEvent.OnSpjDescriptionChange("SPJ Description"))
        viewModel.onEvent(RapidInputUiEvent.OnNominalChange("5000"))

        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick())
        advanceUntilIdle()

        // Verify that 3 documents were inserted to staging
        coVerify(exactly = 3) { stagingRepository.insertToStaging(any()) }
    }

    @Test
    fun `MNG_001 - test editing staged document`() = runTest {
        setupValidBoxContext()

        val mockDoc = ArchiveDocument(
            id = "doc-123",
            boxSessionId = "session-1",
            type = DocType.SP2D,
            documentNumber = "OLD-NUM",
            copyType = DocCopyType.ORIGINAL,
            description = "Old Subject",
            nominal = 1000.0,
            year = 2026,
            status = DocStatus.UNVERIFIED,
            metadata = null
        )

        viewModel.onEvent(RapidInputUiEvent.OnEditStagedDoc(mockDoc))
        advanceUntilIdle()

        val stateBefore = viewModel.uiState.value
        assertEquals("doc-123", stateBefore.editingId)
        assertEquals("OLD-NUM", stateBefore.documentNumber)

        // Change values
        viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange("NEW-NUM"))
        viewModel.onEvent(RapidInputUiEvent.OnNominalChange("2000"))
        
        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick())
        advanceUntilIdle()

        // Verify it inserted to staging
        coVerify { stagingRepository.insertToStaging(match { it.id == "doc-123" && it.documentNumber == "NEW-NUM" }) }
    }

    @Test
    fun `INP_007 - copy count locked to 1 for ORIGINAL`() = runTest {
        viewModel.onEvent(RapidInputUiEvent.OnCopyTypeChange(DocCopyType.ORIGINAL))
        viewModel.onEvent(RapidInputUiEvent.OnCopyCountChange("5")) // Try to change
        
        val state = viewModel.uiState.value
        assertEquals("1", state.copyCount)
    }

    @Test
    fun `INP_008 - copy count can be changed for COPY`() = runTest {
        viewModel.onEvent(RapidInputUiEvent.OnCopyTypeChange(DocCopyType.COPY))
        viewModel.onEvent(RapidInputUiEvent.OnCopyCountChange("5"))
        
        val state = viewModel.uiState.value
        assertEquals("5", state.copyCount)
    }

    @Test
    fun `INP_012 - auto bundle error if SPM number empty`() = runTest {
        setupValidBoxContext()

        viewModel.onEvent(RapidInputUiEvent.OnDocTypeChange(DocType.SP2D))
        viewModel.onEvent(RapidInputUiEvent.OnAutoBundleToggle(true))
        viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange("SP2D-123"))
        viewModel.onEvent(RapidInputUiEvent.OnSubjectChange("Subject"))
        viewModel.onEvent(RapidInputUiEvent.OnSpmDocNumberChange("")) // Empty SPM

        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Nomor SPM wajib diisi", state.validationErrors["spmDocNumber"])
    }

    @Test
    fun `BOX_003 - year must be 4 digits`() = runTest {
        viewModel.onEvent(RapidInputUiEvent.OnRoomSelected(Room("R1", "Gedung A")))
        viewModel.onEvent(RapidInputUiEvent.OnShelfSelected(Shelf("S1", "Rak 1", "R1")))
        viewModel.onEvent(RapidInputUiEvent.OnBoxLocationChange("B1"))
        
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("26"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Tahun tidak valid (harus 4 digit)", state.validationErrors["year"])
    }

    private fun TestScope.setupValidBoxContext() {
        val room = Room("R1", "Gedung A")
        val shelf = Shelf("S1", "Rak 1", "R1")
        coEvery { storageLocationRepository.checkBoxExists(any(), any()) } returns false
        
        viewModel.onEvent(RapidInputUiEvent.OnRoomSelected(room))
        viewModel.onEvent(RapidInputUiEvent.OnShelfSelected(shelf))
        viewModel.onEvent(RapidInputUiEvent.OnBoxLocationChange("B1"))
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        advanceUntilIdle()
    }
}
