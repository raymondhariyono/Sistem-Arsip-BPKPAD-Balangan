package com.example.arsipbpkpad.presentation.archive.add.manual

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class RapidInputScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testValidationErrorsWhenFieldsEmpty() {
        val viewModel = mockk<RapidInputViewModel>(relaxed = true)
        val state = MutableStateFlow(RapidInputUiState(
            validationErrors = mapOf(
                "docNumber" to "Nomor dokumen wajib diisi",
                "subject" to "Uraian dokumen wajib diisi"
            )
        ))
        every { viewModel.uiState } returns state

        composeTestRule.setContent {
            RapidInputScreen(
                sessionId = "test-session",
                onNavigateBack = {},
                onNavigateToScan = {},
                onNavigateToBottomNav = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Nomor dokumen wajib diisi").assertIsDisplayed()
        composeTestRule.onNodeWithText("Uraian dokumen wajib diisi").assertIsDisplayed()
    }

    @Test
    fun testCopyCountDisabledWhenOriginal() {
        val viewModel = mockk<RapidInputViewModel>(relaxed = true)
        val state = MutableStateFlow(RapidInputUiState(
            copyType = DocCopyType.ORIGINAL,
            copyCount = "1"
        ))
        every { viewModel.uiState } returns state

        composeTestRule.setContent {
            RapidInputScreen(
                sessionId = "test-session",
                onNavigateBack = {},
                onNavigateToScan = {},
                onNavigateToBottomNav = {},
                viewModel = viewModel
            )
        }

        // When ORIGINAL, the copy count field is NOT even shown based on RapidInputScreen.kt:606
        // if (uiState.copyType == DocCopyType.COPY) { ... }
        composeTestRule.onNodeWithText("Jumlah Salinan").assertDoesNotExist()
    }

    @Test
    fun testCopyCountEnabledWhenCopy() {
        val viewModel = mockk<RapidInputViewModel>(relaxed = true)
        val state = MutableStateFlow(RapidInputUiState(
            copyType = DocCopyType.COPY,
            copyCount = "2"
        ))
        every { viewModel.uiState } returns state

        composeTestRule.setContent {
            RapidInputScreen(
                sessionId = "test-session",
                onNavigateBack = {},
                onNavigateToScan = {},
                onNavigateToBottomNav = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Jumlah Salinan").assertIsDisplayed()
    }

    @Test
    fun testAutoBundleToggleShowsSpmField() {
        val viewModel = mockk<RapidInputViewModel>(relaxed = true)
        val state = MutableStateFlow(RapidInputUiState(
            docType = DocType.SP2D,
            isAutoBundleEnabled = true
        ))
        every { viewModel.uiState } returns state

        composeTestRule.setContent {
            RapidInputScreen(
                sessionId = "test-session",
                onNavigateBack = {},
                onNavigateToScan = {},
                onNavigateToBottomNav = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Nomor SPM").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nomor SP2D").assertIsDisplayed()
    }
}
