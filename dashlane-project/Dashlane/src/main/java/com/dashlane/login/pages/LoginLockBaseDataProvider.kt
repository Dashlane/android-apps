package com.dashlane.login.pages

import android.content.Intent
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.inapplogin.sendAutofillActivationLog
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.sso.MigrationToSsoMemberInfo
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository

abstract class LoginLockBaseDataProvider<T : LoginLockBaseContract.Presenter>(
    val lockManager: LockManager,
    private val successIntentFactory: LoginSuccessIntentFactory,
    private val inAppLoginManager: InAppLoginManager,
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) :
    LoginBaseDataProvider<T>(),
    LoginLockBaseContract.DataProvider {
    final override lateinit var lockSetting: LockSetting
        private set

    var migrationToSsoMemberInfoProvider: (() -> MigrationToSsoMemberInfo?)? = null

    val usageLogRepository: UsageLogRepository?
        get() = bySessionUsageLogRepository[sessionManager.session]

    override fun log(log: UsageLog) {
        usageLogRepository?.enqueue(log)
    }

    override fun initLockSetting(lockSetting: LockSetting) {
        this.lockSetting = lockSetting
    }

    override fun onUnlockSuccess(): Intent? {
        val resultIntent = lockSetting.unlockReason?.let { reason ->
            lockManager.sendUnLock(reason, true)
            Intent().apply {
                putExtra(LockSetting.EXTRA_LOCK_REASON, lockSetting.unlockReason)
            }
        }
        sendAutofillActivationLog(usageLogRepository, inAppLoginManager)
        return resultIntent
    }

    override fun createNextActivityIntent(): Intent? {
        val migrationToSsoMemberInfo = migrationToSsoMemberInfoProvider?.invoke()

        return when {
            migrationToSsoMemberInfo != null -> successIntentFactory.createMigrationToSsoMemberIntent(
                login = migrationToSsoMemberInfo.login,
                serviceProviderUrl = migrationToSsoMemberInfo.serviceProviderUrl,
                isNitroProvider = migrationToSsoMemberInfo.isNitroProvider,
                totpAuthTicket = migrationToSsoMemberInfo.totpAuthTicket
            )
            lockSetting.redirectToHome -> successIntentFactory.createApplicationHomeIntent()
            else -> null
        }
    }

    override fun logUsageLogCode75(action: String) {
        log(
            UsageLogCode75(
                type = UsageLogConstant.ViewType.lock,
                subtype = presenter.lockTypeName,
                subaction = lockSetting.getLogReferrer(),
                website = lockSetting.lockWebsite,
                action = action
            )
        )
    }

    internal fun usageLogUnlock() {
        logUsageLogCode75(UsageLogConstant.LockAction.unlock)
    }

    internal fun usageLogFailedUnlockAttempt() {
        logUsageLogCode75(UsageLogConstant.LockAction.wrong)
    }
}