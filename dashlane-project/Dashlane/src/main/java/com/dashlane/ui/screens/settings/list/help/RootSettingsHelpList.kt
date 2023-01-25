package com.dashlane.ui.screens.settings.list.help

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dashlane.Legal
import com.dashlane.R
import com.dashlane.core.DataSync
import com.dashlane.crashreport.CrashReporter
import com.dashlane.help.HelpCenterLink
import com.dashlane.help.newIntent
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.navigation.Navigator
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.ui.screens.settings.LicensesActivity
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.screens.settings.item.SettingScreenItem
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.usersupportreporter.UserSupportFileUploader
import com.dashlane.util.clipboard.ClipboardUtils
import com.dashlane.util.getBaseActivity
import com.dashlane.util.launchUrl
import com.dashlane.util.safelyStartBrowserActivity
import com.dashlane.util.startActivity
import java.time.Duration
import java.time.Instant
import java.util.Locale



@Suppress("UseDataClass")
class RootSettingsHelpList(
    context: Context,
    navigator: Navigator,
    rootHeader: SettingHeader,
    crashReporter: CrashReporter,
    userSupportFileUploader: UserSupportFileUploader,
    dataSync: DataSync,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) {
    private val helpHeader =
        SettingHeader(context.getString(R.string.settings_category_help))

    private val troubleShootingItem = object : SettingItem {
        override val id = "troubleshooting"
        override val header: SettingHeader? = helpHeader
        override val title = context.getString(R.string.settings_help_troubleshooting)
        override val description = context.getString(R.string.settings_help_troubleshooting_description)
        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true
        override fun onClick(context: Context) {
            val intent = HelpCenterLink.Base.newIntent(context)
            context.safelyStartBrowserActivity(intent)
        }
    }

    private val userSupportReporterIdItem = object : SettingItem {

        private val clickCounterResetPeriod = Duration.ofSeconds(10)
        private var lastClickTime: Instant? = null
        private var clickCounter = 0

        override val id = "user-support-reporter-id"
        override val header: SettingHeader? = helpHeader
        override val title = context.getString(R.string.setting_about_crash_report_id)
        override val description = crashReporter.crashReporterId
            .takeIf { it.length > 8 }
            ?.run { substring(0, 8).uppercase(Locale.US) }
            ?: "-"

        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true
        override fun onClick(context: Context) {
            ClipboardUtils.copyToClipboard(description, sensitiveData = false, autoClear = false)

            

            val lastClickTime = this.lastClickTime
            val now = Instant.now()
            this.lastClickTime = now
            if (lastClickTime != null && Duration.between(lastClickTime, now) > clickCounterResetPeriod) {
                clickCounter = 0
            }
            clickCounter++
            if (clickCounter >= 5) {
                val activity = context.getBaseActivity() as? AppCompatActivity ?: return
                val file = dataSync.syncHelper.logsWriter.file ?: return
                userSupportFileUploader.startSyncLogsUpload(
                    context,
                    activity.lifecycleScope,
                    crashReporter.crashReporterId,
                    file
                )
                clickCounter = 0
            }
        }
    }

    private val privacyPolicyItem = object : SettingItem {
        override val id = "privacy"
        override val header: SettingHeader? = helpHeader
        override val title = context.getString(R.string.settings_privacy_policy)
        override val description = ""
        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true
        override fun onClick(context: Context) {
            bySessionUsageLogRepository[sessionManager.session]
                ?.enqueue(
                    UsageLogCode75(
                        type = "helpCenter",
                        action = "privacyPolicy"
                    )
                )
            context.launchUrl(Legal.URL_PRIVACY_POLICY)
        }
    }

    private val termsOfServiceItem = object : SettingItem {
        override val id = "tos"
        override val header: SettingHeader? = helpHeader
        override val title = context.getString(R.string.settings_tos)
        override val description = ""
        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true
        override fun onClick(context: Context) {
            bySessionUsageLogRepository[sessionManager.session]
                ?.enqueue(
                    UsageLogCode75(
                        type = "helpCenter",
                        action = "termsOfService"
                    )
                )
            context.launchUrl(Legal.URL_TERMS_OF_SERVICE)
        }
    }

    private val licencesItem = object : SettingItem {
        override val id = "licence"
        override val header: SettingHeader? = helpHeader
        override val title = context.getString(R.string.settings_title_licenses)
        override val description = ""
        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true
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
            override fun isEnable(context: Context) = true
            override fun isVisible(context: Context) = true
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