package com.aanand.EncryptionDemoApp.ui.screens

import android.os.Build
import android.security.keystore.KeyProperties
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.aanand.EncryptionDemoApp.viewmodels.AndroidKeystoreViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidKeystoreScreen(
    onBack: () -> Unit,
    viewModel: AndroidKeystoreViewModel = koinViewModel()
) {
    var inputText by remember { mutableStateOf("") }
    var algorithm by remember { mutableStateOf(KeyProperties.KEY_ALGORITHM_AES) }
    var blockMode by remember { mutableStateOf(KeyProperties.BLOCK_MODE_GCM) }
    var padding by remember { mutableStateOf(KeyProperties.ENCRYPTION_PADDING_NONE) }
    var useStrongBox by remember { mutableStateOf(false) }

    var ciphertext by remember { mutableStateOf("") }
    var decryptedText by remember { mutableStateOf("") }
    var isHardwareBacked by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    val algorithms = listOf(
        KeyProperties.KEY_ALGORITHM_AES,
        KeyProperties.KEY_ALGORITHM_RSA,
        KeyProperties.KEY_ALGORITHM_3DES
    )

    val blockModes = remember(algorithm) {
        when (algorithm) {
            KeyProperties.KEY_ALGORITHM_RSA -> listOf(KeyProperties.BLOCK_MODE_ECB)
            KeyProperties.KEY_ALGORITHM_AES -> listOf(
                KeyProperties.BLOCK_MODE_GCM,
                KeyProperties.BLOCK_MODE_CBC,
                KeyProperties.BLOCK_MODE_CTR,
                KeyProperties.BLOCK_MODE_ECB
            )
            else -> listOf(KeyProperties.BLOCK_MODE_CBC, KeyProperties.BLOCK_MODE_ECB)
        }
    }

    val paddings = remember(algorithm, blockMode) {
        when (algorithm) {
            KeyProperties.KEY_ALGORITHM_RSA -> listOf(
                KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1,
                KeyProperties.ENCRYPTION_PADDING_RSA_OAEP
            )
            KeyProperties.KEY_ALGORITHM_AES -> {
                if (blockMode == KeyProperties.BLOCK_MODE_GCM) listOf(KeyProperties.ENCRYPTION_PADDING_NONE)
                else listOf(KeyProperties.ENCRYPTION_PADDING_PKCS7, KeyProperties.ENCRYPTION_PADDING_NONE)
            }
            else -> listOf(KeyProperties.ENCRYPTION_PADDING_PKCS7, KeyProperties.ENCRYPTION_PADDING_NONE)
        }
    }

    LaunchedEffect(algorithm) {
        if (!blockModes.contains(blockMode)) blockMode = blockModes.first()
    }

    LaunchedEffect(blockMode, algorithm) {
        if (!paddings.contains(padding)) padding = paddings.first()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Android Keystore") },
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
                label = { Text("Input Text (Sensitive)") },
                modifier = Modifier.fillMaxWidth()
            )

            ConfigDropdown(
                label = "Algorithm",
                options = algorithms,
                selectedOption = algorithm,
                onOptionSelected = { algorithm = it },
                helpTitle = "Keystore Algorithm",
                helpContent = "AES and RSA are hardware-backed on most modern devices. 3DES is legacy."
            )

            ConfigDropdown(
                label = "Block Mode",
                options = blockModes,
                selectedOption = blockMode,
                onOptionSelected = { blockMode = it },
                helpTitle = "Keystore Block Mode",
                helpContent = "AES/GCM is highly recommended. RSA always uses ECB in Keystore."
            )

            ConfigDropdown(
                label = "Padding",
                options = paddings,
                selectedOption = padding,
                onOptionSelected = { padding = it },
                helpTitle = "Keystore Padding",
                helpContent = "RSA requires PKCS1 or OAEP. AES/GCM requires None."
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = useStrongBox,
                        onCheckedChange = { useStrongBox = it }
                    )
                    Text("Use StrongBox (if available)", modifier = Modifier.padding(start = 8.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val state = viewModel.encrypt(inputText, algorithm, blockMode, padding, useStrongBox)
                        ciphertext = state.ciphertext
                        isHardwareBacked = state.isHardwareBacked
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
                        val state = viewModel.decrypt(ciphertext, algorithm, blockMode, padding)
                        decryptedText = state.decryptedText
                        errorText = state.error
                    },
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    enabled = ciphertext.isNotEmpty()
                ) {
                    Text("Decrypt")
                }
            }

            if (ciphertext.isNotEmpty()) {
                Text(
                    text = "Hardware Backed: ${if (isHardwareBacked) "YES" else "NO"}",
                    color = if (isHardwareBacked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text("Ciphertext (Stored in Keystore):", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = ciphertext,
                    onValueChange = { ciphertext = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }

            if (decryptedText.isNotEmpty()) {
                Text("Decrypted Output:", style = MaterialTheme.typography.titleMedium)
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
