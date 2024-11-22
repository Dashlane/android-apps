package com.dashlane.authentication.accountsmanager

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@Deprecated(
    "Account Manager is no more used to store the local key when setting up Pin or Biometrics authentication. " +
            "We're only keeping restricted access for migration purposes. See SessionRestorerImpl for more info"
)
class AccountsManager @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val accountManager: AccountManager
        get() = AccountManager.get(context)

    fun getPassword(username: String): AccountsManagerPassword? {
        if (username.isBlank()) {
            return null
        }
        try {
            val accountManager = accountManager
            if (accountManager.getAccountsByType(ACCOUNT_TYPE).isEmpty()) {
                return null
            }

            val account = Account(username, ACCOUNT_TYPE)
            val data = accountManager.getPassword(account)

            if (data.isNullOrEmpty()) {
                return null
            }

            val isLocalKey = PASSWORD_TYPE_LOCAL_KEY == accountManager.getUserData(account, PASSWORD_TYPE)
            val serverKey = accountManager.getUserData(account, SERVER_KEY)

            return AccountsManagerPassword(data, isLocalKey, serverKey)
        } catch (ex: SecurityException) {
            return null
        }
    }

    fun clearAllAccounts() {
        val accountManager = accountManager
        var accounts: Array<Account?>? = null
        try {
            accounts = accountManager.getAccountsByType(ACCOUNT_TYPE)
        } catch (ex: SecurityException) {
            
        }

        if (accounts == null) return
        for (account in accounts) {
            accountManager.removeAccountExplicitly(account)
        }
    }

    companion object {
        private const val ACCOUNT_TYPE = "com.dashlane"

        private const val SERVER_KEY = "serverkey"

        private const val PASSWORD_TYPE = "passwordtype"
        private const val PASSWORD_TYPE_LOCAL_KEY = "lk"
    }
}
