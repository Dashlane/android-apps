package com.dashlane.teamspaces.manager

import com.dashlane.useractivity.log.usage.UsageLogCode33
import com.dashlane.useractivity.log.usage.UsageLogConstant



object TeamspaceRestrictionLogHelper {

    @JvmStatic
    fun getLog(usageLogType: String, action: String) = UsageLogCode33(
        sender = UsageLogConstant.LoginOrigin.FROM_MOBILE,
        type = usageLogType,
        action = action
    )
}