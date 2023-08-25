package com.dashlane.util.log

import com.dashlane.authentication.accountsmanager.AccountsManager
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import javax.inject.Inject

class FirstLaunchDetector @Inject constructor(
    private val preferencesManager: GlobalPreferencesManager,
    private val accountsManager: AccountsManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver
) {
    fun detect() {
        if (preferencesManager.isInitialRunFinished) {
            return
        }
        
        
            "Count of GlobalPreferences stored: ${preferencesManager.count()}",
            logToUserSupportFile = true
        )
        accountsManager.clearAllAccounts()
        sessionCredentialsSaver.deleteSavedCredentials(preferencesManager.getDefaultUsername())
        preferencesManager.isInitialRunFinished = true
    }
}
