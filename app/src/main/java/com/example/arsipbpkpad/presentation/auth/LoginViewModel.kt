package com.example.arsipbpkpad.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalErrorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Pre-fill credentials if Remember Me is enabled
        if (authRepository.isRememberMeEnabled()) {
            _uiState.update { 
                it.copy(
                    email = authRepository.getSavedEmail() ?: "",
                    password = authRepository.getSavedPassword() ?: "",
                    rememberMe = true
                )
            }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onRememberMeChange(checked: Boolean) {
        _uiState.update { it.copy(rememberMe = checked) }
    }

    fun authenticateAdmin() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val rememberMe = _uiState.value.rememberMe

        var hasError = false
        
        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email tidak boleh kosong.") }
            hasError = true
        } else {
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
            if (!email.matches(emailRegex)) {
                _uiState.update { it.copy(emailError = "Format email tidak valid.") }
                hasError = true
            }
        }

        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password tidak boleh kosong.") }
            hasError = true
        } else if (password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password minimal 6 karakter.") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalErrorMessage = null) }
            val result = authRepository.login(email, password, rememberMe)
            
            when (result) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
                }
                is DomainResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, generalErrorMessage = result.message) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(generalErrorMessage = null) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // After logout, reset UI state but keep credentials if remember me is active
            _uiState.update { 
                LoginUiState(
                    email = authRepository.getSavedEmail() ?: "",
                    password = authRepository.getSavedPassword() ?: "",
                    rememberMe = authRepository.isRememberMeEnabled()
                )
            }
        }
    }
}
