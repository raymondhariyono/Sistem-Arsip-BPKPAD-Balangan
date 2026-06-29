package com.example.arsipbpkpad.data.repository

import android.content.SharedPreferences
import com.example.arsipbpkpad.data.mapper.toDomain
import com.example.arsipbpkpad.data.remote.dto.UserProfileDto
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.model.UserProfile
import com.example.arsipbpkpad.domain.model.UserRole
import com.example.arsipbpkpad.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val sharedPreferences: SharedPreferences
) : AuthRepository {

    private val _currentUserRole = MutableStateFlow(UserRole.UNKNOWN)
    override val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    override val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(false)
    override val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _isSessionChecked = MutableStateFlow(false)
    override val isSessionChecked: StateFlow<Boolean> = _isSessionChecked.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        android.util.Log.e("AuthRepository", "Unhandled exception in background task: ${throwable.message}", throwable)
    }
    
    private val repositoryScope = CoroutineScope(Dispatchers.Main + exceptionHandler)

    init {
        // Check session on initialization
        repositoryScope.launch {
            checkSession()
        }
    }

    override suspend fun login(email: String, password: String, rememberMe: Boolean): DomainResult<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val user = supabase.auth.currentUserOrNull() ?: throw Exception("Login failed: User not found")
            
            // Load profile from user_profiles table
            val profileResult = loadUserProfile(user.id)
            if (profileResult is DomainResult.Error) {
                // If profile is missing or inactive, sign out immediately
                supabase.auth.signOut()
                sharedPreferences.edit().putBoolean("remember_me", false).apply()
                _currentUserRole.value = UserRole.UNKNOWN
                _currentUserProfile.value = null
                _isUserLoggedIn.value = false
                return DomainResult.Error(profileResult.message)
            }

            val profile = (profileResult as DomainResult.Success).data
            
            // Save remember me preference and credentials if enabled
            if (rememberMe) {
                sharedPreferences.edit()
                    .putBoolean("remember_me", true)
                    .putString("saved_email", email)
                    .putString("saved_password", password)
                    .apply()
            } else {
                sharedPreferences.edit()
                    .putBoolean("remember_me", false)
                    .remove("saved_email")
                    .remove("saved_password")
                    .apply()
            }
            
            _currentUserProfile.value = profile
            _currentUserRole.value = profile.role
            _isUserLoggedIn.value = true
            
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            // Ensure remember me is NOT true on failure
            sharedPreferences.edit().putBoolean("remember_me", false).apply()

            val message = when {
                e is UnknownHostException || e is HttpRequestTimeoutException -> 
                    "Koneksi internet bermasalah. Silakan periksa jaringan Anda."
                e is ResponseException && e.response.status.value == 400 -> 
                    "Email atau password salah."
                e is ResponseException && e.response.status.value == 429 ->
                    "Terlalu banyak percobaan masuk. Silakan tunggu beberapa saat."
                e.message?.contains("invalid_credentials", ignoreCase = true) == true ->
                    "Email atau password salah."
                else -> "Gagal masuk. Pastikan kredensial benar atau hubungi admin."
            }
            DomainResult.Error(message)
        }
    }

    override suspend fun logout(): DomainResult<Unit> {
        return try {
            supabase.auth.signOut()
            // Clear local state
            clearLocalAuth()
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            // Even if signout fails, clear local state
            clearLocalAuth()
            DomainResult.Error(e.localizedMessage ?: "Logout failed")
        }
    }

    private fun clearLocalAuth() {
        sharedPreferences.edit()
            .putBoolean("remember_me", false)
            .remove("saved_email")
            .remove("saved_password")
            .apply()
        _currentUserRole.value = UserRole.UNKNOWN
        _currentUserProfile.value = null
        _isUserLoggedIn.value = false
    }

    override suspend fun checkSession(): Boolean {
        return try {
            android.util.Log.d("AuthRepository", "Checking session...")
            
            // Wait for session initialization
            supabase.auth.sessionStatus.first { it !is SessionStatus.Initializing }

            val rememberMe = sharedPreferences.getBoolean("remember_me", false)
            val session = supabase.auth.currentSessionOrNull()
            val user = supabase.auth.currentUserOrNull() ?: session?.user

            android.util.Log.d("AuthRepository", "Session found: ${session != null}, User found: ${user != null}, RememberMe: $rememberMe")

            if (!rememberMe) {
                if (session != null) {
                    android.util.Log.i("AuthRepository", "RememberMe is disabled but session exists. Signing out.")
                    try {
                        supabase.auth.signOut()
                    } catch (e: Exception) {
                        android.util.Log.w("AuthRepository", "SignOut failed during session check: ${e.message}")
                    }
                }
                clearLocalAuth()
                _isSessionChecked.value = true
                return false
            }

            if (session == null || user == null) {
                android.util.Log.d("AuthRepository", "No active session or user found for RememberMe.")
                clearLocalAuth()
                _isSessionChecked.value = true
                return false
            }

            // Try to load profile
            val profileResult = loadUserProfile(user.id)
            val isValid = if (profileResult is DomainResult.Success) {
                val profile = profileResult.data
                _currentUserProfile.value = profile
                _currentUserRole.value = profile.role
                _isUserLoggedIn.value = true
                android.util.Log.i("AuthRepository", "Session validated successfully for ${profile.email}")
                true
            } else if (profileResult is DomainResult.Error) {
                val isNetworkError = profileResult.message.contains("koneksi", ignoreCase = true) || 
                                    profileResult.message.contains("internet", ignoreCase = true)
                
                if (isNetworkError) {
                    // Keep session if it's just a network error
                    // We assume user is logged in but profile is pending
                    _isUserLoggedIn.value = true
                    android.util.Log.w("AuthRepository", "Network error during profile load, keeping session.")
                    true
                } else {
                    // Profile missing or inactive, sign out
                    android.util.Log.e("AuthRepository", "Profile validation failed: ${profileResult.message}. Signing out.")
                    try {
                        supabase.auth.signOut()
                    } catch (e: Exception) {}
                    clearLocalAuth()
                    false
                }
            } else {
                false
            }

            _isSessionChecked.value = true
            isValid
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Critical error in checkSession: ${e.message}", e)
            _isSessionChecked.value = true
            false
        }
    }

    override fun getCurrentUserId(): String? {
        val user = supabase.auth.currentUserOrNull()
        val session = supabase.auth.currentSessionOrNull()
        return user?.id ?: session?.user?.id
    }

    override fun getCurrentUserEmail(): String? {
        val user = supabase.auth.currentUserOrNull()
        val session = supabase.auth.currentSessionOrNull()
        return user?.email ?: session?.user?.email ?: _currentUserProfile.value?.email
    }

    override fun getCurrentUserFullName(): String? {
        return _currentUserProfile.value?.fullName
    }

    override fun getSavedEmail(): String? = sharedPreferences.getString("saved_email", null)

    override fun getSavedPassword(): String? = sharedPreferences.getString("saved_password", null)

    override fun isRememberMeEnabled(): Boolean = sharedPreferences.getBoolean("remember_me", false)

    private suspend fun loadUserProfile(userId: String): DomainResult<UserProfile> {
        return try {
            val profileDto = supabase.postgrest["user_profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<UserProfileDto>()

            if (profileDto == null) {
                return DomainResult.Error("Profil pengguna tidak ditemukan. Hubungi administrator.")
            }

            if (!profileDto.isActive) {
                return DomainResult.Error("Akun tidak aktif. Hubungi administrator.")
            }

            DomainResult.Success(profileDto.toDomain())
        } catch (e: Exception) {
            val message = when {
                e is UnknownHostException || e is HttpRequestTimeoutException -> 
                    "Gagal memuat profil pengguna. Periksa koneksi internet Anda."
                else -> "Gagal memuat profil pengguna. Silakan coba lagi nanti."
            }
            DomainResult.Error(message)
        }
    }
}
