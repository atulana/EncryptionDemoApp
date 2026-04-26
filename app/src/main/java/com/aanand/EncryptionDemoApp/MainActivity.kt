package com.aanand.EncryptionDemoApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aanand.EncryptionDemoApp.ui.navigation.MainHost
import com.aanand.EncryptionDemoApp.ui.theme.EncryptionDemoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EncryptionDemoAppTheme {
                MainHost()
            }
        }
    }
}
