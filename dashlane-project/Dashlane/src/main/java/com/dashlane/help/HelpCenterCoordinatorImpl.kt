package com.dashlane.help

import android.content.Context
import android.content.Intent
import com.dashlane.util.safelyStartBrowserActivity
import com.dashlane.util.usagelogs.ViewLogger
import javax.inject.Inject

class HelpCenterCoordinatorImpl @Inject constructor(private val viewLogger: ViewLogger) : HelpCenterCoordinator {
    override fun openLink(
        context: Context,
        helpCenterLink: HelpCenterLink,
        sendLogs: Boolean
    ) {
        val intent: Intent = helpCenterLink.newIntent(context, viewLogger, sendLogs)
        context.safelyStartBrowserActivity(intent)
    }
}
