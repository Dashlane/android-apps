package com.dashlane.storage.securestorage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.session.Username
import com.dashlane.util.MD5Hash
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface SecureDataStorage {
    val type: Type

    fun exists(identifier: String): Boolean
    fun read(identifier: String): EncryptedBase64String?
    fun write(identifier: String, data: EncryptedBase64String)
    fun remove(identifier: String)

    enum class Type(val identifier: String) {
        LOCAL_KEY_PROTECTED("lk"),
        MASTER_PASSWORD_PROTECTED("mp"),
        RECOVERY_KEY_PROTECTED("rk"),
        ANDROID_KEYSTORE_PROTECTED("aks")
    }

    class Factory @Inject constructor(@ApplicationContext private val context: Context) {
        fun create(
            username: Username,
            type: Type
        ): SecureDataStorage {
            val storeName = MD5Hash.hash(username.email) + "." + type.identifier
            val sharedPreferences = context.getSharedPreferences(storeName, Context.MODE_PRIVATE)
            return SecureDataStorageImpl(sharedPreferences, type)
        }
    }
}



class SecureDataStorageImpl(
    private val sharedPreferences: SharedPreferences,
    override val type: SecureDataStorage.Type
) : SecureDataStorage {

    override fun exists(identifier: String): Boolean =
        !sharedPreferences.getString(identifier, null).isNullOrEmpty()

    override fun read(identifier: String): EncryptedBase64String? =
        sharedPreferences.getString(identifier, null)?.asEncryptedBase64()

    override fun write(identifier: String, data: EncryptedBase64String) {
        sharedPreferences.edit {
            putString(identifier, data.value)
        }
    }

    override fun remove(identifier: String) {
        sharedPreferences.edit {
            remove(identifier)
        }
    }
}
