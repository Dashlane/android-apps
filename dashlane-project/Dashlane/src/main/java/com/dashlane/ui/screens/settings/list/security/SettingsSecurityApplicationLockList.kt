package com.dashlane.ui.screens.settings.list.security

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.fragment.app.FragmentActivity
import com.dashlane.R
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountrecoverykey.setting.AccountRecoveryKeySettingState
import com.dashlane.accountrecoverykey.setting.AccountRecoveryKeySettingStateHolder
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.biometricrecovery.BiometricRecoveryIntroActivity
import com.dashlane.crypto.keys.serverKeyUtf8Bytes
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.disabletotp.DisableTotpActivity
import com.dashlane.disabletotp.DisableTotpEnforcedIntroActivity
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.hardwaresecurity.SecurityHelper
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockType
import com.dashlane.navigation.Navigator
import com.dashlane.pin.settings.PinSettingsActivity
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.ui.Feature
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.screens.settings.SingleChoiceDialog
import com.dashlane.ui.screens.settings.Use2faSettingStateHolder
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingChange
import com.dashlane.ui.screens.settings.item.SettingCheckable
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.screens.settings.item.SettingLoadable
import com.dashlane.ui.util.DialogHelper
import com.dashlane.user.UserAccountInfo
import com.dashlane.util.clearTop
import com.dashlane.util.getBaseActivity
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.tryOrNull
import java.time.Duration
import java.util.Arrays

