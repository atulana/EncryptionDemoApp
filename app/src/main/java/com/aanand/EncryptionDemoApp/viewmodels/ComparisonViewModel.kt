package com.aanand.EncryptionDemoApp.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.aanand.EncryptionDemoApp.services.JcaService
import com.aanand.EncryptionDemoApp.services.KeystoreService
import com.aanand.EncryptionDemoApp.services.TinkService
import kotlin.system.measureNanoTime

data class ComparisonResult(
    val method: String,
    val algorithm: String,
    val inputSize: Int,
    val encryptTimeMs: Double,
    val decryptTimeMs: Double,
    val isHardware: Boolean = false,
    val error: String = ""
)

data class ComparisonState(
    val results: List<ComparisonResult> = emptyList(),
    val isRunning: Boolean = false
)

class ComparisonViewModel(
    private val jcaService: JcaService,
    private val keystoreService: KeystoreService,
    private val tinkService: TinkService
) : ViewModel() {

    private val _state = mutableStateOf(ComparisonState())
    val state: State<ComparisonState> = _state

    fun runBenchmark(
        algorithm: String,
        blockMode: String,
        padding: String,
        inputSize: Int
    ) {
        _state.value = _state.value.copy(isRunning = true)

        val input = generateRandomString(inputSize)
        val newResults = mutableListOf<ComparisonResult>()

        // 1. Manual JCA
        newResults.add(benchmarkJca(input, algorithm, blockMode, padding))

        // 2. Android Keystore
        newResults.add(benchmarkKeystore(input, algorithm, blockMode, padding))

        // 3. Google Tink
        newResults.add(benchmarkTink(input, algorithm, blockMode))

        _state.value = _state.value.copy(
            results = _state.value.results + newResults,
            isRunning = false
        )
    }

    private fun benchmarkJca(input: String, algo: String, mode: String, padding: String): ComparisonResult {
        return try {
            val keySize = if (algo == "RSA") 2048 else 256
            var ciphertext = ""
            var key = ""
            
            val encTime = measureNanoTime {
                val res = jcaService.encrypt(input, algo, mode, padding, keySize)
                ciphertext = res.ciphertext
                key = res.key
            }
            
            val decTime = measureNanoTime {
                jcaService.decrypt(ciphertext, key, algo, mode, padding)
            }
            
            ComparisonResult("Manual JCA", algo, input.length, encTime / 1_000_000.0, decTime / 1_000_000.0, false)
        } catch (e: Exception) {
            ComparisonResult("Manual JCA", algo, input.length, 0.0, 0.0, false, e.localizedMessage ?: "Error")
        }
    }

    private fun benchmarkKeystore(input: String, algo: String, mode: String, padding: String): ComparisonResult {
        return try {
            var ciphertext = ""
            var isHW = false
            
            val encTime = measureNanoTime {
                val res = keystoreService.encrypt(input, algo, mode, padding, false)
                ciphertext = res.ciphertext
                isHW = res.isHardwareBacked
            }
            
            val decTime = measureNanoTime {
                keystoreService.decrypt(ciphertext, algo, mode, padding)
            }
            
            ComparisonResult("Keystore", algo, input.length, encTime / 1_000_000.0, decTime / 1_000_000.0, isHW)
        } catch (e: Exception) {
            ComparisonResult("Keystore", algo, input.length, 0.0, 0.0, false, e.localizedMessage ?: "Error")
        }
    }

    private fun benchmarkTink(input: String, algo: String, mode: String): ComparisonResult {
        return try {
            val template = when {
                algo == "RSA" -> "RSA_OAEP_3072_SHA256_AES128_GCM"
                algo == "AES" && mode == "GCM" -> "AES256_GCM"
                algo == "AES" && mode == "CTR" -> "AES256_CTR_HMAC_SHA256"
                else -> return ComparisonResult("Tink", algo, input.length, 0.0, 0.0, false, "N/A for $algo/$mode")
            }

            var ciphertext = ""
            val encTime = measureNanoTime {
                val res = tinkService.encryptStandard(input, "BENCHMARK", template, "bench")
                ciphertext = res.ciphertext
            }
            
            val decTime = measureNanoTime {
                tinkService.decryptStandard(ciphertext, "BENCHMARK", template, "bench")
            }
            
            val label = if (algo == "RSA") "Tink (RSA Hybrid)" else "Google Tink"
            ComparisonResult(label, algo, input.length, encTime / 1_000_000.0, decTime / 1_000_000.0, true)
        } catch (e: Exception) {
            ComparisonResult("Google Tink", algo, input.length, 0.0, 0.0, false, e.localizedMessage ?: "Error")
        }
    }

    private fun generateRandomString(sizeBytes: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val sr = java.security.SecureRandom()
        return (1..sizeBytes).map { chars[sr.nextInt(chars.length)] }.joinToString("")
    }

    fun clearResults() {
        _state.value = ComparisonState()
    }
}
