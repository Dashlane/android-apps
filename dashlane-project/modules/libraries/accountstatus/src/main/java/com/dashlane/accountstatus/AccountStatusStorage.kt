package com.dashlane.accountstatus

import android.util.Log
import com.dashlane.session.Session
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.util.anonymize
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import javax.inject.Inject

interface AccountStatusStorage {
    fun saveAccountStatus(session: Session, newStatus: AccountStatus): Boolean

    fun readAccountStatus(session: Session): AccountStatus?
}

class AccountStatusStorageImpl @Inject constructor(
    private val userSecureDataStorageManager: UserSecureStorageManager,
    private val gson: Gson
) : AccountStatusStorage {
    override fun saveAccountStatus(session: Session, newStatus: AccountStatus): Boolean {
        val jsonStatus = gson.toJson(newStatus)
        if (jsonStatus.isNullOrBlank()) return false

        userSecureDataStorageManager.storeAccountStatus(session, jsonStatus)

        return newStatus == readAccountStatus(session)
    }

    override fun readAccountStatus(session: Session): AccountStatus? =
        try {
            val storedStatus = userSecureDataStorageManager.readAccountStatus(session) ?: throw NullPointerException()
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