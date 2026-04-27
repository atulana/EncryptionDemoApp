package com.aanand.EncryptionDemoApp.services

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.HybridDecrypt
import com.google.crypto.tink.HybridEncrypt
import com.google.crypto.tink.JsonKeysetReader
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.TinkJsonProtoKeysetFormat
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.hybrid.HybridConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.integration.android.SharedPrefKeysetWriter
import java.util.Base64

data class TinkEncryptionResult(
    val ciphertext: String,
    val keysetInfo: String,
    val publicKey: String = ""
)

class TinkService(private val context: Context) {

    private val MASTER_KEY_URI = "android-keystore://tink_global_master_key"
    private val managerCache = mutableMapOf<String, AndroidKeysetManager>()

    init {
        AeadConfig.register()
        HybridConfig.register()
    }

    private fun getManager(
        cacheKey: String,
        keysetName: String,
        prefFile: String,
        templateName: String? = null,
        useMasterKey: Boolean = true
    ): AndroidKeysetManager {
        return managerCache.getOrPut(cacheKey) {
            val builder = AndroidKeysetManager.Builder()
                .withSharedPref(context, keysetName, prefFile)
            if (templateName != null) builder.withKeyTemplate(KeyTemplates.get(templateName))
            if (useMasterKey) builder.withMasterKeyUri(MASTER_KEY_URI)
            builder.build()
        }
    }

    private fun isAsymmetric(template: String) = template.startsWith("DH") || template.startsWith("ECIES") || template.startsWith("DHKEM")

    fun encryptStandard(input: String, useCase: String, templateName: String, aad: String): TinkEncryptionResult {
        val algoPrefix = if (isAsymmetric(templateName)) "ASYM" else "SYM"
        val cacheKey = "STD_${algoPrefix}_${useCase}_$templateName"
        val storageName = "tink_keyset_${algoPrefix}_${useCase}"
        val prefFile = "tink_prefs_${algoPrefix}_${useCase}"

        val manager = getManager(cacheKey, storageName, prefFile, templateName)
        
        return if (isAsymmetric(templateName)) {
            val hybridEncrypt = manager.keysetHandle.publicKeysetHandle.getPrimitive(RegistryConfiguration.get(), HybridEncrypt::class.java)
            val encrypted = hybridEncrypt.encrypt(input.toByteArray(), aad.toByteArray())
            TinkEncryptionResult(Base64.getEncoder().encodeToString(encrypted), "Asymmetric Active")
        } else {
            val aead = manager.keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
            val encrypted = aead.encrypt(input.toByteArray(), aad.toByteArray())
            TinkEncryptionResult(Base64.getEncoder().encodeToString(encrypted), "Symmetric Active")
        }
    }

    fun decryptStandard(ciphertext: String, useCase: String, templateName: String, aad: String): String {
        val algoPrefix = if (isAsymmetric(templateName)) "ASYM" else "SYM"
        val cacheKey = "STD_${algoPrefix}_${useCase}_$templateName"
        val storageName = "tink_keyset_${algoPrefix}_${useCase}"
        val prefFile = "tink_prefs_${algoPrefix}_${useCase}"

        val manager = getManager(cacheKey, storageName, prefFile)
        
        return if (isAsymmetric(templateName)) {
            val hybridDecrypt = manager.keysetHandle.getPrimitive(RegistryConfiguration.get(), HybridDecrypt::class.java)
            String(hybridDecrypt.decrypt(Base64.getDecoder().decode(ciphertext), aad.toByteArray()))
        } else {
            val aead = manager.keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
            String(aead.decrypt(Base64.getDecoder().decode(ciphertext), aad.toByteArray()))
        }
    }

    // Isolated Hybrid Simulation Methods
    fun simulateServerKeyGeneration(algorithm: String): TinkEncryptionResult {
        val template = if (algorithm == "DHKEM") "DHKEM_X25519_HKDF_SHA256_HKDF_SHA256_AES_256_GCM" else "ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM"
        val cacheKey = "SERVER_VAULT_$algorithm"
        val storageName = "server_keyset_$algorithm"
        val prefFile = "server_prefs_$algorithm"

        val manager = getManager(cacheKey, storageName, prefFile, template)
        val publicJson = TinkJsonProtoKeysetFormat.serializeKeysetWithoutSecret(manager.keysetHandle.publicKeysetHandle)
        return TinkEncryptionResult("", "Server Key Pair Ready ($algorithm)", publicJson)
    }



    fun persistServerKeyLocally(publicKeyJson: String, algorithm: String) {
        val keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withString(publicKeyJson))
        val storageName = "client_server_key_$algorithm"
        val prefFile = "client_server_prefs_$algorithm"
        
        // FIX: Use CleartextKeysetHandle.write for public keys (non-encrypted storage)
        // Calling keysetHandle.write(writer, null) was triggering Aead.encrypt on a null object.
        CleartextKeysetHandle.write(
            keysetHandle,
            SharedPrefKeysetWriter(context, storageName, prefFile)
        )
        
        managerCache.remove("CLIENT_VAULT_$algorithm")
    }

    // Hybrid Encryption only provides privacy, not authenticity.
    // It is only secure if the recipient can accept anonymous messages or rely on other mechanisms to authenticate the sender.
    // If authentication is required,
    // The sender should sign the message with their private key and the recipient should verify it with the sender's public key.
    // This can be done using Signing constructs of Tink
    // Additional for a 2 way signature check this can be done both from client and servers side

    fun encryptHybrid(input: String, contextInfo: String, algorithm: String): String {
        val storageName = "client_server_key_$algorithm"
        val prefFile = "client_server_prefs_$algorithm"
        
        val manager = getManager("CLIENT_VAULT_$algorithm", storageName, prefFile, useMasterKey = false)
        val hybridEncrypt = manager.keysetHandle.getPrimitive(RegistryConfiguration.get(), HybridEncrypt::class.java)
        return Base64.getEncoder().encodeToString(hybridEncrypt.encrypt(input.toByteArray(), contextInfo.toByteArray()))
    }

    fun decryptHybrid(ciphertext: String, contextInfo: String, algorithm: String): String {
        val storageName = "server_keyset_$algorithm"
        val prefFile = "server_prefs_$algorithm"
        
        val manager = getManager("SERVER_VAULT_$algorithm", storageName, prefFile)
        val hybridDecrypt = manager.keysetHandle.getPrimitive(RegistryConfiguration.get(), HybridDecrypt::class.java)
        return String(hybridDecrypt.decrypt(Base64.getDecoder().decode(ciphertext), contextInfo.toByteArray()))
    }
}
