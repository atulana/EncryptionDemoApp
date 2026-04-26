package com.aanand.EncryptionDemoApp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aanand.EncryptionDemoApp.ui.navigation.NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (NavKey) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Encryption Demo") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                FeatureCard(
                    title = "Explore Tink",
                    description = "Click on this to explore Tink based encryption",
                    onClick = { onNavigate(NavKey.Tink) }
                )
            }
            item {
                FeatureCard(
                    title = "Explore Android Keystore",
                    description = "Click on this to explore Android keystore based encryption",
                    onClick = { onNavigate(NavKey.AndroidKeystore) }
                )
            }
            item {
                FeatureCard(
                    title = "Explore Keygenerator encryption",
                    description = "Click on this to explore Manual Keygenerator based encryption",
                    onClick = { onNavigate(NavKey.KeyGenerator) }
                )
            }
            item {
                FeatureCardComingSoon(
                    title = "Compare Timing",
                    description = "Compare Timing of various Encryption algorithms"
                    //onClick = { onNavigate(NavKey.Comparison) }
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun FeatureCardComingSoon(
    title: String,
    description: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Coming Soon!", style = MaterialTheme.typography.labelLarge, color = Color.Red)
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
