package com.aanand.EncryptionDemoApp.services

import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

data class JcaEncryptionResult(
    val ciphertext: String,
    val key: String,
    val privateKey: String = ""
)

class JcaService {

    private fun getTransformation(algo: String, mode: String, padding: String): String {
        return if (algo == "RSA" && mode == "NONE") {
            "$algo"
        } else {
            "$algo/$mode/$padding"
        }
    }

    fun encrypt(
        input: String,
        algorithm: String,
        blockMode: String,
        padding: String,
        keySize: Int
    ): JcaEncryptionResult {
        return if (algorithm == "RSA") {
            encryptAsymmetric(input, algorithm, blockMode, padding, keySize)
        } else {
            encryptSymmetric(input, algorithm, blockMode, padding, keySize)
        }
    }

    private fun encryptSymmetric(
        input: String,
        algorithm: String,
        blockMode: String,
        padding: String,
        keySize: Int
    ): JcaEncryptionResult {
        val keyGen = KeyGenerator.getInstance(algorithm)
        if (algorithm == "AES") keyGen.init(keySize)
        val secretKey = keyGen.generateKey()
        val keyBase64 = Base64.getEncoder().encodeToString(secretKey.encoded)

        val transformation = getTransformation(algorithm, blockMode, padding)
        val cipher = Cipher.getInstance(transformation)

        val ivSize = if (algorithm == "AES") 16 else 8
        val iv = if (blockMode != "ECB") {
            val randomIv = ByteArray(ivSize)
            SecureRandom().nextBytes(randomIv)
            randomIv
        } else null

        if (iv != null) {
            if (blockMode == "GCM") {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
            }
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        }

        val ciphertext = cipher.doFinal(input.toByteArray())
        val finalOutput = if (iv != null) iv + ciphertext else ciphertext

        return JcaEncryptionResult(
            ciphertext = Base64.getEncoder().encodeToString(finalOutput),
            key = keyBase64
        )
    }

    private fun encryptAsymmetric(
        input: String,
        algorithm: String,
        blockMode: String,
        padding: String,
        keySize: Int
    ): JcaEncryptionResult {
        val kpg = KeyPairGenerator.getInstance(algorithm)
        kpg.initialize(keySize)
        val keyPair = kpg.generateKeyPair()
        
        val publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.public.encoded)
        val privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.private.encoded)

        val transformation = getTransformation(algorithm, blockMode, padding)
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.public)

        val ciphertext = cipher.doFinal(input.toByteArray())

        return JcaEncryptionResult(
            ciphertext = Base64.getEncoder().encodeToString(ciphertext),
            key = publicKeyBase64,
            privateKey = privateKeyBase64
        )
    }

    fun decrypt(
        ciphertextBase64: String,
        keyBase64: String,
        algorithm: String,
        blockMode: String,
        padding: String
    ): String {
        return if (algorithm == "RSA") {
            decryptAsymmetric(ciphertextBase64, keyBase64, algorithm, blockMode, padding)
        } else {
            decryptSymmetric(ciphertextBase64, keyBase64, algorithm, blockMode, padding)
        }
    }

    private fun decryptSymmetric(
        ciphertextBase64: String,
        keyBase64: String,
        algorithm: String,
        blockMode: String,
        padding: String
    ): String {
        val keyBytes = Base64.getDecoder().decode(keyBase64)
        val secretKey = SecretKeySpec(keyBytes, algorithm)
        val combinedOutput = Base64.getDecoder().decode(ciphertextBase64)

        val transformation = getTransformation(algorithm, blockMode, padding)
        val cipher = Cipher.getInstance(transformation)

        val ivSize = if (algorithm == "AES") 16 else 8

        if (blockMode != "ECB") {
            val iv = combinedOutput.copyOfRange(0, ivSize)
            val ciphertext = combinedOutput.copyOfRange(ivSize, combinedOutput.size)
            
            if (blockMode == "GCM") {
                cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            }
            
            return String(cipher.doFinal(ciphertext))
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            return String(cipher.doFinal(combinedOutput))
        }
    }

    private fun decryptAsymmetric(
        ciphertextBase64: String,
        privateKeyBase64: String,
        algorithm: String,
        blockMode: String,
        padding: String
    ): String {
        val privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64)
        val keyFactory = KeyFactory.getInstance(algorithm)
        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))

        val ciphertext = Base64.getDecoder().decode(ciphertextBase64)
        val transformation = getTransformation(algorithm, blockMode, padding)
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        return String(cipher.doFinal(ciphertext))
    }
}
