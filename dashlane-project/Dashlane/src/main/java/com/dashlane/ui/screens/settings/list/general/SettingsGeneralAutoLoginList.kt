package com.dashlane.ui.screens.settings.list.general

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.dashlane.R
import com.dashlane.autofill.phishing.AutofillPhishingLogger
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.canUseAntiPhishing
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.lock.LockManager
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.ui.screens.settings.item.SettingChange
import com.dashlane.ui.screens.settings.item.SettingCheckable
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem

class SettingsGeneralAutoLoginList(
    private val context: Context,
    private val lockManager: LockManager,
    private val frozenStateManager: FrozenStateManager,
    inAppLoginManager: InAppLoginManager,
    navigator: Navigator,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    userFeaturesChecker: UserFeaturesChecker,
    autofillPhishingLogger: AutofillPhishingLogger,
) {

    private val isAutofillEnabled = !frozenStateManager.isAccountFrozen

    private val userPreferencesManager: UserPreferencesManager
        get() = preferencesManager[sessionManager.session?.username]

    private val autoLoginHeader =
        SettingHeader(context.getString(R.string.setting_in_app_login_category))

    private val autoLoginAppItem = object : SettingItem, SettingCheckable {

        override val id = "autologin-app"
        override val header = autoLoginHeader
        override val title = context.getString(R.string.setting_in_app_login_enabled_title)
        override val description = if (frozenStateManager.isAccountFrozen) {
            context.getString(R.string.setting_in_app_login_frozen_state_description)
        } else {
            context.getString(R.string.setting_in_app_login_enabled_description)
        }

        override fun isEnable() = isAutofillEnabled
        override fun isVisible() = true
        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = inAppLoginManager.isEnableForApp()

        override fun onCheckChanged(context: Context, enable: Boolean) {
            if (frozenStateManager.isAccountFrozen) {
                navigator.goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
                return
            }

            if (enable) {
                navigator.goToInAppLogin()
            } else {
                lockManager.startAutoLockGracePeriod()
                if (!inAppLoginManager.startActivityToDisableProvider(context)) {
                    
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    context.startActivity(intent)
                }
            }
        }
    }

    private val pausedAndLinkedItem = object : SettingItem {
        override val id = "autologin-paused-and-linked"
        override val header = autoLoginHeader
        override val title = context.getString(R.string.autofill_pause_resume_settings_title)
        override val description = context.getString(R.string.autofill_pause_resume_settings_description)

        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) =
            navigator.goToAutofillPauseAndLinkedFromSettings()
    }

    private val autoLoginInlineItem = object : SettingItem, SettingCheckable, SettingChange.Listenable {
        override var listener: SettingChange.Listener? = null
        override val id = "inline-autologin"
        override val header = autoLoginHeader
        override val title = context.getString(R.string.inline_autofill_settings_title_2)
        override val description = context.getString(R.string.inline_autofill_settings_description)

        override fun isEnable() = autoLoginAppItem.isChecked(context)
        override fun isVisible() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = userPreferencesManager.hasInlineAutofill
        override fun onCheckChanged(context: Context, enable: Boolean) {
            if (!autoLoginAppItem.isChecked(context)) return
            userPreferencesManager.hasInlineAutofill = enable
            listener?.onSettingsInvalidate()
        }
    }

    private val antiPhishing = object : SettingItem, SettingCheckable {
        override val id = "antiphishing"
        override val header = autoLoginHeader
        override val title = context.getString(R.string.settings_anti_phishing)
        override val description = context.getString(R.string.settings_anti_phishing_description)

        override fun isEnable() = autoLoginAppItem.isChecked(context)
        override fun isVisible() = userFeaturesChecker.canUseAntiPhishing() && autoLoginInlineItem.isChecked(context)

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = userPreferencesManager.isAntiPhishingEnable
        override fun onCheckChanged(context: Context, enable: Boolean) {
            if (!autoLoginAppItem.isChecked(context)) return
            userPreferencesManager.isAntiPhishingEnable = enable
            autofillPhishingLogger.logSettingChanged(enable)
        }
    }

    fun getAll(): List<SettingItem> {
        return listOfNotNull(
            autoLoginAppItem,
            pausedAndLinkedItem,
            autoLoginInlineItem,
            antiPhishing
        )
    }
}