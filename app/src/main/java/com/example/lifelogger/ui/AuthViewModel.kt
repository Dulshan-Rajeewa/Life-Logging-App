package com.example.lifelogger.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lifelogger.data.SupabaseHelper
import com.example.lifelogger.data.User
import com.example.lifelogger.data.UserDao
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * State class to represent different authentication phases
 */
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val userDao: UserDao) : ViewModel() {

    // Detailed state for the UI to show loaders or errors
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // Holds the current user info if logged in (from local DB)
    val currentUser: StateFlow<User?> = userDao.getLoggedInUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Creates a new account in the custom 'users' table
     */
    fun registerUser(nameInput: String, emailInput: String, passwordInput: String, onSuccess: () -> Unit) {
        if (nameInput.isBlank()) {
            _uiState.value = AuthUiState.Error("Full Name is required")
            return
        }
        if (emailInput.isBlank()) {
            _uiState.value = AuthUiState.Error("Email is required")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address")
            return
        }
        if (passwordInput.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // Check if user already exists in Supabase users table
                val existing = SupabaseHelper.client.postgrest["users"]
                    .select {
                        filter {
                            eq("email", emailInput)
                        }
                    }.decodeList<User>()

                if (existing.isNotEmpty()) {
                    _uiState.value = AuthUiState.Error("This email is already registered")
                    return@launch
                }

                val newUser = User(
                    fullName = nameInput,
                    email = emailInput,
                    password = passwordInput,
                    isLoggedIn = true
                )

                // 1. Save to Supabase custom table
                // Since isLoggedIn is @Transient, it won't be sent to Supabase
                SupabaseHelper.client.postgrest["users"].insert(newUser)
                
                // 2. Save locally for session
                userDao.logoutAll()
                userDao.insertUser(newUser)
                
                _uiState.value = AuthUiState.Success("Account created! Welcome, $nameInput")
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Registration failed: ${e.message}")
            }
        }
    }

    /**
     * Logs into an account using the custom 'users' table
     */
    fun loginUser(emailInput: String, passwordInput: String, onSuccess: () -> Unit) {
        if (emailInput.isBlank() || passwordInput.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and Password are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // Query Supabase users table
                val results = SupabaseHelper.client.postgrest["users"]
                    .select {
                        filter {
                            eq("email", emailInput)
                            eq("password", passwordInput)
                        }
                    }.decodeList<User>()

                if (results.isNotEmpty()) {
                    val user = results[0].copy(isLoggedIn = true)
                    
                    // Save locally for session
                    userDao.logoutAll()
                    userDao.insertUser(user)
                    
                    _uiState.value = AuthUiState.Success("Login successful!")
                    onSuccess()
                } else {
                    _uiState.value = AuthUiState.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Login failed: ${e.message}")
            }
        }
    }

    /**
     * Signs the user out locally
     */
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            userDao.logoutAll()
            _uiState.value = AuthUiState.Idle
            onLoggedOut()
        }
    }
    
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

class AuthViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}