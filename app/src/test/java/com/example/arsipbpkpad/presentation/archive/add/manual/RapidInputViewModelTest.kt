package com.example.arsipbpkpad.presentation.archive.add.manual

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocType
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.usecase.BulkInsertArchivesUseCase
import com.example.arsipbpkpad.domain.usecase.GetArchiveDetailUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RapidInputViewModelTest {

    private val stagingRepository = mockk<StagingRepository>(relaxed = true)
    private val archiveRepository = mockk<ArchiveRepository>(relaxed = true)
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
    fun `INP_002 - test validation fails when fields are empty`() = runTest {
        // Confirm Box Context first
        viewModel.onEvent(RapidInputUiEvent.OnWarehouseChange("Gedung A"))
        viewModel.onEvent(RapidInputUiEvent.OnRackChange("Rak 1"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxChange("Box 1"))
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        testDispatcher.scheduler.runCurrent()

        // Try to add with empty doc number and subject
        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Nomor dokumen wajib diisi", state.validationErrors["docNumber"])
        assertEquals("Uraian dokumen wajib diisi", state.validationErrors["subject"])
    }

    @Test
    fun `INP_003 - test nominal validation`() = runTest {
        // Setup valid state
        viewModel.onEvent(RapidInputUiEvent.OnWarehouseChange("Gedung A"))
        viewModel.onEvent(RapidInputUiEvent.OnRackChange("Rak 1"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxChange("Box 1"))
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        testDispatcher.scheduler.runCurrent()

        viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange("AUTO-DOC-001"))
        viewModel.onEvent(RapidInputUiEvent.OnSubjectChange("Test Subject"))

        // Test nominal -1
        viewModel.onEvent(RapidInputUiEvent.OnNominalChange("-1"))
        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick)
        testDispatcher.scheduler.runCurrent()
        
        val state = viewModel.uiState.value
        assertEquals("Nominal tidak boleh kurang dari nol", state.validationErrors["nominal"])
    }

    @Test
    fun `INP_006 - test invalid nominal characters`() = runTest {
        // Setup valid state
        viewModel.onEvent(RapidInputUiEvent.OnWarehouseChange("Gedung A"))
        viewModel.onEvent(RapidInputUiEvent.OnRackChange("Rak 1"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxChange("Box 1"))
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        testDispatcher.scheduler.runCurrent()

        viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange("AUTO-DOC-001"))
        viewModel.onEvent(RapidInputUiEvent.OnSubjectChange("Test Subject"))

        // Test nominal "abc"
        viewModel.onEvent(RapidInputUiEvent.OnNominalChange("abc"))
        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick)
        testDispatcher.scheduler.runCurrent()
        
        val state = viewModel.uiState.value
        assertEquals("Nominal harus berupa angka lebih dari 0", state.validationErrors["nominal"])
    }

    @Test
    fun `test document number duplicates are allowed`() = runTest {
        // Setup valid state
        viewModel.onEvent(RapidInputUiEvent.OnWarehouseChange("Gedung A"))
        viewModel.onEvent(RapidInputUiEvent.OnRackChange("Rak 1"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxChange("Box 1"))
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        testDispatcher.scheduler.runCurrent()

        viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange("DUP-001"))
        viewModel.onEvent(RapidInputUiEvent.OnSubjectChange("Test"))

        // We no longer check for duplicates in ViewModel
        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals(null, state.error)
        coVerify { stagingRepository.insertToStaging(any()) }
    }

    @Test
    fun `INP_011 - test auto-bundle SP2D, SPM, and SPJ`() = runTest {
        // Setup valid state
        viewModel.onEvent(RapidInputUiEvent.OnWarehouseChange("Gedung A"))
        viewModel.onEvent(RapidInputUiEvent.OnRackChange("Rak 1"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxChange("Box 1"))
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        testDispatcher.scheduler.runCurrent()

        // Select SP2D and enable auto-bundle
        viewModel.onEvent(RapidInputUiEvent.OnDocTypeChange(DocType.SP2D))
        viewModel.onEvent(RapidInputUiEvent.OnAutoBundleToggle(true))
        viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange("SP2D-BUNDLE-001"))
        viewModel.onEvent(RapidInputUiEvent.OnSpmDocNumberChange("SPM-BUNDLE-001"))
        viewModel.onEvent(RapidInputUiEvent.OnSubjectChange("Bundle Subject"))
        viewModel.onEvent(RapidInputUiEvent.OnSpjDescriptionChange("SPJ Description"))

        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick)
        testDispatcher.scheduler.runCurrent()

        // Verify that 3 documents were inserted to staging
        coVerify(exactly = 3) { stagingRepository.insertToStaging(any()) }
    }

    @Test
    fun `MNG_001 - test editing staged document`() = runTest {
        // First confirm box context
        viewModel.onEvent(RapidInputUiEvent.OnWarehouseChange("Gedung A"))
        viewModel.onEvent(RapidInputUiEvent.OnRackChange("Rak 1"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxChange("Box 1"))
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        testDispatcher.scheduler.runCurrent()

        val mockDoc = mockk<com.example.arsipbpkpad.domain.model.ArchiveDocument>(relaxed = true) {
            every { id } returns "doc-123"
            every { documentNumber } returns "OLD-NUM"
            every { description } returns "Old Subject"
            every { type } returns DocType.SP2D
            every { copyType } returns DocCopyType.ORIGINAL
            every { copyCount } returns 1
            every { nominal } returns 1000.0
        }

        viewModel.onEvent(RapidInputUiEvent.OnEditStagedDoc(mockDoc))
        testDispatcher.scheduler.runCurrent()

        val stateBefore = viewModel.uiState.value
        assertEquals("doc-123", stateBefore.editingId)
        assertEquals("OLD-NUM", stateBefore.documentNumber)

        // Change values
        viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange("NEW-NUM"))
        
        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick)
        testDispatcher.scheduler.runCurrent()

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
        // Setup box context
        viewModel.onEvent(RapidInputUiEvent.OnWarehouseChange("Gedung A"))
        viewModel.onEvent(RapidInputUiEvent.OnRackChange("Rak 1"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxChange("Box 1"))
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("2026"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        testDispatcher.scheduler.runCurrent()

        viewModel.onEvent(RapidInputUiEvent.OnDocTypeChange(DocType.SP2D))
        viewModel.onEvent(RapidInputUiEvent.OnAutoBundleToggle(true))
        viewModel.onEvent(RapidInputUiEvent.OnDocNumberChange("SP2D-123"))
        viewModel.onEvent(RapidInputUiEvent.OnSubjectChange("Subject"))
        viewModel.onEvent(RapidInputUiEvent.OnSpmDocNumberChange("")) // Empty SPM

        viewModel.onEvent(RapidInputUiEvent.OnAddToBoxClick)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Nomor SPM wajib diisi", state.validationErrors["spmDocNumber"])
    }

    @Test
    fun `BOX_003 - year must be 4 digits`() = runTest {
        viewModel.onEvent(RapidInputUiEvent.OnWarehouseChange("Gedung A"))
        viewModel.onEvent(RapidInputUiEvent.OnRackChange("Rak 1"))
        viewModel.onEvent(RapidInputUiEvent.OnBoxChange("Box 1"))
        
        viewModel.onEvent(RapidInputUiEvent.OnYearChange("26"))
        viewModel.onEvent(RapidInputUiEvent.OnConfirmBoxContext)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Tahun tidak valid (harus 4 digit)", state.validationErrors["year"])
    }
}
