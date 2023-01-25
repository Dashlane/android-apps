package com.dashlane.ui

import android.content.Context
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.usage.UsageLogCode96



object InAppLoginWindowLogger {

    @JvmStatic
    fun log(
        context: Context,
        action: UsageLogCode96.Action,
        packageName: String?,
        hasCredentials: Boolean
    ) {
        UserActivityComponent(context).currentSessionUsageLogRepository
            ?.enqueue(
                UsageLogCode96(
                    action = action,
                    app = packageName,
                    sender = UsageLogCode96.Sender.DASHLANE,
                    hasCredentials = hasCredentials
                )
            )
    }
}