package com.dashlane.ui.screens.settings.list.security

import android.content.Context
import com.dashlane.R
import com.dashlane.account.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepository
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.lock.UnlockEvent
import com.dashlane.masterpassword.ChangeMasterPasswordActivity
import com.dashlane.masterpassword.ChangeMasterPasswordFeatureAccessChecker
import com.dashlane.masterpassword.ChangeMasterPasswordOrigin
import com.dashlane.masterpassword.warning.ChangeMPWarningDesktopActivity
import com.dashlane.navigation.Navigator
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserCryptographyRepository
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.screens.settings.SettingPrivacySetting
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingCheckable
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.util.DialogHelper
import com.dashlane.userfeatures.FeatureFlip
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.util.Toaster
import com.dashlane.util.getBaseActivity
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SettingsSecurityMiscList(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    navigator: Navigator,
    screenshotPolicy: ScreenshotPolicy,
    userPreferencesManager: UserPreferencesManager,
    teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    sessionManager: SessionManager,
    dialogHelper: DialogHelper,
    cryptographyRepository: UserCryptographyRepository,
    sensibleSettingsClickHelper: SensibleSettingsClickHelper,
    masterPasswordFeatureAccessChecker: ChangeMasterPasswordFeatureAccessChecker,
    toaster: Toaster,
    userFeaturesChecker: UserFeaturesChecker,
    userAccountStorage: UserAccountStorage,
    subscriptionCodeRepository: SubscriptionCodeRepository,
    @IoCoroutineDispatcher ioDispatcher: CoroutineDispatcher
) {

    private val miscHeader =
        SettingHeader(context.getString(R.string.settings_category_sharing))

    private val allowScreenshotItem = object : SettingItem, SettingCheckable {
        override val id = "allow-screenshot"
        override val header = miscHeader
        override val title = context.getString(R.string.settings_security_flag_title)
        override val description = context.getString(R.string.settings_security_flag_description)
        override fun isEnable() = true
        override fun isVisible() = isAccountTypeMasterPassword()

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = screenshotPolicy.areScreenshotAllowed()

        override fun onCheckChanged(context: Context, enable: Boolean) {
            sensibleSettingsClickHelper.perform(context = context, masterPasswordRecommended = enable) {
                screenshotPolicy.setScreenshotAllowed(enable)
                context.getBaseActivity()?.recreate()
            }
        }

        private fun isAccountTypeMasterPassword(): Boolean {
            return sessionManager.session?.username
                ?.let { username -> userAccountStorage[username]?.accountType }
                .let { accountType -> accountType is UserAccountInfo.AccountType.MasterPassword }
        }
    }

    private val clearClipboardItem = object : SettingItem, SettingCheckable {
        override val id = "clear-clipboard"
        override val header = miscHeader
        override val title = context.getString(R.string.settings_clear_clipboard)
        override val description = context.getString(R.string.settings_clear_clipboard_description)
        override fun isEnable() = true
        override fun isVisible() = true

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) =
            userPreferencesManager.getBoolean(ConstantsPrefs.CLEAR_CLIPBOARD_ON_TIMEOUT)

        override fun onCheckChanged(context: Context, enable: Boolean) {
            userPreferencesManager.putBoolean(ConstantsPrefs.CLEAR_CLIPBOARD_ON_TIMEOUT, enable)
        }
    }

    private val copy2FAClipboardItem = object : SettingItem, SettingCheckable {
        override val id = "copy-2FA-clipboard"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_automatically_copy_2fa)
        override val description = context.getString(R.string.setting_automatically_copy_2fa_description)
        override fun isEnable() = true
        override fun isVisible() = userFeaturesChecker.has(FeatureFlip.AUTOMATICALLY_COPY_2FA)

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = userPreferencesManager.hasAutomatic2faTokenCopy

        override fun onCheckChanged(context: Context, enable: Boolean) {
            userPreferencesManager.hasAutomatic2faTokenCopy = enable
        }
    }

    private val keyDerivationItem = object : SettingItem {
        override val id = "key-derivation"
        override val header = miscHeader
        override val title = context.getString(R.string.settings_cryptography)
        override val description
            get() = readablePayload?.let { context.getString(R.string.settings_cryptography_description, it) }

        private val readablePayload
            get() = when (
                val marker =
                    sessionManager.session?.let { cryptographyRepository.getCryptographyMarker(it) }
            ) {
                CryptographyMarker.Kwc3 -> context.getString(R.string.settings_cryptography_description_compatibility)
                is CryptographyMarker.Flexible -> {
                    when (marker.keyDerivation) {
                        is CryptographyMarker.Flexible.KeyDerivation.Argon2d ->
                            context.getString(R.string.settings_cryptography_description_argon2)

                        is CryptographyMarker.Flexible.KeyDerivation.Pbkdf2 ->
                            context.getString(R.string.settings_cryptography_description_pbkdf2)

                        else -> null
                    }
                }

                else -> null
            }

        override fun isEnable() = false

        
        override fun isVisible() = readablePayload != null

        override fun onClick(context: Context) {
            
            val messageId =
                if (teamSpaceAccessorProvider.get()?.cryptoForcedPayload.isNullOrEmpty()) {
                    R.string.settings_cryptography_dialog_description_b2c
                } else {
                    R.string.settings_cryptography_dialog_description_b2b
                }

            dialogHelper
                .builder(context)
                .setTitle(R.string.settings_cryptography_dialog_title)
                .setMessage(messageId)
                .setPositiveButton(R.string.ok, null)
                .show()
        }
    }

    private val changeMasterPasswordSettingItem = object : SettingItem {

        override val id = "change-master-password"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_change_master_password_title)
        override val description = context.getString(R.string.setting_change_master_password_description)
        override fun isEnable() = true
        override fun isVisible(): Boolean = masterPasswordFeatureAccessChecker.canAccessFeature()

        override fun onClick(context: Context) {
            if (userPreferencesManager.devicesCount > 1) {
                context.startActivity(
                    ChangeMPWarningDesktopActivity.newIntent(
                        context,
                        ChangeMasterPasswordOrigin.Settings
                    )
                )
            } else {
                sensibleSettingsClickHelper.perform(
                    context = context,
                    origin = UnlockEvent.Reason.WithCode.Origin.CHANGE_MASTER_PASSWORD,
                    forceMasterPassword = true
                ) {
                    context.startActivity(
                        ChangeMasterPasswordActivity.newIntent(
                            context,
                            ChangeMasterPasswordOrigin.Settings
                        )
                    )
                }
            }
        }
    }

    private val privacyAndDataSettingItem = object : SettingItem {

        override val id = "privacy-data-setting"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_privacy_title)
        override val description = context.getString(R.string.setting_privacy_message)
        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) {
            coroutineScope.launch {
                SettingPrivacySetting(
                    context = context,
                    subscriptionCodeRepository = subscriptionCodeRepository,
                    ioDispatcher = ioDispatcher,
                    toaster = toaster
                ).open()
            }
        }
    }

    private val manageDevicesItem = object : SettingItem {

        override val id = "manage-devices"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_manage_devices_title)
        override val description = context.getString(R.string.setting_manage_devices_description)
        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) = navigator.goToManageDevicesFromSettings()
    }

    fun getAll() = listOf(
        allowScreenshotItem,
        clearClipboardItem,
        copy2FAClipboardItem,
        changeMasterPasswordSettingItem,
        keyDerivationItem,
        privacyAndDataSettingItem,
        manageDevicesItem
    )
}