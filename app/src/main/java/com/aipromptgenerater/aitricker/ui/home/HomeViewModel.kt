package com.aipromptgenerater.aitricker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipromptgenerater.aitricker.data.model.PromptHistory
import com.aipromptgenerater.aitricker.data.model.UserProfile
import com.aipromptgenerater.aitricker.data.repository.AuthRepository
import com.aipromptgenerater.aitricker.data.repository.PromptRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class HomeViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val promptRepository: PromptRepository = PromptRepository()
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser)

    @OptIn(ExperimentalCoroutinesApi::class)
    val userProfile: StateFlow<UserProfile?> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                authRepository.userProfileFlow(user.uid)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentPrompts: StateFlow<List<PromptHistory>> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                promptRepository.promptHistoryFlow(user.uid).map { list ->
                    list.take(5) // Only take the 5 most recent prompts for the home preview
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun logout() {
        authRepository.signOut()
    }
}
