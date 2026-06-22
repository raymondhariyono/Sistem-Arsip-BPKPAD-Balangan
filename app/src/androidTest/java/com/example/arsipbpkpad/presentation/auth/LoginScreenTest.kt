package com.example.arsipbpkpad.presentation.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.arsipbpkpad.presentation.auth.screen.LoginScreen
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginButtonShowsLoadingIndicator() {
        val viewModel = mockk<LoginViewModel>(relaxed = true)
        val state = LoginUiState(isLoading = true)
        every { viewModel.uiState } returns MutableStateFlow(state)

        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, viewModel = viewModel)
        }

        // CircularProgressIndicator doesn't have text, but the "Log In" button text is hidden when loading
        composeTestRule.onNodeWithText("Log In").assertDoesNotExist()
    }

    @Test
    fun testEnteringCredentials() {
        val viewModel = mockk<LoginViewModel>(relaxed = true)
        val state = MutableStateFlow(LoginUiState(email = "", password = ""))
        every { viewModel.uiState } returns state

        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Email Anda").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password Anda").performTextInput("password123")

        composeTestRule.onNodeWithText("Log In").performClick()
    }

    @Test
    fun testErrorMessageDisplay() {
        val viewModel = mockk<LoginViewModel>(relaxed = true)
        val state = MutableStateFlow(LoginUiState(errorMessage = "Email atau password yang Anda masukkan salah."))
        every { viewModel.uiState } returns state

        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Login Gagal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email atau password yang Anda masukkan salah.").assertIsDisplayed()
    }
}
