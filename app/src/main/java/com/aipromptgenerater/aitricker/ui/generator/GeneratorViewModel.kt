package com.aipromptgenerater.aitricker.ui.generator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipromptgenerater.aitricker.data.model.PromptHistory
import com.aipromptgenerater.aitricker.data.model.UserProfile
import com.aipromptgenerater.aitricker.data.repository.AuthRepository
import com.aipromptgenerater.aitricker.data.repository.PromptRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GeneratorViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val promptRepository: PromptRepository = PromptRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GeneratorUiState>(GeneratorUiState.Idle)
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    // Observe logged-in profile (credits)
    val currentUser = authRepository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser)

    @OptIn(ExperimentalCoroutinesApi::class)
    val userProfile: StateFlow<UserProfile?> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                authRepository.userProfileFlow(user.uid)
                    .catch { emit(null) }
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Executes the secure generative AI sequence and deducts credits.
     */
    fun generatePrompt(
        type: String, // "Website" or "App"
        name: String,
        idea: String,
        techStack: String,
        features: String
    ) {
        val user = currentUser.value
        if (user == null) {
            _uiState.value = GeneratorUiState.Error("User session not found. Please log in.")
            return
        }

        if (idea.trim().isBlank()) {
            _uiState.value = GeneratorUiState.Error("Idea description is required.")
            return
        }

        viewModelScope.launch {
            _uiState.value = GeneratorUiState.Loading
            
            // Check credits local snapshot first
            val currentCredits = userProfile.value?.credits ?: 0
            if (currentCredits < 5) {
                _uiState.value = GeneratorUiState.Error("Insufficient credits. You have $currentCredits credits. Generation requires 5 credits.")
                return@launch
            }

            val result = promptRepository.generateAndSavePrompt(
                userId = user.uid,
                type = type,
                name = name,
                idea = idea,
                techStack = techStack,
                features = features
            )

            if (result.isSuccess) {
                _uiState.value = GeneratorUiState.Success(result.getOrThrow())
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Prompt generation failed."
                _uiState.value = GeneratorUiState.Error(errorMsg)
            }
        }
    }

    fun resetState() {
        _uiState.value = GeneratorUiState.Idle
    }
}

sealed interface GeneratorUiState {
    object Idle : GeneratorUiState
    object Loading : GeneratorUiState
    data class Success(val promptHistory: PromptHistory) : GeneratorUiState
    data class Error(val message: String) : GeneratorUiState
}
