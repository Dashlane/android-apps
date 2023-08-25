package com.dashlane.biometricrecovery

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogConstant
import javax.inject.Inject

class BiometricRecoveryLoggerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : BiometricRecoveryLogger {
    override fun logAccountRecoveryActivation(enabled: Boolean, originViewType: String?) {
        val action =
            if (enabled) UsageLogConstant.ActionType.accountRecoveryOn else UsageLogConstant.ActionType.accountRecoveryOff
        log(originViewType, action)
    }

    override fun logBiometricIntroDisplay() = log(UsageLogConstant.ViewType.biometricIntro, "display")

    override fun logAccountRecoveryIntroDisplay() = log(UsageLogConstant.ViewType.accountRecoveryIntro, "display")

    override fun logBiometricRecoveryIntroDialogDisplay() =
        log(UsageLogConstant.ViewType.accountRecoveryIntroDialog, "display")

    override fun logPromptBiometricForRecovery(origin: String) = log(origin, "promptBiometricForRecovery")

    override fun logGoToChangeMP(origin: String) = log(origin, "goToChangeMP")

    private fun log(type: String?, action: String?) {
        sessionManager.session
            ?.let { bySessionUsageLogRepository[it] }
            ?.enqueue(
                UsageLogCode35(
                    type = type,
                    action = action
                )
            )
    }
}