package com.dashlane.autofill.api.monitorlog



interface MonitorAutofillDeviceInfoService {
    

    fun getDeviceManufacturer(): String

    

    fun getDeviceModel(): String

    

    fun isDefault(monitoredApp: MonitoredApp): Boolean

    

    fun matchAutofillConfiguration(monitoredBrowser: MonitoredApp.Browser): Boolean?

    

    fun getVersionName(monitoredApp: MonitoredApp): String?

    

    fun isAutofillByApiEnabled(): Boolean

    

    fun isAutofillByAccessibilityEnabled(): Boolean

    

    fun isAutofillByKeyboardEnabled(): Boolean
}