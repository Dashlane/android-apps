package com.dashlane.accountrecoverykey.enforce

import android.app.Activity
import android.os.Bundle
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.navigation.Navigator
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.user.UserAccountInfo
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRecoveryKeyEnforcer @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    private val navigator: Navigator,
    @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher private val mainCoroutineDispatcher: CoroutineDispatcher,
) : AbstractActivityLifecycleListener() {

    override fun onFirstLoggedInActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onFirstLoggedInActivityCreated(activity, savedInstanceState)
        checkArkStatus()
    }

    private fun checkArkStatus() {
        applicationCoroutineScope.launch {
            val preferences = preferencesManager[sessionManager.session?.username]
            if (preferences.mplessARKEnabled) return@launch
            val accountType = preferences.accountType?.let { UserAccountInfo.AccountType.fromString(it) } ?: return@launch
            if (accountType == UserAccountInfo.AccountType.MasterPassword) return@launch

            val arkStatus = accountRecoveryKeyRepository.getAccountRecoveryStatusAsync()
            if (arkStatus.enabled) {
                preferences.mplessARKEnabled = true
            } else {
                withContext(mainCoroutineDispatcher) {
                    navigator.goToAccountRecoveryKey(
                        settingsId = "account-recovery",
                        showIntro = true,
                        userCanExitFlow = false
                    )
                }
            }
        }
    }
}
