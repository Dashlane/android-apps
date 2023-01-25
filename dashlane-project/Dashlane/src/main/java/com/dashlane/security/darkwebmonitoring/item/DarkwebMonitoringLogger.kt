package com.dashlane.security.darkwebmonitoring.item

import com.dashlane.darkweb.DarkWebEmailStatus

interface DarkwebMonitoringLogger {
    
    

    fun onInactiveDarkwebModuleShow()

    

    fun onInactiveClickRegisterEmail()

    
    

    fun onClickRemoveEmail(item: DarkWebEmailStatus)

    companion object {
        const val DARK_WEB_MODULE_ORIGIN = "dark_web_monitoring"
    }
}