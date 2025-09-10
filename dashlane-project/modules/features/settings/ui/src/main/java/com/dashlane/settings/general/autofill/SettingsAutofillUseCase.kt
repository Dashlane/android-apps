package com.dashlane.settings.general.autofill

import android.content.Context
import android.os.Build
import com.dashlane.BuildVersion
import com.dashlane.autofillapi.ChromeAutofillDetector
import com.dashlane.autofillapi.ChromeAutofillStatus.CompatibilityAutofill
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.canUseAntiPhishing
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.inappautofill.AutofillPhishingLogger
import com.dashlane.inappautofill.InAppLoginManager
import com.dashlane.lock.LockManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.settings.Autofill
import com.dashlane.settings.Autofill.EnableAutofill
import com.dashlane.settings.Autofill.EnableChromeAutofill
import com.dashlane.settings.Autofill.KeyboardAutofill
import com.dashlane.settings.Autofill.PausedAutofill
import com.dashlane.settings.Autofill.PhishingAlert
import com.dashlane.settings.SettingsError.Generic
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.Error
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.checked
import com.dashlane.settings.enabled
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.Initial
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.RefreshSettings
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.UpdateAutofill
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.UpdateChromeAutofill
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.UpdateKeyboardAutofill
import com.dashlane.settings.general.autofill.SettingsAutofillState.View.UpdatePhishingAlert
import com.dashlane.settings.toggle
import com.dashlane.settings.ui.R
import com.dashlane.settings.updateSetting
import com.dashlane.settings.visible
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Suppress("kotlin:S3776", "kotlin:S107", "LongMethod")
fun loadAutofillSettings(
    @ApplicationContext context: Context,
    sessionManager: SessionManager,
    frozenStateManager: FrozenStateManager,
    inAppLoginManager: InAppLoginManager,
    preferencesManager: PreferencesManager,
    userFeaturesChecker: UserFeaturesChecker,
    chromeAutofillDetector: ChromeAutofillDetector,
    state: Initial,
): Flow<SettingsEvent<Autofill>> {
    return flow {

        val session = sessionManager.session ?: run {
            emit(Error(Generic))
            return@flow
        }

        val userPreferencesManager = preferencesManager[session.username]
        val autofillSettings = mutableListOf<SettingsItemData>()

        
        autofillSettings.add(
            SettingsItemData.Toggle(
                id = EnableAutofill,
                title = context.getString(R.string.setting_in_app_login_enabled_title),
                description = context.getString(R.string.setting_in_app_login_enabled_description),
                enabled = !frozenStateManager.isAccountFrozen,
                checked = inAppLoginManager.isEnableForApp(),
            ),
        )

        val chromeAutofillStatus = chromeAutofillDetector.getChromeAutofillStatus(context.contentResolver, context.packageManager)
        if (chromeAutofillStatus !is CompatibilityAutofill) {
            
            autofillSettings.add(
                SettingsItemData.Toggle(
                    id = EnableChromeAutofill,
                    title = context.getString(R.string.setting_in_app_login_chrome_enabled_title),
                    description = context.getString(R.string.setting_in_app_login_chrome_enabled_description),
                    enabled = inAppLoginManager.isEnableForApp(),
                    checked = inAppLoginManager.isEnableForApp() &&
                        !chromeAutofillDetector.isThirdPartyAutofillDeactivated(chromeAutofillStatus),
                ),
            )
        }

        
        autofillSettings.add(
            SettingsItemData.Click(
                id = PausedAutofill,
                title = context.getString(R.string.autofill_pause_resume_settings_title),
                description = context.getString(R.string.autofill_pause_resume_settings_description),
                enabled = !frozenStateManager.isAccountFrozen,
            ),
        )

        
        if (BuildVersion.getSdkInt() >= Build.VERSION_CODES.R) {
            autofillSettings.add(
                SettingsItemData.Toggle(
                    id = KeyboardAutofill,
                    title = context.getString(R.string.inline_autofill_settings_title_2),
                    description = context.getString(R.string.inline_autofill_settings_description),
                    enabled = inAppLoginManager.isEnableForApp(),
                    checked = userPreferencesManager.hasInlineAutofill,
                ),
            )
        }

        
        autofillSettings.add(
            SettingsItemData.Toggle(
                id = PhishingAlert,
                title = context.getString(R.string.settings_anti_phishing),
                description = context.getString(R.string.settings_anti_phishing_description),
                enabled = inAppLoginManager.isEnableForApp(),
                checked = userPreferencesManager.isAntiPhishingEnable,
                visible = userFeaturesChecker.canUseAntiPhishing() && userPreferencesManager.hasInlineAutofill,
            ),
        )
        emit(SettingsLoaded(autofillSettings))
    }
}

