package com.dashlane.ui.screens.settings.list.security

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.fragment.app.FragmentActivity
import com.dashlane.R
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountrecovery.AccountRecovery
import com.dashlane.accountrecovery.AccountRecoveryBiometricIntroActivity
import com.dashlane.activatetotp.ActivateTotpLogger
import com.dashlane.disabletotp.DisableTotpActivity
import com.dashlane.disabletotp.DisableTotpEnforcedIntroActivity
import com.dashlane.activatetotp.DownloadAuthenticatorAppIntroActivity
import com.dashlane.authenticator.isAuthenticatorAppInstalled
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.lock.OnboardingApplicationLockActivity
import com.dashlane.security.SecurityHelper
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.serverKeyUtf8Bytes
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.TeamspaceRestrictionNotificator
import com.dashlane.teamspaces.manager.is2FAEnforced
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.screens.settings.SingleChoiceDialog
import com.dashlane.ui.screens.settings.Use2faSettingStateHolder
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingChange
import com.dashlane.ui.screens.settings.item.SettingCheckable
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.screens.settings.item.SettingLoadable
import com.dashlane.ui.util.DialogHelper
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.clearTop
import com.dashlane.util.getBaseActivity
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.tryOrNull
import java.time.Duration
import java.util.Arrays



class SettingsSecurityApplicationLockList(
    private val context: Context,
    private val lockManager: LockManager,
    securityHelper: SecurityHelper,
    biometricAuthModule: BiometricAuthModule,
    teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    sessionManager: SessionManager,
    userAccountStorage: UserAccountStorage,
    private val dialogHelper: DialogHelper,
    sensibleSettingsClickHelper: SensibleSettingsClickHelper,
    private val accountRecovery: AccountRecovery,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    use2faSettingStateHolder: Use2faSettingStateHolder,
    activateTotpLogger: ActivateTotpLogger,
    private val sessionCredentialsSaver: SessionCredentialsSaver
) {

    private val teamspaceNotificator = TeamspaceRestrictionNotificator()

    private val appLockHeader =
        SettingHeader(context.getString(R.string.settings_app_lock))

    private val lockPinCodeItem = object : SettingItem, SettingCheckable, SettingChange.Listenable {

        override var listener: SettingChange.Listener? = null
        override val id = "pin-lock"
        override val header = appLockHeader
        override val title = context.getString(R.string.settings_use_pincode)
        override val description = context.getString(R.string.setting_use_pincode_description)
        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true
        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) =
            getLockType(lockManager) == LockTypeManager.LOCK_TYPE_PIN_CODE

        override fun onCheckChanged(context: Context, enable: Boolean) {
            if (!securityHelper.allowedToUsePin()) {
                securityHelper.showPopupPinCodeDisable(context)
                return
            }
            if (enable) {
                
                runIfAccountRecoveryDeactivationAcknowledged(context) {
                    sensibleSettingsClickHelper.perform(context = context) {
                        lockManager.showLockActivityToSetPinCode(context, false)
                    }
                }
            } else {
                

                bySessionUsageLogRepository[sessionManager.session]?.apply {
                    enqueue(
                        UsageLogCode35(
                            type = UsageLogConstant.ViewType.pin,
                            action = UsageLogConstant.ActionType.setupUseMasterPasswordSelected
                        )
                    )
                    enqueue(
                        UsageLogCode35(
                            type = UsageLogConstant.ViewType.settings,
                            action = UsageLogConstant.ActionType.usePinCodeOff
                        )
                    )
                    enqueue(
                        UsageLogCode35(
                            type = UsageLogConstant.ViewType.settings,
                            action = UsageLogConstant.ActionType.storeMasterPasswordOff
                        )
                    )
                }
                lockManager.setLockType(LockTypeManager.LOCK_TYPE_MASTER_PASSWORD)
                listener?.onSettingsInvalidate()
            }
        }
    }

    private val lockFingerprintItem =
        object : SettingItem, SettingCheckable, SettingChange.Listenable {
            override var listener: SettingChange.Listener? = null
            override val id = "fp-lock"
            override val header = appLockHeader
            override val title = context.getString(R.string.setting_use_google_fingerprint_title)
            override val description =
                context.getString(R.string.setting_use_google_fingerprint_description)

            override fun isEnable(context: Context) = true
            override fun isVisible(context: Context) = biometricAuthModule.isHardwareSupported()
            override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
            override fun isChecked(context: Context) = biometricAuthModule.isFeatureEnabled()

            override fun onCheckChanged(context: Context, enable: Boolean) {
                if (enable) {
                    sensibleSettingsClickHelper.perform(context = context) {
                        
                        biometricAuthModule.startOnboarding(context)
                    }
                } else {
                    runIfAccountRecoveryDeactivationAcknowledged(context) {
                        
                        lockManager.setLockType(LockTypeManager.LOCK_TYPE_MASTER_PASSWORD)
                        listener?.onSettingsInvalidate()
                    }
                }
            }
        }

    private val disableLocalOtp2Item = object : SettingItem, SettingCheckable {
        override val id = "disable-opt2"
        override val header = appLockHeader
        override val title = context.getString(R.string.settings_disable_totp2_for_device)
        override val description =
            context.getString(R.string.settings_disable_totp2_for_device_description)

        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context): Boolean {
            if (!isOtp2()) return false
            val lockType = getLockType(lockManager)
            return lockType == LockTypeManager.LOCK_TYPE_PIN_CODE || lockType == LockTypeManager.LOCK_TYPE_BIOMETRIC
        }

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))

        override fun isChecked(context: Context): Boolean {
            val session = sessionManager.session
            return if (session == null) false else sessionCredentialsSaver.hasServerKey(session.username)
        }

        override fun onCheckChanged(context: Context, enable: Boolean) {
            val session = sessionManager.session ?: return
            sensibleSettingsClickHelper.perform(
                context = context,
                masterPasswordRecommended = enable
            ) {
                val ulAction = if (enable) {
                    sessionCredentialsSaver.saveServerKey(
                        serverKey = session.appKey.serverKeyUtf8Bytes?.use(ObfuscatedByteArray::toByteArray),
                        localKey = session.localKey,
                        username = session.username
                    )
                    UsageLogConstant.ActionType.disable2FAOn
                } else {
                    sessionCredentialsSaver.removeServerKey(session.username)
                    UsageLogConstant.ActionType.disable2FAOff
                }
                bySessionUsageLogRepository[sessionManager.session]
                    ?.enqueue(
                        UsageLogCode35(
                            type = UsageLogConstant.ViewType.settings,
                            action = ulAction
                        )
                    )
            }
        }

        private fun isOtp2() = sessionManager.session
            ?.username
            ?.let { userAccountStorage[it] }
            ?.otp2
            ?: false
    }

    private val unlockItemPinCode = object : SettingItem, SettingCheckable {
        override val id = "unlock-item-pin"
        override val header = appLockHeader
        override val title = context.getString(R.string.setting_unlock_item_with_pincode)
        override val description =
            context.getString(R.string.setting_unlock_item_with_pincode_description)

        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) =
            getLockType(lockManager) == LockTypeManager.LOCK_TYPE_PIN_CODE

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = lockManager.isItemUnlockableByPinOrFingerprint()

        override fun onCheckChanged(context: Context, enable: Boolean) {
            if (!securityHelper.allowedToUsePin()) {
                securityHelper.showPopupPinCodeDisable(context)
                return
            }
            sensibleSettingsClickHelper.perform(
                context = context,
                masterPasswordRecommended = enable
            ) {
                lockManager.setItemUnlockableByPinOrFingerprint(enable)
                bySessionUsageLogRepository[sessionManager.session]
                    ?.enqueue(
                        UsageLogCode35(
                            type = "pin",
                            action = if (enable) "usePinToUnlockItemsON" else "usePinToUnlockItemsOFF"
                        )
                    )
            }
        }
    }

    private val unlockItemFingerprint = object : SettingItem, SettingCheckable {
        override val id = "unlock-item-fingerprint"
        override val header = unlockItemPinCode.header
        override val title = context.getString(R.string.setting_unlock_item_with_fingerprint)
        override val description =
            context.getString(R.string.setting_unlock_item_with_fingerprint_description)

        override fun isEnable(context: Context) = unlockItemPinCode.isEnable(context)
        override fun isVisible(context: Context) =
            getLockType(lockManager) == LockTypeManager.LOCK_TYPE_BIOMETRIC

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = unlockItemPinCode.isChecked(context)

        override fun onCheckChanged(context: Context, enable: Boolean) =
            unlockItemPinCode.onCheckChanged(context, enable)
    }

    private val autoLockOnExitItem = object : SettingItem, SettingCheckable {
        override val id = "auto-lock-exit"
        override val header = appLockHeader
        override val title = context.getString(R.string.settings_lock_on_exit)
        override val description = context.getString(R.string.setting_lock_on_exit_description)
        override fun isEnable(context: Context) =
            teamspaceAccessorProvider.get()?.isFeatureEnabled(Teamspace.Feature.AUTOLOCK) ?: false

        override fun isVisible(context: Context) = true

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = lockManager.isLockOnExit()

        override fun onCheckChanged(context: Context, enable: Boolean) {
            if (!isEnable(context)) {
                teamspaceNotificator.notifyFeatureRestricted(
                    context.getBaseActivity() as FragmentActivity,
                    Teamspace.Feature.AUTOLOCK
                )
                return
            }

            
            sensibleSettingsClickHelper.perform(
                context = context,
                masterPasswordRecommended = !enable
            ) {
                bySessionUsageLogRepository[sessionManager.session]
                    ?.enqueue(
                        UsageLogCode35(
                            type = UsageLogConstant.ViewType.settings,
                            action = if (enable) {
                                UsageLogConstant.ActionType.lockOnExitOn
                            } else {
                                UsageLogConstant.ActionType.lockOnExitOff
                            }
                        )
                    )
                lockManager.setLockOnExit(enable)
            }
        }
    }

    private val autoLockTimeItem = object : SettingItem, SettingChange.Listenable {
        override var listener: SettingChange.Listener? = null
        override val id = "auto-lock-time"
        override val header = appLockHeader
        override val title = context.getString(R.string.settings_use_lock_timeout)
        override val description: String
            get() = getCurrentTimeoutArrayPosition(context.resources).let { pos ->
                if (pos <= 0) {
                    context.getString(R.string.settings_use_lock_timeout_description_never_lock)
                } else {
                    context.getString(
                        R.string.settings_use_lock_timeout_description,
                        context.resources.getStringArray(R.array.timeout_strings)[pos]
                    )
                }
            }

        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true

        override fun onClick(context: Context) {
            sensibleSettingsClickHelper.perform(
                context = context,
                masterPasswordRecommended = true
            ) {
                showTimeSelector(
                    context
                )
            }
        }

        private fun showTimeSelector(context: Context) {
            val selectedItem = getCurrentTimeoutArrayPosition(context.resources)

            SingleChoiceDialog(dialogHelper)
                .show(
                    context,
                    R.string.setting_choose_timeout,
                    R.array.timeout_strings,
                    selectedItem
                ) { which ->
                    val timeOutValue = context.resources.getIntArray(R.array.timeout_values)[which]
                        .takeIf { it > 0 }
                        ?.let { Duration.ofSeconds(it.toLong()) }
                    lockManager.lockTimeout = timeOutValue
                    (when (timeOutValue) {
                        null -> UsageLogConstant.ActionType.autoLockTimeNever
                        Duration.ofSeconds(5) -> UsageLogConstant.ActionType.autoLockTime5s
                        Duration.ofSeconds(30) -> UsageLogConstant.ActionType.autoLockTime30s
                        Duration.ofMinutes(1) -> UsageLogConstant.ActionType.autoLockTime1m
                        Duration.ofMinutes(3) -> UsageLogConstant.ActionType.autoLockTime3m
                        Duration.ofMinutes(5) -> UsageLogConstant.ActionType.autoLockTime5m
                        Duration.ofMinutes(15) -> UsageLogConstant.ActionType.autoLockTime15m
                        else -> null
                    })?.let {
                        bySessionUsageLogRepository[sessionManager.session]
                            ?.enqueue(
                                UsageLogCode35(
                                    type = UsageLogConstant.ViewType.settings,
                                    action = it
                                )
                            )
                    }
                    listener?.onSettingsInvalidate()
                }
        }

        private fun getCurrentTimeoutArrayPosition(res: Resources): Int {
            val timeout = lockManager.lockTimeout ?: return 0
            return Arrays.binarySearch(
                res.getIntArray(R.array.timeout_values),
                timeout.seconds.toInt()
            )
        }
    }

    private val accountRecoveryItem =
        object : SettingItem, SettingCheckable, SettingChange.Listenable {
            override var listener: SettingChange.Listener? = null
            override val id = "master-password-reset"
            override val header = lockFingerprintItem.header
            override val title = context.getString(R.string.account_recovery_setting_title)
            override val description =
                context.getString(R.string.account_recovery_setting_description)

            override fun isEnable(context: Context) = true
            override fun isVisible(context: Context) = accountRecovery.isFeatureAvailable

            override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))

            override fun isChecked(context: Context) =
                accountRecovery.isFeatureEnabled && biometricAuthModule.isFeatureEnabled()

            override fun onCheckChanged(context: Context, enable: Boolean) {
                accountRecovery.isFeatureKnown = true

                if (!enable) {
                    dialogHelper.builder(context)
                        .setTitle(R.string.account_recovery_deactivation_title)
                        .setMessage(R.string.account_recovery_deactivation_message)
                        .setPositiveButton(R.string.account_recovery_deactivation_positive_cta) { _, _ ->
                            accountRecovery.setFeatureEnabled(
                                false,
                                UsageLogConstant.ViewType.settings
                            )
                            listener?.onSettingsInvalidate()
                        }
                        .setNegativeButton(
                            R.string.account_recovery_deactivation_negative_cta,
                            null
                        )
                        .show()
                    return
                }

                sensibleSettingsClickHelper.perform(context = context) {
                    val hasBiometric =
                        getLockType(lockManager) == LockTypeManager.LOCK_TYPE_BIOMETRIC

                    if (!hasBiometric) {
                        context.startActivity(
                            AccountRecoveryBiometricIntroActivity.newIntent(context)
                                .clearTop()
                        )
                    } else {
                        accountRecovery.setFeatureEnabled(true, UsageLogConstant.ViewType.settings)
                    }
                }
            }
        }

    private fun getLockType(lockManager: LockManager): Int {
        return tryOrNull { lockManager.getLockType() } ?: LockTypeManager.LOCK_TYPE_MASTER_PASSWORD
    }

    private fun runIfAccountRecoveryDeactivationAcknowledged(
        uiContext: Context,
        action: () -> Unit
    ) {
        if (accountRecovery.isFeatureEnabled) {
            dialogHelper.builder(uiContext)
                .setTitle(R.string.account_recovery_biometric_deactivation_title)
                .setMessage(R.string.account_recovery_biometric_deactivation_message)
                .setPositiveButton(R.string.account_recovery_biometric_deactivation_positive_cta) { _, _ ->
                    accountRecovery.setFeatureEnabled(false, UsageLogConstant.ViewType.settings)
                    action()
                }
                .setNegativeButton(
                    R.string.account_recovery_biometric_deactivation_negative_cta,
                    null
                )
                .show()
        } else {
            action()
        }
    }

    private val use2faItem = object : SettingItem, SettingCheckable, SettingLoadable {
        private val use2faSettingState get() = use2faSettingStateHolder.use2faSettingStateFlow.value
        private val authAppInstalled = context.isAuthenticatorAppInstalled()

        override val id get() = "use-2fa"

        override val header get() = appLockHeader

        override val title
            get() = context.getString(
                if (authAppInstalled) {
                    R.string.settings_use_2fa_auth_app_installed
                } else {
                    R.string.settings_use_2fa
                }
            )

        override val description
            get() = context.getString(
                if (authAppInstalled) {
                    R.string.settings_use_2fa_description_auth_app_installed
                } else {
                    R.string.settings_use_2fa_description
                }
            )

        override fun isVisible(context: Context) = use2faSettingState.visible

        override fun isEnable(context: Context) = use2faSettingState.enabled

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))

        override fun isChecked(context: Context) = use2faSettingState.checked

        override fun isLoaded(context: Context) = use2faSettingState.loaded

        override fun onCheckChanged(context: Context, enable: Boolean) {
            if (!use2faSettingState.enabled) return

            if (enable) {
                activateTotpLogger.logActivationStart()
                val nextIntent = Intent(
                    context,
                    DownloadAuthenticatorAppIntroActivity::class.java
                )
                context.startActivity(
                    OnboardingApplicationLockActivity.newIntent(
                        context,
                        nextIntent = nextIntent,
                        fromUse2fa = true
                    )
                )
            } else {
                if (teamspaceAccessorProvider.get().is2FAEnforced()) {
                    context.startActivity(Intent(context, DisableTotpEnforcedIntroActivity::class.java).clearTop())
                } else {
                    dialogHelper
                        .builder(context, com.dashlane.activatetotp.R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
                        .setTitle(com.dashlane.activatetotp.R.string.disable_totp_confirmation_title)
                        .setMessage(com.dashlane.activatetotp.R.string.disable_totp_confirmation_message)
                        .setPositiveButton(com.dashlane.activatetotp.R.string.disable_totp_confirmation_cta_positive) { _, _ ->
                            context.startActivity(Intent(context, DisableTotpActivity::class.java).clearTop())
                        }
                        .setNegativeButton(com.dashlane.activatetotp.R.string.disable_totp_confirmation_cta_negative) { _, _ -> }
                        .setCancelable(false)
                        .show()
                }
            }
        }
    }

    fun getAll() = listOf(
        lockPinCodeItem,
        lockFingerprintItem,
        use2faItem,
        accountRecoveryItem,
        disableLocalOtp2Item,
        unlockItemPinCode,
        unlockItemFingerprint,
        autoLockOnExitItem,
        autoLockTimeItem
    )
}
