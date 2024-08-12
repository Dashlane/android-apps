package com.dashlane.accountrecoverykey.enforce

import android.app.Activity
import android.os.Bundle
import com.dashlane.user.UserAccountInfo
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountrecoverykey.AccountRecoveryKeySetupNavigation
import com.dashlane.navigation.Navigator
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class AccountRecoveryKeyEnforcer @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
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
            if (userPreferencesManager.mplessARKEnabled) return@launch
            val accountType = UserAccountInfo.AccountType.fromString(userPreferencesManager.accountType)
            if (accountType == UserAccountInfo.AccountType.MasterPassword) return@launch

            val arkStatus = accountRecoveryKeyRepository.getAccountRecoveryStatusAsync()
            if (arkStatus.enabled) {
                userPreferencesManager.mplessARKEnabled = true
                return@launch
            }

            withContext(mainCoroutineDispatcher) {
                navigator.goToAccountRecoveryKey(
                    settingsId = "account-recovery",
                    startDestination = AccountRecoveryKeySetupNavigation.introDestination,
                    userCanExitFlow = false
                )
            }
        }
    }
}
