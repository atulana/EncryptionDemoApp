package com.aanand.EncryptionDemoApp.services

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec

data class KeystoreEncryptionResult(
    val ciphertext: String,
    val isHardwareBacked: Boolean,
    val publicKey: String = ""
)

class KeystoreService {

    private val PROVIDER = "AndroidKeyStore"

    private fun getTransformation(algo: String, mode: String, padding: String): String {
        return ("$algo/$mode/$padding")
    }

    fun encrypt(
        input: String,
        algorithm: String,
        blockMode: String,
        padding: String,
        useStrongBox: Boolean
    ): KeystoreEncryptionResult {
        val alias = getAlias(algorithm)
        val key = getOrCreateKey(algorithm, blockMode, padding, useStrongBox)
        val transformation = getTransformation(algorithm, blockMode, padding)
        val cipher = Cipher.getInstance(transformation)

        val publicKeyBase64 = if (algorithm == KeyProperties.KEY_ALGORITHM_RSA) {
            val keyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }
            val publicKey = keyStore.getCertificate(alias).publicKey
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            Base64.getEncoder().encodeToString(publicKey.encoded)
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, key)
            ""
        }

        val ciphertext = cipher.doFinal(input.toByteArray())
        val iv = cipher.iv

        val combined = if (iv != null) iv + ciphertext else ciphertext
        val isHardware = checkHardwareBacked(key, algorithm)

        return KeystoreEncryptionResult(
            ciphertext = Base64.getEncoder().encodeToString(combined),
            isHardwareBacked = isHardware,
            publicKey = publicKeyBase64
        )
    }

    fun decrypt(
        ciphertextBase64: String,
        algorithm: String,
        blockMode: String,
        padding: String
    ): String {
        val keyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }
        val key = keyStore.getKey(getAlias(algorithm), null) ?: throw Exception("Key not found")

        val combined = Base64.getDecoder().decode(ciphertextBase64)
        val transformation = getTransformation(algorithm, blockMode, padding)
        val cipher = Cipher.getInstance(transformation)

        return if (algorithm == KeyProperties.KEY_ALGORITHM_RSA) {
            cipher.init(Cipher.DECRYPT_MODE, key as PrivateKey)
            String(cipher.doFinal(combined))
        } else {
            val ivSize = when {
                blockMode == KeyProperties.BLOCK_MODE_GCM -> 12
                algorithm == KeyProperties.KEY_ALGORITHM_3DES -> 8
                else -> 16
            }

            if (blockMode == KeyProperties.BLOCK_MODE_ECB) {
                cipher.init(Cipher.DECRYPT_MODE, key as SecretKey)
                String(cipher.doFinal(combined))
            } else {
                val iv = combined.copyOfRange(0, ivSize)
                val ciphertext = combined.copyOfRange(ivSize, combined.size)

                if (blockMode == KeyProperties.BLOCK_MODE_GCM) {
                    cipher.init(Cipher.DECRYPT_MODE, key as SecretKey, GCMParameterSpec(128, iv))
                } else {
                    cipher.init(Cipher.DECRYPT_MODE, key as SecretKey, IvParameterSpec(iv))
                }

                String(cipher.doFinal(ciphertext))
            }
        }
    }

    private fun getAlias(algorithm: String) = "KeystoreKey_$algorithm"

    private fun getOrCreateKey(algorithm: String, blockMode: String, padding: String, useStrongBox: Boolean): Key {
        val alias = getAlias(algorithm)

        return if (algorithm == KeyProperties.KEY_ALGORITHM_RSA) {
            val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, PROVIDER)
            val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(blockMode)
                .setEncryptionPaddings(padding)
                .setKeySize(2048)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512, KeyProperties.DIGEST_SHA1)
            if (useStrongBox) builder.setIsStrongBoxBacked(true)
            kpg.initialize(builder.build())
            kpg.generateKeyPair().private
        } else {
            val keyGenerator = KeyGenerator.getInstance(algorithm, PROVIDER)
            val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(blockMode)
                .setEncryptionPaddings(padding)
                .setRandomizedEncryptionRequired(true)
            if (algorithm == KeyProperties.KEY_ALGORITHM_AES) builder.setKeySize(256)
            if (useStrongBox) builder.setIsStrongBoxBacked(true)
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        }
    }

    private fun checkHardwareBacked(key: Key, algorithm: String): Boolean {
        return try {
            val keyInfo = if (algorithm == KeyProperties.KEY_ALGORITHM_RSA) {
                KeyFactory.getInstance(algorithm, PROVIDER).getKeySpec(key, KeyInfo::class.java)
            } else {
                SecretKeyFactory.getInstance(algorithm, PROVIDER).getKeySpec(key as SecretKey, KeyInfo::class.java)
            }
            (keyInfo as KeyInfo).isInsideSecureHardware
        } catch (e: Exception) {
            false
        }
    }
}
