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
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onRememberMeChange(checked: Boolean) {
        _uiState.update { it.copy(rememberMe = checked) }
    }

    fun authenticateAdmin() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val rememberMe = _uiState.value.rememberMe

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email dan password tidak boleh kosong.") }
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Format email tidak valid.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(email, password, rememberMe)
            
            when (result) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
                }
                is DomainResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
