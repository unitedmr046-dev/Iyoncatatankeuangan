package com.finora.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String = "", val icon: ImageVector? = null) {
    data object Dashboard : Screen("dashboard", "Beranda", Icons.Default.Home)
    data object Wallets : Screen("wallets", "Dompet", Icons.Default.AccountBalanceWallet)
    data object Budget : Screen("budget", "Rencana", Icons.Default.Savings)
    data object Reports : Screen("reports", "Laporan", Icons.Default.BarChart)
    data object Profile : Screen("profile", "Profil", Icons.Default.Person)
    data object AddTransaction : Screen("add_transaction")
    data object Scanner : Screen("scanner")

    companion object {
        val bottomNavItems = listOf(Dashboard, Wallets, Budget, Reports, Profile)
    }
}
