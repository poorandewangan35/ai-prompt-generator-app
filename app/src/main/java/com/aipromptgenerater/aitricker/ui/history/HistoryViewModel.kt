package com.aipromptgenerater.aitricker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipromptgenerater.aitricker.data.model.PromptHistory
import com.aipromptgenerater.aitricker.data.repository.AuthRepository
import com.aipromptgenerater.aitricker.data.repository.PromptRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class HistoryViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val promptRepository: PromptRepository = PromptRepository()
) : ViewModel() {

    val currentUser = authRepository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser)

    @OptIn(ExperimentalCoroutinesApi::class)
    val fullPromptHistory: StateFlow<List<PromptHistory>> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                promptRepository.promptHistoryFlow(user.uid)
                    .catch { emit(emptyList()) }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
