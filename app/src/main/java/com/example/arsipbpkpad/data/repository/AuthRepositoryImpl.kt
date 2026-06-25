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
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import io.ktor.client.network.sockets.SocketTimeoutException as KtorSocketTimeoutException

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

    private val repositoryScope = CoroutineScope(Dispatchers.Main)

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
            
            // Save remember me preference
            sharedPreferences.edit().putBoolean("remember_me", rememberMe).apply()
            
            _currentUserProfile.value = profile
            _currentUserRole.value = profile.role
            _isUserLoggedIn.value = true
            
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            // Ensure remember me is NOT true on failure
            sharedPreferences.edit().putBoolean("remember_me", false).apply()

            val message = when {
                e is UnknownHostException || e is HttpRequestTimeoutException || 
                e is ConnectException || e is SocketTimeoutException ||
                e is ConnectTimeoutException || e is KtorSocketTimeoutException -> 
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
            // Clear remember me preference
            sharedPreferences.edit().putBoolean("remember_me", false).apply()
            
            _currentUserRole.value = UserRole.UNKNOWN
            _currentUserProfile.value = null
            _isUserLoggedIn.value = false
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            DomainResult.Error(e.localizedMessage ?: "Logout failed")
        }
    }

    override suspend fun checkSession(): Boolean {
        val rememberMe = sharedPreferences.getBoolean("remember_me", false)
        val session = supabase.auth.currentSessionOrNull()
        val user = supabase.auth.currentUserOrNull()
        
        val isValid = if (session != null && user != null) {
            if (rememberMe) {
                val profileResult = loadUserProfile(user.id)
                if (profileResult is DomainResult.Success) {
                    val profile = profileResult.data
                    _currentUserProfile.value = profile
                    _currentUserRole.value = profile.role
                    _isUserLoggedIn.value = true
                    true
                } else {
                    // Profile missing or inactive, sign out
                    try {
                        supabase.auth.signOut()
                    } catch (e: Exception) {}
                    sharedPreferences.edit().putBoolean("remember_me", false).apply()
                    _currentUserRole.value = UserRole.UNKNOWN
                    _currentUserProfile.value = null
                    _isUserLoggedIn.value = false
                    false
                }
            } else {
                // If session exists but rememberMe is false, clear it
                try {
                    supabase.auth.signOut()
                } catch (e: Exception) {
                    // Ignore signout errors during session clearing
                }
                _currentUserRole.value = UserRole.UNKNOWN
                _currentUserProfile.value = null
                _isUserLoggedIn.value = false
                false
            }
        } else {
            _currentUserRole.value = UserRole.UNKNOWN
            _currentUserProfile.value = null
            _isUserLoggedIn.value = false
            false
        }
        
        _isSessionChecked.value = true
        return isValid
    }

    override fun getCurrentUserId(): String? {
        val user = supabase.auth.currentUserOrNull()
        val session = supabase.auth.currentSessionOrNull()
        val userId = user?.id ?: session?.user?.id
        return userId
    }

    override fun getCurrentUserEmail(): String? {
        return _currentUserProfile.value?.email
            ?: supabase.auth.currentUserOrNull()?.email
            ?: supabase.auth.currentSessionOrNull()?.user?.email
    }

    override fun getCurrentUserFullName(): String? {
        return _currentUserProfile.value?.fullName
    }

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
                e is UnknownHostException || e is HttpRequestTimeoutException ||
                e is ConnectException || e is SocketTimeoutException ||
                e is ConnectTimeoutException || e is KtorSocketTimeoutException ->
                    "Gagal memuat profil pengguna. Periksa koneksi internet Anda."
                else -> "Gagal memuat profil pengguna. Silakan coba lagi nanti."
            }
            DomainResult.Error(message)
        }
    }
}
