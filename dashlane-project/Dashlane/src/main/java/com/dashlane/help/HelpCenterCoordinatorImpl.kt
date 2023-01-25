package com.dashlane.help

import android.content.Context
import android.content.Intent
import com.dashlane.util.safelyStartBrowserActivity
import javax.inject.Inject

class HelpCenterCoordinatorImpl @Inject constructor() : HelpCenterCoordinator {
    override fun openLink(
        context: Context,
        helpCenterLink: HelpCenterLink,
        sendLogs: Boolean
    ) {
        val intent: Intent = helpCenterLink.newIntent(context, sendLogs)
        context.safelyStartBrowserActivity(intent)
    }
}
