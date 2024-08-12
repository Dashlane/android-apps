package com.dashlane.storage.securestorage

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Discouraged
import androidx.core.content.edit
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.user.Username
import com.dashlane.util.MD5Hash
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface SecureDataStorage {
    val type: Type

    @Discouraged("Use the suspend version instead")
    fun existsLegacy(identifier: String): Boolean
    suspend fun exists(identifier: String): Boolean

    @Discouraged("Use the suspend version instead")
    fun readLegacy(identifier: String): EncryptedBase64String?
    suspend fun read(identifier: String): EncryptedBase64String?

    @Discouraged("Use the suspend version instead")
    fun writeLegacy(identifier: String, data: EncryptedBase64String)
    suspend fun write(identifier: String, data: EncryptedBase64String)

    @Discouraged("Use the suspend version instead")
    fun removeLegacy(identifier: String)
    suspend fun remove(identifier: String)

    enum class Type(val identifier: String) {
        LOCAL_KEY_PROTECTED("lk"),
        MASTER_PASSWORD_PROTECTED("mp"),
        RECOVERY_KEY_PROTECTED("rk"),
        ANDROID_KEYSTORE_PROTECTED("aks")
    }

    class Factory @Inject constructor(
        @ApplicationContext private val context: Context,
        @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) {
        fun create(
            username: Username,
            type: Type
        ): SecureDataStorage {
            val storeName = MD5Hash.hash(username.email) + "." + type.identifier
            return SecureDataStorageImpl(
                context = context,
                storeName = storeName,
                type = type,
                ioDispatcher = ioDispatcher
            )
        }
    }
}

class SecureDataStorageImpl(
    context: Context,
    storeName: String,
    override val type: SecureDataStorage.Type,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SecureDataStorage {

    @Suppress("kotlin:S6291")
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(storeName, Context.MODE_PRIVATE)
    }

    override fun existsLegacy(identifier: String): Boolean =
        runBlocking { exists(identifier) }

    override suspend fun exists(identifier: String): Boolean =
        withContext(ioDispatcher) {
            !sharedPreferences.getString(identifier, null).isNullOrEmpty()
        }

    override fun readLegacy(identifier: String): EncryptedBase64String? =
        runBlocking { read(identifier) }

    override suspend fun read(identifier: String): EncryptedBase64String? =
        withContext(ioDispatcher) {
            sharedPreferences.getString(identifier, null)?.asEncryptedBase64()
        }

    override fun writeLegacy(identifier: String, data: EncryptedBase64String) {
        runBlocking { write(identifier, data) }
    }

    override suspend fun write(identifier: String, data: EncryptedBase64String) =
        withContext(ioDispatcher) {
            sharedPreferences.edit {
                putString(identifier, data.value)
            }
        }

    override fun removeLegacy(identifier: String) {
        runBlocking { remove(identifier) }
    }

    override suspend fun remove(identifier: String) =
        withContext(ioDispatcher) {
            sharedPreferences.edit {
                remove(identifier)
            }
        }
}
