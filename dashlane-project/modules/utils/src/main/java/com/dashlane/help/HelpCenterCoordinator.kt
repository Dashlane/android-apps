package com.dashlane.help

import android.content.Context

interface HelpCenterCoordinator {
    fun openLink(
        context: Context,
        helpCenterLink: HelpCenterLink,
        sendLogs: Boolean
    )
}
