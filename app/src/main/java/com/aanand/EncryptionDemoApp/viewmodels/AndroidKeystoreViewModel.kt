package com.aanand.EncryptionDemoApp.viewmodels

import androidx.lifecycle.ViewModel
import com.aanand.EncryptionDemoApp.services.KeystoreService

data class KeystoreState(
    val ciphertext: String = "",
    val decryptedText: String = "",
    val isHardwareBacked: Boolean = false,
    val publicKey: String = "",
    val error: String = ""
)

class AndroidKeystoreViewModel(private val keystoreService: KeystoreService) : ViewModel() {

    fun encrypt(
        input: String,
        algorithm: String,
        blockMode: String,
        padding: String,
        useStrongBox: Boolean
    ): KeystoreState {
        if (input.isEmpty()) return KeystoreState(error = "Input cannot be empty")

        return try {
            val result = keystoreService.encrypt(input, algorithm, blockMode, padding, useStrongBox)
            KeystoreState(
                ciphertext = result.ciphertext,
                isHardwareBacked = result.isHardwareBacked,
                publicKey = result.publicKey
            )
        } catch (e: Exception) {
            KeystoreState(error = "Encryption Error: ${e.localizedMessage}")
        }
    }

    fun decrypt(
        ciphertextBase64: String,
        algorithm: String,
        blockMode: String,
        padding: String
    ): KeystoreState {
        if (ciphertextBase64.isEmpty()) return KeystoreState(error = "No ciphertext to decrypt")

        return try {
            val decrypted = keystoreService.decrypt(ciphertextBase64, algorithm, blockMode, padding)
            KeystoreState(
                decryptedText = decrypted,
                ciphertext = ciphertextBase64
            )
        } catch (e: Exception) {
            KeystoreState(error = "Decryption Error: ${e.localizedMessage}")
        }
    }
}
