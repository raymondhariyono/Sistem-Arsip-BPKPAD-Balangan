package com.example.arsipbpkpad.presentation.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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

    private val supabase = mockk<SupabaseClient>()
    private val auth = mockk<Auth>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("io.github.jan.supabase.auth.AuthKt")
        every { supabase.auth } returns auth
        viewModel = LoginViewModel(supabase)
    }

    @After
    fun tearDown() {
        unmockkStatic("io.github.jan.supabase.auth.AuthKt")
        Dispatchers.resetMain()
    }

    @Test
    fun `LGN_001 - login successful with valid credentials`() = runTest {
        viewModel.onEmailChange("valid@example.com")
        viewModel.onPasswordChange("password")
        
        // Mocking the extension function might be tricky. 
        // Let's try to mock the internal call if possible, 
        // or ensure the extension is correctly mocked.
        coEvery { auth.signInWith(any<Email>(), any(), any()) } returns Unit

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

        coEvery { auth.signInWith(any<Email>(), any(), any()) } throws Exception("Invalid credentials")

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
}
