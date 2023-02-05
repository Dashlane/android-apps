package com.dashlane.util.hardwaresecurity

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository

class AuthModuleLogger(
    private val featurePrefName: String,
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) {

    fun logUsageStartRegisterProcess() {
        sendUsageLog35("startRegister")
    }

    fun logUsageAuthSuccess(referrer: String?) {
        sendUsageLog35("authSuccess")
        sendUsageLog75(UsageLogConstant.LockAction.unlock, referrer)
    }

    fun logUsageAuthFailure(referrer: String?) {
        sendUsageLog35("authFail")
        sendUsageLog75(UsageLogConstant.LockAction.wrong, referrer)
    }

    fun logUsageLogoutFromBiometrics() {
        sendUsageLog35("askLogout")
        sendUsageLog75(UsageLogConstant.LockAction.logout, UsageLogConstant.LockSubAction.fromApp)
    }

    fun logUsageCancelAuthorisation() {
        sendUsageLog35("askCancel")
        sendUsageLog75(UsageLogConstant.LockAction.cancel, UsageLogConstant.LockSubAction.fromApp)
    }

    fun logUsageStartFeature() {
        sendUsageLog35("on")
    }

    fun logUsageStopFeature() {
        sendUsageLog35("off")
    }

    fun logUsageStartOnboarding() {
        sendUsageLog35("onbardingStart")
    }

    private fun sendUsageLog35(action: String) {
        log(
            UsageLogCode35(
                type = featurePrefName,
                action = action
            )
        )
    }

    private fun sendUsageLog75(action: String, subaction: String?) {
        log(
            UsageLogCode75(
                type = UsageLogConstant.ViewType.lock,
                subtype = UsageLogConstant.LockType.fingerPrint,
                action = action,
                subaction = subaction
            )
        )
    }

    private fun log(log: UsageLog) {
        bySessionUsageLogRepository[sessionManager.session]?.enqueue(log)
    }
}