class SettingsSecurityApplicationLockList(
    private val context: Context,
    private val lockManager: LockManager,
    securityHelper: SecurityHelper,
    biometricAuthModule: BiometricAuthModule,
    val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val sessionManager: SessionManager,
    private val userAccountStorage: UserAccountStorage,
    private val dialogHelper: DialogHelper,
    sensibleSettingsClickHelper: SensibleSettingsClickHelper,
    private val biometricRecovery: BiometricRecovery,
    use2faSettingStateHolder: Use2faSettingStateHolder,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val navigator: Navigator,
    private val teamspaceNotificator: TeamSpaceRestrictionNotificator,
    private val accountRecoveryKeySettingStateHolder: AccountRecoveryKeySettingStateHolder,
) {

    private val appLockHeader =
        SettingHeader(context.getString(R.string.settings_app_lock))

    private val teamSpaceAccessor: TeamSpaceAccessor?
        get() = teamSpaceAccessorProvider.get()

    private val lockPinCodeItem = object : SettingItem, SettingCheckable, SettingChange.Listenable {

        override var listener: SettingChange.Listener? = null
        override val id = "pin-lock"
        override val header = appLockHeader
        override val title = context.getString(R.string.settings_use_pincode)
        override val description = context.getString(R.string.setting_use_pincode_description)
        override fun isEnable() = true
        override fun isVisible() = isAccountTypeMasterPassword()
        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = LockType.PinCode in getLocks(lockManager)

        override fun onCheckChanged(context: Context, enable: Boolean) {
            if (!securityHelper.isDeviceSecured(sessionManager.session?.username)) {
                securityHelper.showPopupPinCodeDisable(context)
                return
            }
            if (enable) {
                
                sensibleSettingsClickHelper.perform(context = context, forceMasterPassword = true) {
                    context.startActivity(Intent(context, PinSettingsActivity::class.java))
                }
            } else {
                
                sensibleSettingsClickHelper.perform(context = context, forceMasterPassword = true) {
                    sessionManager.session?.username?.let { lockManager.removeLock(it, LockType.PinCode) }
                    listener?.onSettingsInvalidate()
                }
            }
        }
    }

    private val changePinCodeItem = object : SettingItem, SettingChange.Listenable {

        override var listener: SettingChange.Listener? = null
        override val id = "pin-change"
        override val header = appLockHeader
        override val title = context.getString(R.string.settings_change_pincode)
        override val description = context.getString(R.string.settings_change_pincode_description)
        override fun isEnable() = true
        override fun isVisible() = isPinLockEnabled()
        override fun onClick(context: Context) = sensibleSettingsClickHelper.perform(context = context, forceMasterPassword = true) {
            context.startActivity(Intent(context, PinSettingsActivity::class.java))
        }

        private fun isPinLockEnabled(): Boolean {
            val username = sessionManager.session?.username ?: return false
            val accountType = userAccountStorage[username]?.accountType
            return accountType is UserAccountInfo.AccountType.InvisibleMasterPassword ||
                LockType.PinCode in lockManager.getLocks(username)
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

            override fun isEnable() = true
            override fun isVisible() = biometricAuthModule.isHardwareSupported()
            override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
            override fun isChecked(context: Context) = sessionManager.session?.userId?.let { biometricAuthModule.isFeatureEnabled(it) } ?: false

            override fun onCheckChanged(context: Context, enable: Boolean) {
                if (enable) {
                    val isUsingPin = lockPinCodeItem.isChecked(context)
                    val positiveButtonText = if (isUsingPin) {
                        R.string.biometric_settings_weak_warning_dialog_positive_button_keep
                    } else {
                        R.string.biometric_settings_weak_warning_dialog_positive_button_setup
                    }
                    sensibleSettingsClickHelper.perform(context = context, forceMasterPassword = true) {
                        if (biometricAuthModule.isOnlyWeakSupported()) {
                            
                            dialogHelper.builder(context)
                                .setTitle(R.string.biometric_settings_weak_warning_dialog_title)
                                .setMessage(R.string.biometric_settings_weak_warning_dialog_message)
                                .setPositiveButton(positiveButtonText) { _, _ ->
                                    
                                    if (!isUsingPin) lockPinCodeItem.onClick(context)
                                }
                                .setNegativeButton(
                                    R.string.biometric_settings_weak_warning_dialog_negative_button,
                                ) { _, _ ->
                                    
                                    navigator.goToBiometricOnboarding(context)
                                }
                                .show()
                        } else {
                            
                            navigator.goToBiometricOnboarding(context)
                        }
                    }
                } else {
                    sensibleSettingsClickHelper.perform(context = context, forceMasterPassword = true) {
                        runIfBiometricRecoveryDeactivationAcknowledged(context) {
                            
                            sessionManager.session?.username?.let { lockManager.removeLock(it, LockType.Biometric) }
                            listener?.onSettingsInvalidate()
                        }
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

        override fun isEnable() = true
        override fun isVisible(): Boolean {
            if (!isOtp2()) return false
            val locks = getLocks(lockManager)
            return LockType.PinCode in locks || LockType.Biometric in locks
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
                if (enable) {
                    sessionCredentialsSaver.saveServerKey(
                        serverKey = session.appKey.serverKeyUtf8Bytes?.use(ObfuscatedByteArray::toByteArray),
                        localKey = session.localKey,
                        username = session.username
                    )
                } else {
                    sessionCredentialsSaver.removeServerKey(session.username)
                }
            }
        }

        private fun isOtp2() = sessionManager.session
            ?.username
            ?.let { userAccountStorage[it] }
            ?.otp2
            ?: false
    }

    private val autoLockOnExitItem = object : SettingItem, SettingCheckable {
        override val id = "auto-lock-exit"
        override val header = appLockHeader
        override val title = context.getString(R.string.settings_lock_on_exit)
        override val description = context.getString(R.string.setting_lock_on_exit_description)
        override fun isEnable() = teamSpaceAccessor?.isLockOnExitEnabled == false

        override fun isVisible() = true

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = lockManager.isLockOnExit()

        override fun onCheckChanged(context: Context, enable: Boolean) {
            if (!isEnable()) {
                teamspaceNotificator.notifyFeatureRestricted(context.getBaseActivity() as FragmentActivity, Feature.AUTOLOCK)
                return
            }

            
            sensibleSettingsClickHelper.perform(context = context, masterPasswordRecommended = !enable) {
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

        override fun isEnable() = true
        override fun isVisible() = true

        override fun onClick(context: Context) {
            sensibleSettingsClickHelper.perform(context = context, masterPasswordRecommended = true) {
                showTimeSelector(context)
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
                        .let { Duration.ofSeconds(it.toLong()) }
                    sessionManager.session?.username?.let { lockManager.updateLockTimeout(timeOutValue, it) }
                    listener?.onSettingsInvalidate()
                }
        }

        private fun getCurrentTimeoutArrayPosition(res: Resources): Int {
            val timeout = lockManager.lockTimeout
            return Arrays.binarySearch(
                res.getIntArray(R.array.timeout_values),
                timeout.seconds.toInt()
            )
        }
    }

    private val biometricRecoveryItem =
        object : SettingItem, SettingCheckable, SettingChange.Listenable {
            override var listener: SettingChange.Listener? = null
            override val id = "master-password-reset"
            override val header = lockFingerprintItem.header
            override val title = context.getString(R.string.account_recovery_setting_title)
            override val description =
                context.getString(R.string.account_recovery_setting_description)

            override fun isEnable() = true
            override fun isVisible() = biometricRecovery.isFeatureAvailable()

            override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))

            override fun isChecked(context: Context) =
                biometricRecovery.isFeatureEnabled && sessionManager.session?.userId?.let { biometricAuthModule.isFeatureEnabled(it) } ?: false

            override fun onCheckChanged(context: Context, enable: Boolean) {
                biometricRecovery.isFeatureKnown = true

                if (!enable) {
                    dialogHelper.builder(context)
                        .setTitle(R.string.account_recovery_deactivation_title)
                        .setMessage(R.string.account_recovery_deactivation_message)
                        .setPositiveButton(R.string.account_recovery_deactivation_positive_cta) { _, _ ->
                            biometricRecovery.setBiometricRecoveryFeatureEnabled(false)
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
                    if (biometricAuthModule.isOnlyWeakSupported()) {
                        
                        dialogHelper.builder(context)
                            .setTitle(R.string.account_recovery_weak_warning_dialog_title)
                            .setMessage(R.string.account_recovery_weak_warning_dialog_message)
                            .setPositiveButton(
                                R.string.account_recovery_weak_warning_dialog_positive_button
                            ) { _, _ ->
                                setupBiometricRecovery(context)
                            }
                            .setNegativeButton(
                                R.string.account_recovery_weak_warning_dialog_negative_button,
                                null
                            )
                            .show()
                    } else {
                        setupBiometricRecovery(context)
                    }
                }
            }

            private fun setupBiometricRecovery(context: Context) {
                val hasBiometric = LockType.Biometric in getLocks(lockManager)
                if (!hasBiometric) {
                    context.startActivity(
                        BiometricRecoveryIntroActivity.newIntent(context)
                            .clearTop()
                    )
                } else {
                    biometricRecovery.setBiometricRecoveryFeatureEnabled(true)
                }
            }
        }

    private val accountRecoveryKeyItem =
        object : SettingItem, SettingChange.Listenable, SettingLoadable {

            private val state: AccountRecoveryKeySettingState
                get() = accountRecoveryKeySettingStateHolder.uiState.value

            override var listener: SettingChange.Listener? = null
            override val id = "account-recovery"
            override val header = appLockHeader
            override val title = context.getString(R.string.account_recovery_key_setting_title)
            override val description
                get() = when (val state = state) {
                    is AccountRecoveryKeySettingState.Loaded ->
                        if (state.isEnabled) {
                            context.getString(R.string.account_recovery_key_setting_description_on)
                        } else {
                            context.getString(R.string.account_recovery_key_setting_description_off)
                        }
                    else -> ""
                }

            override fun isEnable() = true
            override fun isVisible() = state != AccountRecoveryKeySettingState.Hidden

            override fun onClick(context: Context) {
                if (state is AccountRecoveryKeySettingState.Loaded) {
                    sensibleSettingsClickHelper.perform(context = context, forceMasterPassword = true) {
                        navigator.goToAccountRecoveryKey(id)
                    }
                }
            }

            override fun isLoaded(context: Context): Boolean = state != AccountRecoveryKeySettingState.Loading
        }

    private fun getLocks(lockManager: LockManager): List<LockType> {
        return tryOrNull { sessionManager.session?.username?.let { lockManager.getLocks(it) } } ?: listOf(LockType.MasterPassword)
    }

    private fun runIfBiometricRecoveryDeactivationAcknowledged(
        uiContext: Context,
        action: () -> Unit
    ) {
        if (biometricRecovery.isFeatureEnabled) {
            dialogHelper.builder(uiContext)
                .setTitle(R.string.account_recovery_biometric_deactivation_title)
                .setMessage(R.string.account_recovery_biometric_deactivation_message)
                .setPositiveButton(R.string.account_recovery_biometric_deactivation_positive_cta) { _, _ ->
                    biometricRecovery.setBiometricRecoveryFeatureEnabled(false)
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

        override val id get() = "use-2fa"

        override val header get() = appLockHeader

        override val title
            get() = context.getString(R.string.settings_use_2fa)

        override val description
            get() = context.getString(R.string.settings_use_2fa_description)

        override fun isVisible() = use2faSettingState.visible

        override fun isEnable() = use2faSettingState.enabled

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))

        override fun isChecked(context: Context) = use2faSettingState.checked

        override fun isLoaded(context: Context) = use2faSettingState.loaded

        override fun onCheckChanged(context: Context, enable: Boolean) {
            if (!use2faSettingState.enabled) return

            if (teamSpaceAccessor?.is2FAEnforced == true) {
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

    fun getAll() = listOf(
        lockPinCodeItem,
        changePinCodeItem,
        lockFingerprintItem,
        use2faItem,
        biometricRecoveryItem,
        accountRecoveryKeyItem,
        disableLocalOtp2Item,
        autoLockOnExitItem,
        autoLockTimeItem
    )

    private fun isAccountTypeMasterPassword(): Boolean {
        return sessionManager.session?.username
            ?.let { username -> userAccountStorage[username]?.accountType }
            .let { accountType -> accountType is UserAccountInfo.AccountType.MasterPassword }
    }
}
