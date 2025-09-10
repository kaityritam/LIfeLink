package com.lifelink.lifelink.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelink.lifelink.data.User
import com.lifelink.lifelink.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _profileLoadingState = MutableStateFlow<ProfileLoadingState>(ProfileLoadingState.Idle)
    val profileLoadingState: StateFlow<ProfileLoadingState> = _profileLoadingState

    fun registerUser(
        email: String,
        password: String,
        name: String,
        phone: String,
        bloodGroup: String
    ) {
        _registrationState.value = RegistrationState.Loading
        viewModelScope.launch {
            val result = userRepository.registerUser(email, password, name, phone, bloodGroup)
            _registrationState.value = if (result.isSuccess) {
                RegistrationState.Success
            } else {
                RegistrationState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun loginUser(email: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = userRepository.loginUser(email, password)
            _loginState.value = if (result.isSuccess) {
                LoginState.Success
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }


    init {
        Log.d("AuthViewModel_DEBUG", "A new AuthViewModel instance has been created: ${this.hashCode()}")
    }

    fun fetchUserProfile() {
        Log.d("AuthViewModel_DEBUG", "fetchUserProfile() called.")
        if (_profileLoadingState.value == ProfileLoadingState.Loading) return

        _profileLoadingState.value = ProfileLoadingState.Loading
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()

            // Let's see if we are getting a user ID
            Log.d("AuthViewModel_DEBUG", "Retrieved User ID: $userId")

            if (userId != null) {
                val result = userRepository.getUserProfile(userId)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    Log.d("AuthViewModel_DEBUG", "Profile fetch success. User: $user")
                    _userProfile.value = user
                    _profileLoadingState.value = ProfileLoadingState.Success
                } else {
                    val error = result.exceptionOrNull()?.message
                    Log.e("AuthViewModel_DEBUG", "Profile fetch failed: $error")
                    _userProfile.value = null
                    _profileLoadingState.value = ProfileLoadingState.Error(error ?: "Failed to load profile")
                }
            } else {
                Log.w("AuthViewModel_DEBUG", "Cannot fetch profile, user ID is null.")
                _userProfile.value = null
                _profileLoadingState.value = ProfileLoadingState.Error("User not logged in.")
            }
        }
    }


    fun signOut() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.Loading
            val result = userRepository.logout() // Capture the result
            if (result.isSuccess) {
                _logoutState.value = LogoutState.Success
            } else {
                _logoutState.value = LogoutState.Error(
                    result.exceptionOrNull()?.message ?: "Logout failed"
                )
            }
        }
    }
    // Add this new function to your AuthViewModel
    fun onLogoutStateConsumed() {
        _logoutState.value = LogoutState.Idle
    }


    sealed class ProfileLoadingState {
        object Idle : ProfileLoadingState()
        object Loading : ProfileLoadingState()
        object Success : ProfileLoadingState()
        data class Error(val message: String) : ProfileLoadingState()
    }

    sealed class RegistrationState {
        object Idle : RegistrationState()
        object Loading : RegistrationState()
        object Success : RegistrationState()
        data class Error(val message: String) : RegistrationState()
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    sealed class LogoutState {
        object Idle : LogoutState()
        object Loading : LogoutState()
        object Success : LogoutState()
        data class Error(val message: String) : LogoutState()
    }

}