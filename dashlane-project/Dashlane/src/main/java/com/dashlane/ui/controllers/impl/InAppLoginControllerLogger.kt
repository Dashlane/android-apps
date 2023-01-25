package com.dashlane.ui.controllers.impl

import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode96

class InAppLoginControllerLogger(private val usageLogRepository: UsageLogRepository?) {

    fun log(packageName: String?, action: UsageLogCode96.Action?, hasCredentials: Boolean) {
        usageLogRepository?.enqueue(
            UsageLogCode96(
                action = action,
                app = packageName,
                sender = UsageLogCode96.Sender.DASHLANE,
                hasCredentials = hasCredentials
            )
        )
    }
}