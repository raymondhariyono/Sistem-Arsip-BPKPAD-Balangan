package com.example.arsipbpkpad.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject

data class LoginUiState(
    val email: String = "admin123@balangankab.go.id", // For testing only
    val password: String = "arsiparis321", // For testing only
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun authenticateAdmin() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email dan password tidak boleh kosong.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
            } catch (e: Exception) {
                val userFriendlyMessage = when (e) {
                    is UnknownHostException, is HttpRequestTimeoutException -> 
                        "Koneksi terputus. Silakan periksa jaringan Anda."
                    is ResponseException -> {
                        if (e.response.status.value == 400) {
                            "Email atau password yang Anda masukkan salah."
                        } else {
                            "Terjadi kesalahan pada server. Silakan coba lagi nanti."
                        }
                    }
                    else -> "Gagal masuk: ${e.localizedMessage ?: "Kesalahan tidak dikenal"}"
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = userFriendlyMessage) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
