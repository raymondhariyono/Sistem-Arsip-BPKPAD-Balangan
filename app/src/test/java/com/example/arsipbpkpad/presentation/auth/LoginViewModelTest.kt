package com.example.arsipbpkpad.presentation.auth

import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class LoginViewModelTest {

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LGN_001 - login successful with valid credentials`() = runTest {
        viewModel.onEmailChange("valid@example.com")
        viewModel.onPasswordChange("password")
        
        coEvery { authRepository.login(any(), any(), any()) } returns DomainResult.Success(Unit)

        viewModel.authenticateAdmin()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Login should be successful, but error was: ${state.errorMessage}", state.isLoginSuccessful)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `LGN_003 - login failure with invalid credentials`() = runTest {
        viewModel.onEmailChange("wrong@example.com")
        viewModel.onPasswordChange("wrong")

        coEvery { authRepository.login(any(), any(), any()) } returns DomainResult.Error("Gagal masuk: Invalid credentials")

        viewModel.authenticateAdmin()
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoginSuccessful)
        assertTrue(state.errorMessage?.contains("Gagal masuk") == true)
    }

    @Test
    fun `LGN_006 - login fails when fields are empty`() = runTest {
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")

        viewModel.authenticateAdmin()
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Email dan password tidak boleh kosong.", state.errorMessage)
    }

    @Test
    fun `LGN_007 - logout resets UI state`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")
        viewModel.onRememberMeChange(true)

        coEvery { authRepository.logout() } returns DomainResult.Success(Unit)

        viewModel.logout()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals(false, state.rememberMe)
        assertEquals(false, state.isLoginSuccessful)
    }
}
