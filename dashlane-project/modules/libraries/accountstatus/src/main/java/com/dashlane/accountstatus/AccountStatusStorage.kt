package com.dashlane.accountstatus

import android.util.Log
import com.dashlane.crypto.keys.LocalKey
import com.dashlane.session.Session
import com.dashlane.user.Username
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.util.anonymize
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import javax.inject.Inject

interface AccountStatusStorage {
    fun saveAccountStatus(localKey: LocalKey, username: Username, newStatus: AccountStatus): Boolean

    fun readAccountStatus(localKey: LocalKey, username: Username): AccountStatus?
}

class AccountStatusStorageImpl @Inject constructor(
    private val userSecureDataStorageManager: UserSecureStorageManager,
    private val gson: Gson
) : AccountStatusStorage {
    override fun saveAccountStatus(localKey: LocalKey, username: Username, newStatus: AccountStatus): Boolean {
        val jsonStatus = gson.toJson(newStatus)
        if (jsonStatus.isNullOrBlank()) return false

        userSecureDataStorageManager.storeAccountStatus(localKey, username, jsonStatus)

        return newStatus == readAccountStatus(localKey, username)
    }

    override fun readAccountStatus(localKey: LocalKey, username: Username,): AccountStatus? =
        try {
            val storedStatus = userSecureDataStorageManager.readAccountStatus(localKey, username) ?: throw NullPointerException()
            val result = gson.fromJson(
                storedStatus,
                AccountStatus::class.java
            )

            @Suppress("SENSELESS_COMPARISON")
            
            
            if (result.premiumStatus == null || result.subscriptionInfo == null) {
                throw JsonSyntaxException("AccountStatus fields have been deserialized as null")
            }
            result
        } catch (e: NullPointerException) {
            logException(e)
            null
        } catch (e: JsonSyntaxException) {
            null
        } catch (e: Throwable) {
            logException(e)
            throw (e)
        }

    private fun logException(e: Throwable) {
        Log.d("ACCOUNT_STATUS", "Cannot read local AccountStatus ${e.anonymize()}")
    }
}