package com.aanand.EncryptionDemoApp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.aanand.EncryptionDemoApp.ui.composables.ConfigDropdown
import com.aanand.EncryptionDemoApp.viewmodels.TinkViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TinkScreen(
    onBack: () -> Unit,
    viewModel: TinkViewModel = koinViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Tink") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Standard (AEAD/DHKEM)", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Isolated (Hybrid Simulation)", modifier = Modifier.padding(16.dp))
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                if (selectedTab == 0) {
                    StandardContent(viewModel, focusManager)
                } else {
                    IsolatedHybridContent(viewModel, focusManager)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardContent(viewModel: TinkViewModel, focusManager: androidx.compose.ui.focus.FocusManager) {
    var inputText by remember { mutableStateOf("") }
    var useCaseName by remember { mutableStateOf("GENERAL") }
    var associatedData by remember { mutableStateOf("") }
    var template by remember { mutableStateOf("AES256_GCM") }
    var result by remember { mutableStateOf("") }
    var decrypted by remember { mutableStateOf("") }
    var keysetInfo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val templates = listOf(
        "AES128_GCM", 
        "AES256_GCM", 
        "CHACHA20_POLY1305",
        "DHKEM_X25519_HKDF_SHA256_HKDF_SHA256_AES_256_GCM"
    )

    val isAsymmetric = template.startsWith("DH") || template.startsWith("ECIES")

    Text(
        text = if (isAsymmetric) "Asymmetric Encryption: Uses DHKEM-AES Hybrid to securely encrypt/decrypt data."
               else "Symmetric Encryption: Use high-level AEAD templates for fast, authenticated local encryption.",
        style = MaterialTheme.typography.bodyMedium
    )

    OutlinedTextField(
        value = useCaseName, 
        onValueChange = { useCaseName = it }, 
        label = { Text("Use Case (Unique ID)") }, 
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    )

    OutlinedTextField(value = inputText, onValueChange = { inputText = it }, label = { Text("Input Data") }, modifier = Modifier.fillMaxWidth())
    
    OutlinedTextField(
        value = associatedData, 
        onValueChange = { associatedData = it }, 
        label = { Text("Associated Data (Optional)") }, 
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    )

    ConfigDropdown(
        label = "Key Template",
        options = templates,
        selectedOption = template,
        onOptionSelected = { template = it },
        helpTitle = "Tink Templates",
        helpContent = "Tink groups all low-level settings into audited templates.\n\n• GCM: Symmetric.\n• DHKEM-AES: Asymmetric Hybrid."
    )

    Row(modifier = Modifier.padding(vertical = 16.dp)) {
        Button(onClick = {
            focusManager.clearFocus()
            val state = viewModel.encryptStandard(inputText, useCaseName, template, associatedData)
            result = state.ciphertext
            keysetInfo = state.keysetInfo
            error = state.error
            decrypted = ""
        }, modifier = Modifier.weight(1f).padding(end = 4.dp)) { 
            Text("Encrypt") 
        }

        Button(onClick = {
            focusManager.clearFocus()
            val state = viewModel.decryptStandard(result, useCaseName, template, associatedData)
            decrypted = state.decryptedText
            error = state.error
        }, modifier = Modifier.weight(1f).padding(start = 4.dp), enabled = result.isNotEmpty()) { 
            Text("Decrypt") 
        }
    }

    if (keysetInfo.isNotEmpty()) {
        Text("Status:", style = MaterialTheme.typography.titleSmall)
        Text(keysetInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }

    if (result.isNotEmpty()) {
        Text("Ciphertext:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
        OutlinedTextField(value = result, onValueChange = { result = it }, modifier = Modifier.fillMaxWidth())
    }
    if (decrypted.isNotEmpty()) {
        Text("Decrypted Text:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
        OutlinedTextField(value = decrypted, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth())
    }
    if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
}

@Composable
fun IsolatedHybridContent(viewModel: TinkViewModel, focusManager: androidx.compose.ui.focus.FocusManager) {
    var inputText by remember { mutableStateOf("") }
    var contextInfo by remember { mutableStateOf("") }
    var selectedAlgorithm by remember { mutableStateOf("ECIES") }
    var receivedKeyJson by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var decrypted by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val algorithms = listOf("ECIES", "DHKEM")

    Text("Isolated Workflow: Server generates key -> Client imports -> Client uses vault key.", style = MaterialTheme.typography.bodyMedium)

    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            ConfigDropdown(
                label = "Algorithm",
                options = algorithms,
                selectedOption = selectedAlgorithm,
                onOptionSelected = { selectedAlgorithm = it },
                helpTitle = "Hybrid Algorithms",
                helpContent = "ECIES uses Curves."
            )
        }
        Button(onClick = {
            val state = viewModel.simulateServerKeyGeneration(selectedAlgorithm)
            receivedKeyJson = state.serverPublicKey
            statusMessage = state.keysetInfo
            error = state.error
        }, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Text("Simulate Server")
        }
    }

    if (receivedKeyJson.isNotEmpty()) {
        Text("Public Key JSON:", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = receivedKeyJson, onValueChange = { receivedKeyJson = it }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
    }

    Button(onClick = {
        val state = viewModel.persistServerKeyLocally(receivedKeyJson, selectedAlgorithm)
        statusMessage = state.keysetInfo
        error = state.error
    }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), enabled = receivedKeyJson.isNotEmpty()) {
        Text("2. Client: Import to Vault")
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

    OutlinedTextField(value = inputText, onValueChange = { inputText = it }, label = { Text("Payload") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = contextInfo, onValueChange = { contextInfo = it }, label = { Text("Context Info (Binding)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

    Row(modifier = Modifier.padding(vertical = 16.dp)) {
        Button(onClick = {
            focusManager.clearFocus()
            val state = viewModel.encryptHybrid(inputText, contextInfo, selectedAlgorithm)
            result = state.ciphertext
            statusMessage = state.keysetInfo
            error = state.error
            decrypted = ""
        }, modifier = Modifier.weight(1f).padding(end = 4.dp)) { Text("3. Encrypt") }

        Button(onClick = {
            focusManager.clearFocus()
            val state = viewModel.decryptHybrid(result, contextInfo, selectedAlgorithm)
            decrypted = state.decryptedText
            error = state.error
        }, modifier = Modifier.weight(1f).padding(start = 4.dp), enabled = result.isNotEmpty()) { Text("4. Server Decrypt") }
    }

    if (statusMessage.isNotEmpty()) {
        Text("Status:", style = MaterialTheme.typography.titleSmall)
        Text(statusMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }

    if (result.isNotEmpty()) {
        Text("Result Ciphertext:", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = result, onValueChange = { result = it }, modifier = Modifier.fillMaxWidth())
    }
    if (decrypted.isNotEmpty()) {
        Text("Final Decrypted Text:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
        OutlinedTextField(value = decrypted, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth())
    }
    if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
}
