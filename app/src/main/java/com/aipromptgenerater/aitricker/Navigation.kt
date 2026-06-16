package com.aipromptgenerater.aitricker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.aipromptgenerater.aitricker.ui.auth.AuthScreen
import com.aipromptgenerater.aitricker.ui.auth.AuthUiState
import com.aipromptgenerater.aitricker.ui.auth.AuthViewModel
import com.aipromptgenerater.aitricker.ui.generator.GeneratorScreen
import com.aipromptgenerater.aitricker.ui.history.HistoryScreen
import com.aipromptgenerater.aitricker.ui.home.HomeScreen
import com.aipromptgenerater.aitricker.ui.wallet.WalletScreen

@Composable
fun MainNavigation(
    authViewModel: AuthViewModel = viewModel { AuthViewModel() }
) {
    val currentUser by authViewModel.currentUserState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()

    // Treat mock success or real Firebase user as authenticated
    val isAuthenticated = currentUser != null || authUiState is AuthUiState.SuccessMock

    if (!isAuthenticated) {
        AuthScreen(onAuthSuccess = { /* AuthState listener handles transition */ })
    } else {
        val backStack = rememberNavBackStack(Home)
        val currentKey = backStack.lastOrNull() ?: Home

        Scaffold(
            bottomBar = {
                // Hide bottom navigation when inside the generator screen
                if (currentKey !is Generator) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = currentKey == Home,
                            onClick = {
                                if (currentKey != Home) {
                                    backStack.clear()
                                    backStack.add(Home)
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.List, contentDescription = "History") },
                            label = { Text("History") },
                            selected = currentKey == History,
                            onClick = {
                                if (currentKey != History) {
                                    backStack.clear()
                                    backStack.add(History)
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Wallet") },
                            label = { Text("Wallet") },
                            selected = currentKey == Wallet,
                            onClick = {
                                if (currentKey != Wallet) {
                                    backStack.clear()
                                    backStack.add(Wallet)
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                entryProvider = entryProvider {
                    entry<Home> {
                        HomeScreen(
                            onNavigateToGenerator = { type -> backStack.add(Generator(type)) },
                            onNavigateToWallet = { backStack.add(Wallet) }
                        )
                    }
                    entry<Generator> { key ->
                        GeneratorScreen(
                            generatorType = key.type,
                            onNavigateBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<Wallet> {
                        WalletScreen(
                            onNavigateBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<History> {
                        HistoryScreen(
                            onNavigateBack = { backStack.removeLastOrNull() }
                        )
                    }
                }
            )
        }
    }
}
