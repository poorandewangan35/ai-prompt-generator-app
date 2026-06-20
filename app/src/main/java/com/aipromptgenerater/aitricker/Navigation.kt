package com.aipromptgenerater.aitricker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Person
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
import com.aipromptgenerater.aitricker.ui.profile.ProfileScreen

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
                            icon = { Icon(Icons.Default.History, contentDescription = "History") },
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
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                            label = { Text("Wallet") },
                            selected = currentKey == Wallet,
                            onClick = {
                                if (currentKey != Wallet) {
                                    backStack.clear()
                                    backStack.add(Wallet)
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = currentKey == Profile,
                            onClick = {
                                if (currentKey != Profile) {
                                    backStack.clear()
                                    backStack.add(Profile)
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    } else if (backStack.lastOrNull() != Home) {
                        backStack.clear()
                        backStack.add(Home)
                    }
                },
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
                            onNavigateBack = {
                                if (backStack.size > 1) {
                                    backStack.removeLastOrNull()
                                } else {
                                    backStack.clear()
                                    backStack.add(Home)
                                }
                            }
                        )
                    }
                    entry<Wallet> {
                        WalletScreen(
                            onNavigateBack = {
                                if (backStack.size > 1) {
                                    backStack.removeLastOrNull()
                                } else {
                                    backStack.clear()
                                    backStack.add(Home)
                                }
                            }
                        )
                    }
                    entry<History> {
                        HistoryScreen(
                            onNavigateBack = {
                                if (backStack.size > 1) {
                                    backStack.removeLastOrNull()
                                } else {
                                    backStack.clear()
                                    backStack.add(Home)
                                }
                            }
                        )
                    }
                    entry<Profile> {
                        ProfileScreen(
                            onNavigateToWallet = { backStack.add(Wallet) }
                        )
                    }
                }
            )
        }
    }
}
