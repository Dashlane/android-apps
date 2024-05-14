package com.dashlane.ui.screens.settings.list.general

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.dashlane.R
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.login.lock.LockManager
import com.dashlane.navigation.Navigator
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.autofill.onboarding.OnboardingType
import com.dashlane.ui.screens.settings.item.SettingCheckable
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem

class SettingsGeneralAutoLoginList(
    private val context: Context,
    private val lockManager: LockManager,
    inAppLoginManager: InAppLoginManager,
    navigator: Navigator,
    userPreferencesManager: UserPreferencesManager
) {

    private val autoLoginHeader =
        SettingHeader(context.getString(R.string.setting_in_app_login_category))

    private val autoLoginAppItem = object : SettingItem, SettingCheckable {

        override val id = "autologin-app"
        override val header = autoLoginHeader
        override val title = context.getString(R.string.setting_in_app_login_enabled_title)
        override val description = context.getString(R.string.setting_in_app_login_enabled_description)
        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = inAppLoginManager.isEnableForApp()

        override fun onCheckChanged(context: Context, enable: Boolean) {
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

    private val autoLoginWebsiteItem = object : SettingItem, SettingCheckable {
        override val id = "autologin-website"
        override val header = autoLoginHeader
        override val title = context.getString(R.string.setting_in_app_login_browser_enabled_title)
        override val description = context.getString(R.string.setting_in_app_login_browser_enabled_description)
        override fun isEnable() = true
        override fun isVisible() = true

        override fun onClick(context: Context) = onCheckChanged(context, !isChecked(context))
        override fun isChecked(context: Context) = inAppLoginManager.isEnableForChrome()

        override fun onCheckChanged(context: Context, enable: Boolean) {
            if (enable) {
                navigator.goToInAppLogin(OnboardingType.ACCESSIBILITY)
            } else {
                lockManager.startAutoLockGracePeriod()
                inAppLoginManager.inAppLoginByAccessibilityManager.startActivityToChooseProvider(context)
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

    private val autoLoginInlineItem = object : SettingItem, SettingCheckable {
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
        }
    }

    fun getAll(): List<SettingItem> {
        val accessibilityItem = if (isAccessibilityAvailable()) autoLoginWebsiteItem else null
        return listOfNotNull(autoLoginAppItem, accessibilityItem, pausedAndLinkedItem, autoLoginInlineItem)
    }

    private fun isAccessibilityAvailable() = context.resources.getBoolean(R.bool.is_accessibility_supported)
}