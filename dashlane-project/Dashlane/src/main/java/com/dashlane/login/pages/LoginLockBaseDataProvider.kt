package com.dashlane.login.pages

import android.content.Intent
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.sso.MigrationToSsoMemberInfo

abstract class LoginLockBaseDataProvider<T : LoginLockBaseContract.Presenter>(
    val lockManager: LockManager,
    private val successIntentFactory: LoginSuccessIntentFactory
) :
    LoginBaseDataProvider<T>(),
    LoginLockBaseContract.DataProvider {
    final override lateinit var lockSetting: LockSetting
        private set

    var migrationToSsoMemberInfoProvider: (() -> MigrationToSsoMemberInfo?)? = null

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
}