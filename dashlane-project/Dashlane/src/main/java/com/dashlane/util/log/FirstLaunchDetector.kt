package com.dashlane.util.log

import android.content.Context
import com.dashlane.analytics.referrer.ReferrerManager
import com.dashlane.authentication.accountsmanager.AccountsManager
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.useractivity.log.install.InstallLogCode17
import com.dashlane.useractivity.log.install.InstallLogCode43
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.util.Constants
import com.dashlane.util.PackageUtilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject



class FirstLaunchDetector @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val preferencesManager: GlobalPreferencesManager,
    private val userSupportFileLogger: UserSupportFileLogger,
    private val accountsManager: AccountsManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val installLogRepository: InstallLogRepository
) {
    fun detect() {
        if (preferencesManager.isInitialRunFinished) {
            return
        }
        
        
        userSupportFileLogger.add("Count of GlobalPreferences stored: ${preferencesManager.count()}")
        sendInstallLogs()
        userSupportFileLogger.add("First run")
        accountsManager.clearAllAccounts()
        sessionCredentialsSaver.deleteSavedCredentials(preferencesManager.getDefaultUsername())
        preferencesManager.isInitialRunFinished = true
    }

    private fun sendInstallLogs() {
        installLogRepository.enqueue(InstallLogCode17(subStep = "9"))
        sendInstallLogCode43()
    }

    private fun sendInstallLogCode43() {
        val referrer: String?
        val referrerOrigin = ReferrerManager.getInstance().referrerOrigin
        val uniqueReferralId: String?
        val referralDetails: String?
        if (referrerOrigin == null || referrerOrigin.isEmpty()) {
            referrer = preferencesManager.getString(Constants.MARKETING.REFFERAL_STRING)
            uniqueReferralId = null
            referralDetails = null
        } else {
            referrer = referrerOrigin
            uniqueReferralId = preferencesManager.getString(ConstantsPrefs.REFERRER_UNIQUE_REF_ID)
            referralDetails = preferencesManager.getString(ConstantsPrefs.REFERRER_ORIGIN_PACKAGE)
        }
        installLogRepository.enqueue(
            InstallLogCode43(
                store = PackageUtilities.getInstallerOrigin(context),
                referrer = referrer,
                uniqueReferralId = uniqueReferralId,
                referralDetail = referralDetails
            )
        )
    }
}
