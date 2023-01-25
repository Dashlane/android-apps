package com.dashlane.util.hardwaresecurity

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.annotation.CheckResult
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.util.stackTraceToSafeString
import java.security.InvalidKeyException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

private const val TAG_LENGTH = 128
private const val GCM_IV_LENGTH = 12
private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE



class CryptoObjectHelper @Inject constructor(
    val userSupportFileLogger: UserSupportFileLogger,
    private val userPreferencesManager: UserPreferencesManager
) {

    

    @CheckResult
    fun createEncryptionKey(keyStoreKey: KeyStoreKey, isUserAuthenticationRequired: Boolean) = try {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM, ANDROID_KEY_STORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                keyStoreKey.alias(),
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).apply {
                setBlockModes(BLOCK_MODE)
                
                setInvalidatedByBiometricEnrollment(true)
                
                setUserAuthenticationRequired(isUserAuthenticationRequired)
                setEncryptionPaddings(PADDING)
            }.build()
        )
        keyGenerator.generateKey()
    } catch (e: Throwable) {
        userSupportFileLogger.add(
            "[CryptoObjectHelper] Encryption Key can't be generated, exception is: ${e.stackTraceToSafeString()}"
        )
        null
    }

    

    fun deleteEncryptionKey(keyStoreKey: KeyStoreKey) {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        runCatching {
            keyStore.deleteEntry(keyStoreKey.alias())
        }
    }

    

    @CheckResult
    fun getEncryptCipher(keyStoreKey: KeyStoreKey): CipherInitResult = getCipher(keyStoreKey, Cipher.ENCRYPT_MODE)

    

    @CheckResult
    fun getDecryptCipher(keyStoreKey: KeyStoreKey, iv: GCMParameterSpec): CipherInitResult = getCipher(keyStoreKey, Cipher.DECRYPT_MODE, iv)

    

    @CheckResult
    fun challengeAuthentication(cipher: Cipher): CryptoChallengeResult {
        return try {
            cipher.doFinal()
            CryptoChallengeResult.Success
        } catch (e: Throwable) {
            userSupportFileLogger.add(
                "[CryptoObjectHelper] Cryptographic Key is not properly stored, exception is: ${e.stackTraceToSafeString()}"
            )
            CryptoChallengeResult.Failure(
                e,
                "Cryptographic Key is not properly stored, Biometric Authentication can't be securely used"
            )
        }
    }

    

    fun encrypt(keyStoreKey: KeyStoreKey, data: ByteArray): ByteArray? {
        return when (val cipherInitResult = getEncryptCipher(keyStoreKey)) {
            is CipherInitResult.Failure -> null
            is CipherInitResult.Success -> {
                val iv = cipherInitResult.cipher.iv
                check(iv.size == GCM_IV_LENGTH)
                val lk = cipherInitResult.cipher.doFinal(data)
                return iv + lk
            }
        }
    }

    

    fun decrypt(keyStoreKey: KeyStoreKey, data: ByteArray): ByteArray? {
        val iv = data.sliceArray(0 until GCM_IV_LENGTH)
        val encrypted = data.sliceArray(GCM_IV_LENGTH until data.size)
        return when (val cipherInitResult = getDecryptCipher(keyStoreKey, GCMParameterSpec(TAG_LENGTH, iv))) {
            is CipherInitResult.Failure -> null
            is CipherInitResult.Success -> {
                cipherInitResult.cipher.doFinal(encrypted)
            }
        }
    }

    private fun getCipher(keyStoreKey: KeyStoreKey, opMode: Int, iv: GCMParameterSpec? = null): CipherInitResult {
        
        if (keyStoreKey is BiometricsSeal && !userPreferencesManager.biometricSealPaddingMigrationAttempt) migrateEncryptionKey(keyStoreKey)

        val cipher = try {
            Cipher.getInstance("$ALGORITHM/$BLOCK_MODE/$PADDING")
        } catch (t: Throwable) {
            return CipherInitResult.Failure(t, "Error while getting a Cipher instance")
        }

        return try {
            val key = getEncryptionKey(keyStoreKey)
            when (opMode) {
                Cipher.ENCRYPT_MODE -> cipher.init(opMode, key)
                Cipher.DECRYPT_MODE -> cipher.init(opMode, key, iv)
                else -> CipherInitResult.Failure(errorMessage = "Invalid opMode")
            }
            CipherInitResult.Success(cipher)
        } catch (e: KeyPermanentlyInvalidatedException) {
            CipherInitResult.InvalidatedKeyError(e, "Encryption key has been invalidated")
        } catch (e: InvalidKeyException) {
            CipherInitResult.Failure(e, "Error while initializing the Cipher")
        } catch (t: Throwable) {
            CipherInitResult.Failure(t, "Error while initializing the Cipher")
        }
    }

    private fun getEncryptionKey(keyStoreKey: KeyStoreKey): SecretKey? {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        return keyStore.getKey(keyStoreKey.alias(), null) as? SecretKey?
    }

    private fun migrateEncryptionKey(keyStoreKey: KeyStoreKey) {
        
        
        
        deleteEncryptionKey(keyStoreKey)
        createEncryptionKey(keyStoreKey, true)
        userPreferencesManager.biometricSealPaddingMigrationAttempt = true
    }

    sealed class CipherInitResult {
        

        class Success(val cipher: Cipher) : CipherInitResult()

        

        open class Failure(val throwable: Throwable? = null, val errorMessage: String? = null) : CipherInitResult()

        

        class InvalidatedKeyError(throwable: Throwable? = null, errorMessage: String? = null) :
            Failure(throwable, errorMessage)
    }

    sealed class CryptoChallengeResult {
        

        object Success : CryptoChallengeResult()

        

        class Failure(val throwable: Throwable? = null, val errorMessage: String) : CryptoChallengeResult()
    }

    private companion object {
        const val ENCRYPTION_KEY = "encryption_key_"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
    }

    interface KeyStoreKey {
        fun alias(): String
    }

    @JvmInline
    value class BiometricsSeal(val username: String) : KeyStoreKey {
        override fun alias(): String = ENCRYPTION_KEY + username
    }

    @JvmInline
    value class LocalKeyLock(val username: String) : KeyStoreKey {
        private val lockPrefix: String
            get() = "LOCK_"

        override fun alias(): String = ENCRYPTION_KEY + lockPrefix + username
    }
}
