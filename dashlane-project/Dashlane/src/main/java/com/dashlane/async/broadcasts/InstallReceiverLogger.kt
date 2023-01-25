package com.dashlane.async.broadcasts

import android.content.Context
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.install.InstallLogCode17

class InstallReceiverLogger(private val installLogRepository: InstallLogRepository) {

    constructor(context: Context) : this(UserActivityComponent(context).installLogRepository)

    fun logInstallLog17() {
        installLogRepository.enqueue(InstallLogCode17(subStep = "9.1"), true)
    }
}