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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.aanand.EncryptionDemoApp.viewmodels.KeyGeneratorViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyGeneratorScreen(
    onBack: () -> Unit,
    viewModel: KeyGeneratorViewModel = koinViewModel()
) {
    var inputText by rememberSaveable(Unit) { mutableStateOf("") }
    var algorithm by rememberSaveable(Unit) { mutableStateOf("AES") }
    var blockMode by rememberSaveable(Unit) { mutableStateOf("GCM") }
    var padding by rememberSaveable(Unit) { mutableStateOf("NoPadding") }
    var keySize by rememberSaveable(Unit) { mutableStateOf("256") }
    
    var ciphertext by rememberSaveable(Unit) { mutableStateOf("") }
    var generatedKey by rememberSaveable(Unit) { mutableStateOf("") }
    var privateKey by rememberSaveable(Unit) { mutableStateOf("") }
    var decryptedText by rememberSaveable(Unit) { mutableStateOf("") }
    var errorText by rememberSaveable(Unit) { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    val algorithms = listOf("AES", "DES", "DESede", "RSA")
    val blockModes = remember(algorithm) {
        if (algorithm == "RSA") listOf("ECB", "NONE")
        else listOf("GCM", "CBC", "CTR", "ECB", "CFB")
    }
    val paddings = remember(algorithm) {
        if (algorithm == "RSA") listOf("PKCS1Padding", "OAEPWithSHA-256AndMGF1Padding")
        else listOf("NoPadding", "PKCS7Padding", "PKCS5Padding")
    }
    val keySizes = remember(algorithm) {
        if (algorithm == "RSA") listOf("1024", "2048", "3072")
        else listOf("128", "192", "256")
    }

    // Adjust defaults when algorithm changes
    LaunchedEffect(algorithm) {
        if (algorithm == "RSA") {
            blockMode = "ECB"
            padding = "PKCS1Padding"
            keySize = "2048"
        } else {
            blockMode = "GCM"
            padding = "NoPadding"
            keySize = "256"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manual KeyGenerator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Input Text") },
                modifier = Modifier.fillMaxWidth()
            )

            ConfigDropdown(
                label = "Algorithm",
                options = algorithms,
                selectedOption = algorithm,
                onOptionSelected = { algorithm = it },
                helpTitle = "Encryption Algorithm",
                helpContent = "The algorithm defines the mathematical process for encryption.\n\n" +
                        "• AES: Advanced Encryption Standard (Symmetric). Fast and secure.\n" +
                        "• RSA: Rivest-Shamir-Adleman (Asymmetric). Uses a Public Key to encrypt and a Private Key to decrypt."
            )

            ConfigDropdown(
                label = "Block Mode",
                options = blockModes,
                selectedOption = blockMode,
                onOptionSelected = { blockMode = it },
                helpTitle = "Block Cipher Mode",
                helpContent = "Modes define how to handle data blocks. RSA typically uses ECB or NONE."
            )

            ConfigDropdown(
                label = "Padding",
                options = paddings,
                selectedOption = padding,
                onOptionSelected = { padding = it },
                helpTitle = "Padding Scheme",
                helpContent = "RSA requires specific padding like PKCS1 or OAEP for security. AES/GCM requires NoPadding."
            )

            ConfigDropdown(
                label = "Key Size (bits)",
                options = keySizes,
                selectedOption = keySize,
                onOptionSelected = { keySize = it },
                helpTitle = "Key Length",
                helpContent = "RSA keys should be 2048-bit or higher. AES keys are typically 128 or 256-bit."
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val state = viewModel.encrypt(inputText, algorithm, blockMode, padding, keySize.toInt())
                        ciphertext = state.ciphertext
                        generatedKey = state.generatedKey
                        privateKey = state.privateKey
                        errorText = state.error
                        decryptedText = ""
                    },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Text("Encrypt")
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val keyToUse = if (algorithm == "RSA") privateKey else generatedKey
                        val state = viewModel.decrypt(ciphertext, keyToUse, algorithm, blockMode, padding)
                        decryptedText = state.decryptedText
                        errorText = state.error
                    },
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    enabled = ciphertext.isNotEmpty() && (generatedKey.isNotEmpty() || privateKey.isNotEmpty())
                ) {
                    Text("Decrypt")
                }
            }

            if (generatedKey.isNotEmpty()) {
                Text(
                    text = if (algorithm == "RSA") "Public Key (Base64):" else "Generated Key (Base64):",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = generatedKey,
                    onValueChange = { generatedKey = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            if (privateKey.isNotEmpty()) {
                Text("Private Key (Base64):", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = privateKey,
                    onValueChange = { privateKey = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            if (ciphertext.isNotEmpty()) {
                Text("Result Ciphertext (Base64):", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = ciphertext,
                    onValueChange = { ciphertext = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            if (decryptedText.isNotEmpty()) {
                Text("Decrypted Text:", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = decryptedText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (errorText.isNotEmpty()) {
                Text(errorText, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    helpTitle: String,
    helpContent: String
) {
    var expanded by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(helpTitle) },
            text = { Text(helpContent) },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) {
                    Text("Got it")
                }
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = { onOptionSelected(it) },
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
        IconButton(onClick = { showHelp = true }) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Help for $label"
            )
        }
    }
}
