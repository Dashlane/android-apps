package com.dashlane.settings.security.misc

import android.content.Context
import androidx.core.net.toUri
import com.dashlane.Legal
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepository
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.settings.Misc
import com.dashlane.settings.Misc.ChangeMasterPassword
import com.dashlane.settings.Misc.ClearClipboard
import com.dashlane.settings.Misc.Copy2FA
import com.dashlane.settings.Misc.KeyDerivation
import com.dashlane.settings.Misc.ManageDevices
import com.dashlane.settings.Misc.PrivacyAndData
import com.dashlane.settings.Misc.RichIcons
import com.dashlane.settings.Misc.Screenshots
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.ChangeMasterPasswordLoaded
import com.dashlane.settings.SettingsEvent.Error
import com.dashlane.settings.SettingsEvent.KeyDerivationLoaded
import com.dashlane.settings.SettingsEvent.PrivacyLoaded
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.SettingsItemData.Click
import com.dashlane.settings.SettingsItemData.Toggle
import com.dashlane.settings.security.misc.SettingsMiscState.View.Initial
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdateChangeMasterPassword
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdateClearClipboard
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdateCopy2Fa
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdateKeyDerivation
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdatePrivacy
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdateRichIcons
import com.dashlane.settings.security.misc.SettingsMiscState.View.UpdateScreenshotAllowed
import com.dashlane.settings.ui.R
import com.dashlane.settings.updateSetting
import com.dashlane.storage.userdata.RichIconsSettingProvider
import com.dashlane.teamspaces.manager.getCryptoForcedPayload
import com.dashlane.teamspaces.manager.getCurrentTeam
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.user.UserAccountInfo
import com.dashlane.usercryptography.UserCryptographyRepository
import com.dashlane.util.isNotSemanticallyNull
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take

