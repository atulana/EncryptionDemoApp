package com.aanand.EncryptionDemoApp.viewmodels

import androidx.lifecycle.ViewModel
import com.aanand.EncryptionDemoApp.services.JcaService

data class EncryptionState(
    val ciphertext: String = "",
    val generatedKey: String = "", // Used for Symmetric or Public Key
    val privateKey: String = "", // Used for Asymmetric
    val decryptedText: String = "",
    val error: String = ""
)

class KeyGeneratorViewModel(private val jcaService: JcaService) : ViewModel() {

    fun encrypt(
        input: String,
        algorithm: String,
        blockMode: String,
        padding: String,
        keySize: Int
    ): EncryptionState {
        if (input.isEmpty()) return EncryptionState(error = "Input cannot be empty")

        return try {
            val result = jcaService.encrypt(input, algorithm, blockMode, padding, keySize)
            EncryptionState(
                ciphertext = result.ciphertext,
                generatedKey = result.key,
                privateKey = result.privateKey
            )
        } catch (e: Exception) {
            EncryptionState(error = "Encryption Error: ${e.localizedMessage}")
        }
    }

    fun decrypt(
        ciphertextBase64: String,
        keyBase64: String, // This is the SecretKey for Symmetric or PrivateKey for Asymmetric
        algorithm: String,
        blockMode: String,
        padding: String
    ): EncryptionState {
        if (ciphertextBase64.isEmpty() || keyBase64.isEmpty()) {
            return EncryptionState(error = "Ciphertext and Key are required for decryption")
        }

        return try {
            val decrypted = jcaService.decrypt(ciphertextBase64, keyBase64, algorithm, blockMode, padding)
            EncryptionState(
                ciphertext = ciphertextBase64,
                generatedKey = keyBase64,
                decryptedText = decrypted
            )
        } catch (e: Exception) {
            EncryptionState(
                ciphertext = ciphertextBase64,
                generatedKey = keyBase64,
                error = "Decryption Error: ${e.localizedMessage}"
            )
        }
    }
}
