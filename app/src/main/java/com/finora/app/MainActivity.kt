package com.finora.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finora.app.presentation.navigation.Screen
import com.finora.app.presentation.screens.*
import com.finora.app.presentation.screens.scanner.ReceiptScannerScreen
import com.finora.app.presentation.theme.FinoraTheme
import com.finora.app.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinoraRoot()
        }
    }
}

@Composable
private fun FinoraRoot(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-lock jika aplikasi berada di background > 15 detik
    var backgroundTimestamp by remember { mutableLongStateOf(0L) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    backgroundTimestamp = System.currentTimeMillis()
                }
                Lifecycle.Event.ON_START -> {
                    if (backgroundTimestamp > 0L &&
                        System.currentTimeMillis() - backgroundTimestamp > 15_000L
                    ) {
                        viewModel.lockAppIfEnabled()
                    }
                    backgroundTimestamp = 0L
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val showBottomBar = Screen.bottomNavItems.any { it.route == currentRoute }

    FinoraTheme(darkTheme = uiState.isDarkMode) {
        if (uiState.isAppLocked) {
            LockScreen(
                isBiometricEnabled = uiState.isBiometricEnabled,
                onUnlockWithPin = { pin -> viewModel.unlockWithPin(pin) },
                onUnlockWithBiometric = { viewModel.unlockWithBiometric() }
            )
        } else {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar {
                            Screen.bottomNavItems.forEach { screen ->
                                val selected = currentDestination?.hierarchy?.any {
                                    it.route == screen.route
                                } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        screen.icon?.let { Icon(it, contentDescription = screen.label) }
                                    },
                                    label = { Text(screen.label, maxLines = 1) }
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if (currentRoute == Screen.Dashboard.route) {
                        FloatingActionButton(
                            onClick = { navController.navigate(Screen.AddTransaction.route) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route,
                    modifier = Modifier.padding(
                        bottom = innerPadding.calculateBottomPadding()
                    )
                ) {
                    composable(Screen.Dashboard.route) {
                        DashboardScreen(
                            viewModel = viewModel,
                            onAddTransactionClick = {
                                navController.navigate(Screen.AddTransaction.route)
                            }
                        )
                    }

                    composable(Screen.Wallets.route) {
                        WalletScreen(viewModel = viewModel)
                    }

                    composable(Screen.Budget.route) {
                        BudgetScreen(viewModel = viewModel)
                    }

                    composable(Screen.Reports.route) {
                        ReportsScreen(viewModel = viewModel)
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(viewModel = viewModel)
                    }

                    composable(Screen.AddTransaction.route) { backStackEntry ->
                        val scannedAmount by backStackEntry.savedStateHandle
                            .getStateFlow<Double?>("scanned_amount", null)
                            .collectAsStateWithLifecycle()

                        AddTransactionScreen(
                            viewModel = viewModel,
                            scannedAmount = scannedAmount,
                            onScannedAmountConsumed = {
                                backStackEntry.savedStateHandle.remove<Double>("scanned_amount")
                            },
                            onSuccess = { navController.popBackStack() },
                            onBack = { navController.popBackStack() },
                            onScanClick = { navController.navigate(Screen.Scanner.route) }
                        )
                    }

                    composable(Screen.Scanner.route) {
                        ReceiptScannerScreen(
                            onAmountScanned = { amount ->
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("scanned_amount", amount)
                                navController.popBackStack()
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
