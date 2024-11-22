package com.dashlane.ui.screens.settings.list.security

import android.content.Context
import com.dashlane.R
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepository
import com.dashlane.changemasterpassword.ChangeMasterPasswordOrigin
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.lock.LockEvent
import com.dashlane.masterpassword.ChangeMasterPasswordComposeActivity
import com.dashlane.masterpassword.ChangeMasterPasswordFeatureAccessChecker
import com.dashlane.navigation.Navigator
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.storage.userdata.RichIconsSettingProvider
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.screens.settings.SettingPrivacySetting
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingCheckable
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.util.DialogHelper
import com.dashlane.user.UserAccountInfo
import com.dashlane.usercryptography.UserCryptographyRepository
import com.dashlane.util.Toaster
import com.dashlane.util.getBaseActivity
import com.dashlane.util.inject.OptionalProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SettingsSecurityMiscList(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    navigator: Navigator,
    screenshotPolicy: ScreenshotPolicy,
    preferencesManager: PreferencesManager,
    teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    sessionManager: SessionManager,
    dialogHelper: DialogHelper,
    cryptographyRepository: UserCryptographyRepository,
    sensibleSettingsClickHelper: SensibleSettingsClickHelper,
    masterPasswordFeatureAccessChecker: ChangeMasterPasswordFeatureAccessChecker,
    toaster: Toaster,
    userAccountStorage: UserAccountStorage,
    subscriptionCodeRepository: SubscriptionCodeRepository,
    userDataRepository: UserDataRepository,
    richIconsSettingProvider: RichIconsSettingProvider
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
            preferencesManager[sessionManager.session?.username].getBoolean(ConstantsPrefs.CLEAR_CLIPBOARD_ON_TIMEOUT)

        override fun onCheckChanged(context: Context, enable: Boolean) {
            preferencesManager[sessionManager.session?.username].putBoolean(ConstantsPrefs.CLEAR_CLIPBOARD_ON_TIMEOUT, enable)
        }
    }

    private val copy2FAClipboardItem = object : SettingItem, SettingCheckable {
        override val id = "copy-2FA-clipboard"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_automatically_copy_2fa)
        override val description = context.getString(R.string.setting_automatically_copy_2fa_description)
        override fun isEnable() = true
        override fun isVisible() = true

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = preferencesManager[sessionManager.session?.username].hasAutomatic2faTokenCopy

        override fun onCheckChanged(context: Context, enable: Boolean) {
            preferencesManager[sessionManager.session?.username].hasAutomatic2faTokenCopy = enable
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
            sensibleSettingsClickHelper.perform(
                context = context,
                origin = LockEvent.Unlock.Reason.WithCode.Origin.CHANGE_MASTER_PASSWORD,
                forceMasterPassword = true
            ) {
                context.startActivity(
                    ChangeMasterPasswordComposeActivity.newIntent(
                        context = context,
                        origin = ChangeMasterPasswordOrigin.Settings,
                        showWarningDesktop = preferencesManager[sessionManager.session?.username].devicesCount > 1
                    )
                )
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

    private val showRichIconsItem = object : SettingItem, SettingCheckable {
        override val id = "show-rich-icons"
        override val header = miscHeader
        override val title = context.getString(R.string.setting_show_rich_icons_title)
        override val description = context.getString(R.string.setting_show_rich_icons_description)
        override fun isEnable() = richIconsSettingProvider.editable
        override fun isVisible() = true
        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context): Boolean = richIconsSettingProvider.richIcons
        override fun onCheckChanged(context: Context, enable: Boolean) {
            if (!isEnable()) {
                dialogHelper
                    .builder(context)
                    .setTitle(R.string.setting_rich_icons_force_title)
                    .setMessage(R.string.setting_rich_icons_force_description)
                    .setPositiveButton(R.string.ok, null)
                    .show()
            } else {
                sessionManager.session?.let {
                    val settingsManager = userDataRepository.getSettingsManager(it)
                    settingsManager.updateSettings(settingsManager.getSettings().copy { richIcons = enable })
                }
            }
        }
    }

    fun getAll() = listOf(
        allowScreenshotItem,
        clearClipboardItem,
        copy2FAClipboardItem,
        changeMasterPasswordSettingItem,
        keyDerivationItem,
        privacyAndDataSettingItem,
        manageDevicesItem,
        showRichIconsItem
    )
}