fun refreshAutofill(
    context: Context,
    inAppLoginManager: InAppLoginManager,
    chromeAutofillDetector: ChromeAutofillDetector,
    state: RefreshSettings,
): Flow<SettingsEvent<Autofill>> {
    return flow {
        val autofillEnabled = inAppLoginManager.isEnableForApp()
        val newSettings = state.data.autofillSettings.toMutableList()
        newSettings
            .find { it.id == EnableAutofill }
            ?.let { setting ->
                if (SettingsItemData.toggle.checked.getOrNull(setting) != autofillEnabled) {
                    val index = newSettings.indexOf(setting)
                    newSettings[index] = SettingsItemData.toggle.checked.modify(setting) { autofillEnabled }
                    newSettings
                        .updateSetting(KeyboardAutofill) { SettingsItemData.enabled.modify(it) { autofillEnabled } }
                        .updateSetting(PhishingAlert) { SettingsItemData.enabled.modify(it) { autofillEnabled } }
                        .updateSetting(EnableChromeAutofill) { SettingsItemData.enabled.modify(it) { autofillEnabled } }
                }
            }
        val chromeAutofillEnabled = inAppLoginManager.isEnableForApp() &&
            !chromeAutofillDetector.isThirdPartyAutofillDeactivated(context.contentResolver, context.packageManager)
        newSettings
            .find { it.id == EnableChromeAutofill }
            ?.let { setting ->
                if (SettingsItemData.toggle.checked.getOrNull(setting) != chromeAutofillEnabled) {
                    val index = newSettings.indexOf(setting)
                    newSettings[index] = SettingsItemData.toggle.checked.modify(setting) { chromeAutofillEnabled }
                }
            }
        emit(SettingsLoaded(newSettings.toList()))
    }
}

fun updateAutofill(
    frozenStateManager: FrozenStateManager,
    lockManager: LockManager,
    state: UpdateAutofill,
): Flow<SettingsEvent<Autofill>> {
    return flow {
        if (frozenStateManager.isAccountFrozen) {
            emit(SettingsEvent.GoTo(SettingsAutofillState.SideEffect.GoToPaywall))
            return@flow
        }
        if (state.checked) {
            emit(SettingsEvent.GoTo(SettingsAutofillState.SideEffect.GoToInAppLogin))
        } else {
            lockManager.startAutoLockGracePeriod()
            emit(SettingsEvent.GoTo(SettingsAutofillState.SideEffect.GoToAutofillSettings))
        }
    }
}

fun updateChromeAutofill(
    context: Context,
    chromeAutofillDetector: ChromeAutofillDetector,
    frozenStateManager: FrozenStateManager,
    lockManager: LockManager,
    state: UpdateChromeAutofill,
): Flow<SettingsEvent<Autofill>> {
    return flow {
        if (frozenStateManager.isAccountFrozen) {
            emit(SettingsEvent.GoTo(SettingsAutofillState.SideEffect.GoToPaywall))
            return@flow
        }
        if (state.checked) {
            emit(SettingsEvent.GoTo(SettingsAutofillState.SideEffect.GoToChromeInAppLogin))
        } else {
            lockManager.startAutoLockGracePeriod()
            val packageName = chromeAutofillDetector.getChromeAutofillStatus(context.contentResolver, context.packageManager).packageName
            emit(SettingsEvent.GoTo(SettingsAutofillState.SideEffect.GoToChromeAutofillSettings(packageName)))
        }
    }
}

fun updateKeyboardAutofill(
    sessionManager: SessionManager,
    preferencesManager: PreferencesManager,
    state: UpdateKeyboardAutofill,
): Flow<SettingsEvent<Autofill>> {
    return flow {
        val session = sessionManager.session ?: run {
            emit(Error(Generic))
            return@flow
        }
        preferencesManager[session.username].hasInlineAutofill = state.checked
        val newSettings = state.data.autofillSettings.toMutableList()
        newSettings
            .updateSetting(KeyboardAutofill, state.checked)
            .find { it.id == PhishingAlert }
            ?.let { setting ->
                val index = newSettings.indexOf(setting)
                newSettings[index] = SettingsItemData.visible.modify(setting) { state.checked }
            }
        emit(SettingsLoaded(newSettings.toList()))
    }
}

fun updatePhishingAlert(
    sessionManager: SessionManager,
    preferencesManager: PreferencesManager,
    autofillPhishingLogger: AutofillPhishingLogger,
    state: UpdatePhishingAlert,
): Flow<SettingsEvent<Autofill>> {
    return flow {
        val session = sessionManager.session ?: run {
            emit(Error(Generic))
            return@flow
        }

        preferencesManager[session.username].isAntiPhishingEnable = state.checked
        autofillPhishingLogger.logSettingChanged(state.checked)
        val newSettings = state.data.autofillSettings.toMutableList().updateSetting(PhishingAlert, state.checked).toList()
        emit(SettingsLoaded(newSettings))
    }
}