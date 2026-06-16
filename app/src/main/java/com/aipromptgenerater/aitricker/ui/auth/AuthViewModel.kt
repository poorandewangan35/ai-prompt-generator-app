package com.aipromptgenerater.aitricker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipromptgenerater.aitricker.data.model.UserProfile
import com.aipromptgenerater.aitricker.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Observe active user state
    val currentUserState: StateFlow<FirebaseUser?> = authRepository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser)

    // Dynamically retrieve user profile (e.g. credits) when logged in
    @OptIn(ExperimentalCoroutinesApi::class)
    val userProfile: StateFlow<UserProfile?> = currentUserState
        .flatMapLatest { user ->
            if (user != null) {
                authRepository.userProfileFlow(user.uid)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Authenticate or register with Email + Password
     */
    fun signInWithEmail(email: String, authCode: String, isSignUp: Boolean) {
        if (email.isBlank() || authCode.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val firebaseAuth = FirebaseAuth.getInstance()
                val result = if (isSignUp) {
                    firebaseAuth.createUserWithEmailAndPassword(email, authCode).await()
                } else {
                    firebaseAuth.signInWithEmailAndPassword(email, authCode).await()
                }

                val user = result.user
                if (user != null) {
                    // Force initialize Firestore profile with 15 credits
                    authRepository.checkAndCreateUserProfile(user)
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Sign in failed. No user object returned.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    /**
     * Firebase Google sign-in helper.
     * Takes credential from Google Sign-In SDK and signs in securely.
     */
    fun signInWithGoogle(credential: com.google.firebase.auth.AuthCredential) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    authRepository.checkAndCreateUserProfile(user)
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Google sign in failed.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Google Sign-In failed")
            }
        }
    }

    /**
     * Simulated sign-in for quick offline sandbox testing.
     */
    fun signInMock() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // Simulate network call
                kotlinx.coroutines.delay(1000)
                _uiState.value = AuthUiState.SuccessMock
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Mock Sign-In failed")
            }
        }
    }

    fun logout() {
        authRepository.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: FirebaseUser) : AuthUiState
    object SuccessMock : AuthUiState // Used for sandbox testing/fallback
    data class Error(val message: String) : AuthUiState
}
