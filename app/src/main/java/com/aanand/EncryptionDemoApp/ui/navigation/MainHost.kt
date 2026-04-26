package com.aanand.EncryptionDemoApp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.aanand.EncryptionDemoApp.ui.screens.AndroidKeystoreScreen
import com.aanand.EncryptionDemoApp.ui.screens.ComparisonScreen
import com.aanand.EncryptionDemoApp.ui.screens.HomeScreen
import com.aanand.EncryptionDemoApp.ui.screens.KeyGeneratorScreen
import com.aanand.EncryptionDemoApp.ui.screens.TinkScreen

@Composable
fun MainHost() {
    val backStack = remember { mutableStateListOf<NavKey>(NavKey.Home) }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                entryProvider = { key ->
                    when (key) {
                        is NavKey.Home -> NavEntry(key) {
                            HomeScreen(onNavigate = { backStack.add(it) })
                        }
                        is NavKey.Tink -> NavEntry(key) {
                            TinkScreen(onBack = { backStack.removeAt(backStack.lastIndex) })
                        }
                        is NavKey.AndroidKeystore -> NavEntry(key) {
                            AndroidKeystoreScreen(onBack = { backStack.removeAt(backStack.lastIndex) })
                        }
                        is NavKey.KeyGenerator -> NavEntry(key) {
                            KeyGeneratorScreen(onBack = { backStack.removeAt(backStack.lastIndex) })
                        }
                        is NavKey.Comparison -> NavEntry(key) {
                            ComparisonScreen(onBack = { backStack.removeAt(backStack.lastIndex) })
                        }
                    }
                }
            )
        }
    }
}