@Suppress("kotlin:S107", "LongMethod")
fun loadMiscSecuritySettings(
    @ApplicationContext context: Context,
    sessionManager: SessionManager,
    userAccountStorage: UserAccountStorage,
    accountStatusRepository: AccountStatusRepository,
    screenshotPolicy: ScreenshotPolicy,
    preferencesManager: PreferencesManager,
    userDatabaseRepository: UserDatabaseRepository,
    cryptographyRepository: UserCryptographyRepository,
    richIconsSettingProvider: RichIconsSettingProvider,
    state: Initial,
): Flow<SettingsEvent<Misc>> {
    return flow {

        val securitySettings = mutableListOf<SettingsItemData>()

        val session = sessionManager.session ?: run {
            emit(Error(SettingsError.Generic))
            return@flow
        }
        val userAccountInfo = userAccountStorage[session.username] ?: run {
            emit(Error(SettingsError.Generic))
            return@flow
        }
        val accountStatus = accountStatusRepository[session]
        val currentTeam = accountStatus?.getCurrentTeam()

        
        if (userAccountInfo.accountType is UserAccountInfo.AccountType.MasterPassword) {
            securitySettings.add(
                Toggle(
                    id = Screenshots,
                    title = context.getString(R.string.settings_security_flag_title),
                    description = context.getString(R.string.settings_security_flag_description),
                    enabled = true,
                    checked = screenshotPolicy.areScreenshotAllowed(),
                ),
            )
        }

        
        securitySettings.add(
            Toggle(
                id = ClearClipboard,
                title = context.getString(R.string.settings_clear_clipboard),
                description = context.getString(R.string.settings_clear_clipboard_description),
                enabled = true,
                checked = preferencesManager[session.username].getBoolean(ConstantsPrefs.CLEAR_CLIPBOARD_ON_TIMEOUT),
            ),
        )

        
        securitySettings.add(
            Toggle(
                id = Copy2FA,
                title = context.getString(R.string.setting_automatically_copy_2fa),
                description = context.getString(R.string.setting_automatically_copy_2fa_description),
                enabled = true,
                checked = preferencesManager[session.username].hasAutomatic2faTokenCopy,
            ),
        )

        
        if (userAccountInfo.accountType is UserAccountInfo.AccountType.MasterPassword &&
            userDatabaseRepository.isRacletteDatabaseAccessible(session) &&
            currentTeam?.space?.teamMembership?.isSSOUser != true
        ) {
            securitySettings.add(
                Click(
                    id = ChangeMasterPassword,
                    title = context.getString(R.string.setting_change_master_password_title),
                    description = context.getString(R.string.setting_change_master_password_description),
                    enabled = true,
                ),
            )
        }

        
        val description = when (val marker = cryptographyRepository.getCryptographyMarker(session)) {
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
        if (description != null) {
            securitySettings.add(
                Click(
                    id = KeyDerivation,
                    title = context.getString(R.string.settings_cryptography),
                    description = description,
                    enabled = false,
                ),
            )
        }

        
        securitySettings.add(
            Click(
                id = PrivacyAndData,
                title = context.getString(R.string.setting_privacy_title),
                description = context.getString(R.string.setting_privacy_message),
                enabled = true,
            ),
        )

        
        securitySettings.add(
            Click(
                id = ManageDevices,
                title = context.getString(R.string.setting_manage_devices_title),
                description = context.getString(R.string.setting_manage_devices_description),
                enabled = true,
            ),
        )

        
        securitySettings.add(
            Toggle(
                id = RichIcons,
                title = context.getString(R.string.setting_show_rich_icons_title),
                description = context.getString(R.string.setting_show_rich_icons_description),
                enabled = richIconsSettingProvider.editable,
                checked = richIconsSettingProvider.richIcons,
            ),
        )
        emit(SettingsLoaded(securitySettings))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun updateScreenshotAllowed(
    lockManager: LockManager,
    screenshotPolicy: ScreenshotPolicy,
    state: UpdateScreenshotAllowed,
): Flow<SettingsEvent<Misc>> {
    return lockManager.lockEventFlow
        .take(1)
        .flatMapMerge { lockEvent ->
            flow {
                if (lockEvent !is LockEvent.Unlock) {
                    emit(SettingsLoaded(state.data.miscSettings))
                    return@flow
                }
                screenshotPolicy.setScreenshotAllowed(state.checked)
                val newSettings = state.data.miscSettings.toMutableList().updateSetting(Screenshots, state.checked).toList()
                emit(SettingsLoaded(newSettings))
            }
        }
}

fun updateClearClipboard(
    sessionManager: SessionManager,
    preferencesManager: PreferencesManager,
    state: UpdateClearClipboard,
): Flow<SettingsEvent<Misc>> {
    return flow {
        preferencesManager[sessionManager.session?.username].putBoolean(ConstantsPrefs.CLEAR_CLIPBOARD_ON_TIMEOUT, state.checked)
        val newSettings = state.data.miscSettings.toMutableList().updateSetting(ClearClipboard, state.checked).toList()
        emit(SettingsLoaded(newSettings))
    }
}

fun updateCopy2Fa(
    sessionManager: SessionManager,
    preferencesManager: PreferencesManager,
    state: UpdateCopy2Fa,
): Flow<SettingsEvent<Misc>> {
    return flow {
        preferencesManager[sessionManager.session?.username].hasAutomatic2faTokenCopy = state.checked
        val newSettings = state.data.miscSettings.toMutableList().updateSetting(Copy2FA, state.checked).toList()
        emit(SettingsLoaded(newSettings))
    }
}

fun updateChangeMasterPassword(
    lockManager: LockManager,
    sessionManager: SessionManager,
    preferencesManager: PreferencesManager,
    state: UpdateChangeMasterPassword,
): Flow<SettingsEvent<Misc>> {
    return lockManager.lockEventFlow
        .take(1)
        .mapNotNull { lockEvent ->
            if (lockEvent !is LockEvent.Unlock) {
                return@mapNotNull SettingsLoaded(state.data.miscSettings)
            }
            val showWarningDesktop = preferencesManager[sessionManager.session?.username].devicesCount > 1
            ChangeMasterPasswordLoaded(showWarningDesktop)
        }
}

fun updateKeyDerivation(
    sessionManager: SessionManager,
    accountStatusRepository: AccountStatusRepository,
    state: UpdateKeyDerivation,
): Flow<SettingsEvent<Misc>> {
    return flow {
        val session = sessionManager.session ?: run {
            emit(Error(SettingsError.Generic))
            return@flow
        }
        val hasCryptoForcedPayload = accountStatusRepository[session]?.getCryptoForcedPayload().isNotSemanticallyNull()
        emit(KeyDerivationLoaded(hasCryptoForcedPayload))
    }
}

fun updatePrivacy(
    subscriptionCodeRepository: SubscriptionCodeRepository,
    state: UpdatePrivacy,
): Flow<SettingsEvent<Misc>> {
    return flow {
        val subscriptionCode = subscriptionCodeRepository.get()
        val uri = "${Legal.URL_PRIVACY_POLICY_SETTINGS}$subscriptionCode".toUri()
        emit(PrivacyLoaded(uri))
    }
}

fun updateRichIcons(
    sessionManager: SessionManager,
    userDataRepository: UserDataRepository,
    state: UpdateRichIcons,
): Flow<SettingsEvent<Misc>> {
    return flow {
        sessionManager.session?.let {
            val settingsManager = userDataRepository.getSettingsManager(it)
            settingsManager.updateSettings(settingsManager.getSettings().copy { richIcons = state.checked })
        }
        val newSettings = state.data.miscSettings.toMutableList().updateSetting(RichIcons, state.checked).toList()
        emit(SettingsLoaded(newSettings))
    }
}