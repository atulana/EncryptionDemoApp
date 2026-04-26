package com.aanand.EncryptionDemoApp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavKey {
    @Serializable data object Home : NavKey
    @Serializable data object Tink : NavKey
    @Serializable data object AndroidKeystore : NavKey
    @Serializable data object KeyGenerator : NavKey
    @Serializable data object Comparison : NavKey
}
