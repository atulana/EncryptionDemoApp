package com.aanand.EncryptionDemoApp.ui.screens

import android.security.keystore.KeyProperties
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aanand.EncryptionDemoApp.ui.composables.ConfigDropdown
import com.aanand.EncryptionDemoApp.viewmodels.ComparisonResult
import com.aanand.EncryptionDemoApp.viewmodels.ComparisonViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    onBack: () -> Unit,
    viewModel: ComparisonViewModel = koinViewModel()
) {
    val state by viewModel.state
    var algorithm by remember { mutableStateOf(KeyProperties.KEY_ALGORITHM_AES) }
    var blockMode by remember { mutableStateOf(KeyProperties.BLOCK_MODE_GCM) }
    var padding by remember { mutableStateOf(KeyProperties.ENCRYPTION_PADDING_NONE) }
    var inputSize by remember { mutableStateOf("1024") }

    val algorithms = listOf(
        KeyProperties.KEY_ALGORITHM_AES, 
        KeyProperties.KEY_ALGORITHM_RSA,
        "DESede"
    )
    
    val blockModes = remember(algorithm) {
        when (algorithm) {
            KeyProperties.KEY_ALGORITHM_RSA -> listOf(KeyProperties.BLOCK_MODE_ECB)
            KeyProperties.KEY_ALGORITHM_AES -> listOf(KeyProperties.BLOCK_MODE_GCM, KeyProperties.BLOCK_MODE_CBC, KeyProperties.BLOCK_MODE_CTR, KeyProperties.BLOCK_MODE_ECB)
            else -> listOf(KeyProperties.BLOCK_MODE_CBC, KeyProperties.BLOCK_MODE_ECB)
        }
    }

    val paddings = remember(algorithm, blockMode) {
        when (algorithm) {
            KeyProperties.KEY_ALGORITHM_RSA -> listOf(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1, KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            else -> {
                if (blockMode == KeyProperties.BLOCK_MODE_GCM) listOf(KeyProperties.ENCRYPTION_PADDING_NONE)
                else listOf(KeyProperties.ENCRYPTION_PADDING_PKCS7, KeyProperties.ENCRYPTION_PADDING_NONE)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Benchmark") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            
            Text("Select Config & Input Size (bytes)", style = MaterialTheme.typography.titleMedium)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    ConfigDropdown(
                        label = "Algo",
                        options = algorithms,
                        selectedOption = algorithm,
                        onOptionSelected = { algorithm = it },
                        helpTitle = "", helpContent = ""
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    ConfigDropdown(
                        label = "Mode",
                        options = blockModes,
                        selectedOption = blockMode,
                        onOptionSelected = { blockMode = it },
                        helpTitle = "", helpContent = ""
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    ConfigDropdown(
                        label = "Padding",
                        options = paddings,
                        selectedOption = padding,
                        onOptionSelected = { padding = it },
                        helpTitle = "", helpContent = ""
                    )
                }
                OutlinedTextField(
                    value = inputSize,
                    onValueChange = { inputSize = it.filter { char -> char.isDigit() } },
                    label = { Text("Size") },
                    modifier = Modifier.weight(1f).padding(start = 4.dp, top = 8.dp)
                )
            }

            Row(modifier = Modifier.padding(vertical = 16.dp)) {
                Button(
                    onClick = { viewModel.runBenchmark(algorithm, blockMode, padding, inputSize.toIntOrNull() ?: 1024) },
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                    enabled = !state.isRunning
                ) {
                    if (state.isRunning) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Run Benchmark")
                }
                Button(
                    onClick = { viewModel.clearResults() },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("Clear")
                }
            }

            HorizontalDivider()

            // Result Table
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                item { TableHeader() }
                items(state.results) { result ->
                    TableRow(result)
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun TableHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Method", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        Text("Enc (ms)", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Dec (ms)", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("HW", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TableRow(result: ComparisonResult) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.5f)) {
            Text(result.method, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text("${result.algorithm} (${result.inputSize}b)", style = MaterialTheme.typography.labelSmall)
            if (result.error.isNotEmpty()) Text(result.error, color = Color.Red, style = MaterialTheme.typography.labelSmall)
        }
        Text("%.3f".format(result.encryptTimeMs), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text("%.3f".format(result.decryptTimeMs), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(if (result.isHardware) "✅" else "❌", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.bodySmall)
    }
}
