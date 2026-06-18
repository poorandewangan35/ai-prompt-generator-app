package com.aipromptgenerater.aitricker.ui.wallet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipromptgenerater.aitricker.data.model.UserProfile
import com.aipromptgenerater.aitricker.data.repository.AuthRepository
import com.aipromptgenerater.aitricker.data.repository.PaymentRepository
import com.aipromptgenerater.aitricker.data.repository.PaymentRepository.PaymentPlan
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WalletViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _plans = MutableStateFlow<List<PaymentPlan>>(emptyList())
    val plans: StateFlow<List<PaymentPlan>> = _plans.asStateFlow()

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

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

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            val list = paymentRepository.getPaymentPlans()
            _plans.value = list
        }
    }

    /**
     * Launches checkout via Razorpay or Cashfree.
     */
    fun purchasePlan(context: Context, plan: PaymentPlan, gateway: String, isSandbox: Boolean) {
        val user = currentUser.value
        if (user == null) {
            _paymentState.value = PaymentState.Error("Session expired. Please log in.")
            return
        }

        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            paymentRepository.checkout(
                context = context,
                userId = user.uid,
                plan = plan,
                gateway = gateway,
                isSandboxMode = isSandbox,
                onSuccess = { successMessage ->
                    _paymentState.value = PaymentState.Success(successMessage)
                },
                onFailure = { errorMessage ->
                    _paymentState.value = PaymentState.Error(errorMessage)
                }
            )
        }
    }

    fun resetState() {
        _paymentState.value = PaymentState.Idle
    }
}

sealed interface PaymentState {
    object Idle : PaymentState
    object Loading : PaymentState
    data class Success(val message: String) : PaymentState
    data class Error(val error: String) : PaymentState
}
