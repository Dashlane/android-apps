package com.dashlane.ui.screens.settings.list.help

import android.content.Context
import com.dashlane.Legal
import com.dashlane.R
import com.dashlane.crashreport.CrashReporter
import com.dashlane.help.HelpCenterLink
import com.dashlane.help.newIntent
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.navigation.Navigator
import com.dashlane.ui.screens.settings.LicensesActivity
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.screens.settings.item.SettingScreenItem
import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.util.launchUrl
import com.dashlane.util.safelyStartBrowserActivity
import com.dashlane.util.startActivity
import java.util.Locale

@Suppress("UseDataClass")
class RootSettingsHelpList(
    context: Context,
    navigator: Navigator,
    rootHeader: SettingHeader,
    crashReporter: CrashReporter,
    clipboardCopy: ClipboardCopy
) {
    private val helpHeader =
        SettingHeader(context.getString(R.string.settings_category_help))

    private val troubleShootingItem = object : SettingItem {
        override val id = "troubleshooting"
        override val header: SettingHeader = helpHeader
        override val title = context.getString(R.string.settings_help_troubleshooting)
        override val description = context.getString(R.string.settings_help_troubleshooting_description)
        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) {
            val intent = HelpCenterLink.Base.newIntent(
                context = context
            )
            context.safelyStartBrowserActivity(intent)
        }
    }

    private val userSupportReporterIdItem = object : SettingItem {
        override val id = "user-support-reporter-id"
        override val header: SettingHeader = helpHeader
        override val title = context.getString(R.string.setting_about_crash_report_id)
        override val description = crashReporter.crashReporterId
            .takeIf { it.length > 8 }
            ?.run { substring(0, 8).uppercase(Locale.US) }
            ?: "-"

        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) {
            clipboardCopy.copyToClipboard(data = description, sensitiveData = false, autoClear = false)
        }
    }

    private val privacyPolicyItem = object : SettingItem {
        override val id = "privacy"
        override val header: SettingHeader = helpHeader
        override val title = context.getString(R.string.settings_privacy_policy)
        override val description = ""
        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) {
            context.launchUrl(Legal.URL_PRIVACY_POLICY)
        }
    }

    private val termsOfServiceItem = object : SettingItem {
        override val id = "tos"
        override val header: SettingHeader = helpHeader
        override val title = context.getString(R.string.settings_tos)
        override val description = ""
        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) {
            context.launchUrl(Legal.URL_TERMS_OF_SERVICE)
        }
    }

    private val licencesItem = object : SettingItem {
        override val id = "licence"
        override val header: SettingHeader = helpHeader
        override val title = context.getString(R.string.settings_title_licenses)
        override val description = ""
        override fun isEnable() = true
        override fun isVisible() = true
        override fun onClick(context: Context) = context.startActivity<LicensesActivity>()
    }

    val root = SettingScreenItem(
        navigator,
        AnyPage.SETTINGS,
        object : SettingItem {
            override val id = "help"
            override val header = rootHeader
            override val title = context.getString(R.string.settings_category_help)
            override val description = context.getString(R.string.settings_category_help_description)
            override fun isEnable() = true
            override fun isVisible() = true
            override fun onClick(context: Context) = Unit
        },
        listOf(
            troubleShootingItem,
            userSupportReporterIdItem,
            privacyPolicyItem,
            termsOfServiceItem,
            licencesItem
        )
    )
}