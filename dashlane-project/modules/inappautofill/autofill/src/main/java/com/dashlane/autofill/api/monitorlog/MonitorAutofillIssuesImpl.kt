package com.dashlane.autofill.api.monitorlog

import javax.inject.Inject



class MonitorAutofillIssuesImpl @Inject constructor(
    private val monitorAutofillDeviceInfoService: MonitorAutofillDeviceInfoService
) : MonitorAutofillIssues {

    override fun collectAutofillStakeholdersInfo(): AutofillStakeholdersInfo {
        val monitorKeyboards = MonitoredApp.Keyboard.values().getVersionNames()
        val keyboardInfo = monitorKeyboards.firstOrNull { (monitoredKeyboard, _) ->
            monitorAutofillDeviceInfoService.isDefault(monitoredKeyboard)
        } ?: monitorKeyboards.firstOrNull()

        val monitorBrowsers = MonitoredApp.Browser.values().getVersionNames()
        val browserInfo = monitorBrowsers.firstOrNull { (monitoredBrowser, _) ->
            monitorAutofillDeviceInfoService.isDefault(monitoredBrowser)
        } ?: monitorBrowsers.firstOrNull()

        val matchAutofillConfiguration = browserInfo?.first?.let {
            monitorAutofillDeviceInfoService.matchAutofillConfiguration(it)
        }

        val isAutofillByAccessibilityEnabled = monitorAutofillDeviceInfoService.isAutofillByAccessibilityEnabled()
        val isAutofillByApiEnabled = monitorAutofillDeviceInfoService.isAutofillByApiEnabled()
        val isAutofillInKeyboardEnabled = monitorAutofillDeviceInfoService.isAutofillByKeyboardEnabled()
        val manufacturer = monitorAutofillDeviceInfoService.getDeviceManufacturer()
        val model = monitorAutofillDeviceInfoService.getDeviceModel()

        return AutofillStakeholdersInfo(
            isAutofillByApiEnabled = isAutofillByApiEnabled,
            isAutofillByAccessibilityEnabled = isAutofillByAccessibilityEnabled,
            isAutofillInKeyboardEnabled = isAutofillInKeyboardEnabled,
            manufacturer = manufacturer,
            model = model,
            keyboard = keyboardInfo,
            browser = browserInfo,
            browserMatchAutofillConfiguration = matchAutofillConfiguration
        )
    }

    private fun <T : MonitoredApp> Array<T>.getVersionNames(): List<Pair<T, String>> {
        return this.mapNotNull { monitoredApp: T ->
            monitorAutofillDeviceInfoService.getVersionName(monitoredApp)?.let {
                monitoredApp to it
            }
        }
    }
}