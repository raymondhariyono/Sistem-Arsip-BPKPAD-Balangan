package com.example.arsipbpkpad.presentation.archive.add.manual

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class StagingBoxListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAddBoxDialogValidation() {
        val viewModel = mockk<RapidInputViewModel>(relaxed = true)
        val state = MutableStateFlow(RapidInputUiState(
            validationErrors = mapOf(
                "warehouse" to "Gudang wajib diisi",
                "rack" to "Nomor rak wajib diisi",
                "box" to "Nomor box wajib diisi"
            )
        ))
        every { viewModel.uiState } returns state

        composeTestRule.setContent {
            StagingBoxListScreen(
                viewModel = viewModel,
                onNavigateToRapidInput = {},
                onNavigateBack = {},
                onNavigateToBottomNav = {}
            )
        }

        // Trigger FAB to show dialog (mock state already says it's showing or we can just mock the UI to show it)
        // In StagingBoxListScreen.kt, showAddBoxDialog is a local remember state.
        // To test it properly, we might need to performClick on the FAB.
        composeTestRule.onNodeWithText("Tambah Box").performClick()

        composeTestRule.onNodeWithText("Gudang wajib diisi").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nomor rak wajib diisi").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nomor box wajib diisi").assertIsDisplayed()
    }

    @Test
    fun testYearValidationMessage() {
        val viewModel = mockk<RapidInputViewModel>(relaxed = true)
        val state = MutableStateFlow(RapidInputUiState(
            validationErrors = mapOf("year" to "Tahun tidak valid (harus 4 digit)")
        ))
        every { viewModel.uiState } returns state

        composeTestRule.setContent {
            StagingBoxListScreen(
                viewModel = viewModel,
                onNavigateToRapidInput = {},
                onNavigateBack = {},
                onNavigateToBottomNav = {}
            )
        }

        composeTestRule.onNodeWithText("Tambah Box").performClick()
        composeTestRule.onNodeWithText("Tahun tidak valid (harus 4 digit)").assertIsDisplayed()
    }
}
