package com.dashlane.async.broadcasts

import android.content.Context
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.install.InstallLogCode53



class AppboyNotificationLogger(private val installLogRepository: InstallLogRepository) {

    constructor(context: Context) : this(UserActivityComponent(context).installLogRepository)

    fun logInstallLog53(trackingKey: String?, action: String?, deepLink: String?) {
        installLogRepository.enqueue(
            InstallLogCode53(
                type = "appBoyNotification",
                trackingKey = trackingKey,
                action = action,
                link = deepLink
            )
        )
    }
}