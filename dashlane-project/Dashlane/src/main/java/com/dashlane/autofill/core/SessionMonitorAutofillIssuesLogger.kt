package com.dashlane.autofill.core

import com.dashlane.autofill.api.monitorlog.AutofillStakeholdersInfo
import com.dashlane.autofill.api.monitorlog.MonitorAutofillIssuesLogger
import com.dashlane.autofill.api.monitorlog.MonitoredApp
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode142
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject



class SessionMonitorAutofillIssuesLogger @Inject constructor(
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : MonitorAutofillIssuesLogger, AutofillLegacyLogger(sessionManager, bySessionUsageLogRepository) {

    override fun logAutofillDeviceInfo(autofillStakeholdersInfo: AutofillStakeholdersInfo) {
        log(
            autofillStakeholdersInfo.toUsageLog142()
        )
    }

    private fun AutofillStakeholdersInfo.toUsageLog142(): UsageLogCode142 {
        return UsageLogCode142(
            autofillEnabled = this.isAutofillByApiEnabled,
            settingAutofillAccessibilityOn = this.isAutofillByAccessibilityEnabled,
            settingAutofillKeyboardOn = this.isAutofillInKeyboardEnabled,
            deviceManufacturer = this.manufacturer,
            deviceModel = this.model,
            browserName = this.browser?.first?.toBrowserName(),
            browserVersion = this.browser?.second,
            wellDefinedBrowserCompatSetting = this.browserMatchAutofillConfiguration,
            keyboardName = this.keyboard?.first?.toKeyboardName(),
            keyboardVersion = this.keyboard?.second
        )
    }

    private fun MonitoredApp.Browser.toBrowserName(): UsageLogCode142.BrowserName {
        return when (this) {
            MonitoredApp.Browser.CHROME -> UsageLogCode142.BrowserName.CHROME
            MonitoredApp.Browser.SAMSUNG_BROWSER -> UsageLogCode142.BrowserName.SAMSUNG
        }
    }

    private fun MonitoredApp.Keyboard.toKeyboardName(): UsageLogCode142.KeyboardName {
        return when (this) {
            MonitoredApp.Keyboard.GBOARD -> UsageLogCode142.KeyboardName.GBOARD
            MonitoredApp.Keyboard.HONEYBOARD -> UsageLogCode142.KeyboardName.HONEYBOARD
        }
    }
}