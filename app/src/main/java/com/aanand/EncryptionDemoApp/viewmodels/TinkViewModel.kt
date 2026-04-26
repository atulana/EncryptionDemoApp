package com.aanand.EncryptionDemoApp.viewmodels

import androidx.lifecycle.ViewModel
import com.aanand.EncryptionDemoApp.services.TinkService

data class TinkState(
    val ciphertext: String = "",
    val decryptedText: String = "",
    val keysetInfo: String = "",
    val serverPublicKey: String = "",
    val error: String = ""
)

class TinkViewModel(private val tinkService: TinkService) : ViewModel() {

    /**
     * STANDARD ENCRYPTION (Local Vault)
     */
    fun encryptStandard(input: String, useCase: String, templateName: String, aad: String): TinkState {
        if (input.isEmpty()) return TinkState(error = "Input is empty")
        return try {
            val result = tinkService.encryptStandard(input, useCase, templateName, aad)
            TinkState(
                ciphertext = result.ciphertext,
                keysetInfo = result.keysetInfo
            )
        } catch (e: Exception) {
            TinkState(error = "Tink Standard Error: ${e.localizedMessage}")
        }
    }

    fun decryptStandard(ciphertext: String, useCase: String, templateName: String, aad: String): TinkState {
        if (ciphertext.isEmpty()) return TinkState(error = "No ciphertext")
        return try {
            val decrypted = tinkService.decryptStandard(ciphertext, useCase, templateName, aad)
            TinkState(decryptedText = decrypted, ciphertext = ciphertext)
        } catch (e: Exception) {
            TinkState(error = "Tink Standard Decrypt Error: ${e.localizedMessage}")
        }
    }

    /**
     * ISOLATED HYBRID FLOW (Simulated Server Interaction)
     */
    fun simulateServerKeyGeneration(algorithm: String): TinkState {
        return try {
            val result = tinkService.simulateServerKeyGeneration(algorithm)
            TinkState(
                serverPublicKey = result.publicKey,
                keysetInfo = result.keysetInfo
            )
        } catch (e: Exception) {
            TinkState(error = "Hybrid Setup Error: ${e.localizedMessage}")
        }
    }

    fun persistServerKeyLocally(publicKeyJson: String, algorithm: String): TinkState {
        if (publicKeyJson.isEmpty()) return TinkState(error = "Empty JSON")
        return try {
            tinkService.persistServerKeyLocally(publicKeyJson, algorithm)
            TinkState(serverPublicKey = publicKeyJson, keysetInfo = "Client: $algorithm key imported to vault.")
        } catch (e: Exception) {
            TinkState(error = "Import Error: ${e.localizedMessage}")
        }
    }

    fun encryptHybrid(input: String, contextInfo: String, algorithm: String): TinkState {
        if (input.isEmpty()) return TinkState(error = "Input is empty")
        return try {
            val encrypted = tinkService.encryptHybrid(input, contextInfo, algorithm)
            TinkState(ciphertext = encrypted, keysetInfo = "Client: Encrypted via $algorithm.")
        } catch (e: Exception) {
            TinkState(error = "Hybrid Encrypt Error: ${e.localizedMessage}")
        }
    }

    fun decryptHybrid(ciphertext: String, contextInfo: String, algorithm: String): TinkState {
        if (ciphertext.isEmpty()) return TinkState(error = "No ciphertext")
        return try {
            val decrypted = tinkService.decryptHybrid(ciphertext, contextInfo, algorithm)
            TinkState(decryptedText = decrypted, ciphertext = ciphertext)
        } catch (e: Exception) {
            TinkState(error = "Hybrid Decrypt Error: ${e.localizedMessage}")
        }
    }
}
