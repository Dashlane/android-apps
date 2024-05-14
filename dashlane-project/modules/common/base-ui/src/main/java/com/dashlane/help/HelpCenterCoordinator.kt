package com.dashlane.help

import android.content.Context
import android.content.Intent
import com.dashlane.util.safelyStartBrowserActivity

object HelpCenterCoordinator {
    fun openLink(
        context: Context,
        helpCenterLink: HelpCenterLink
    ) {
        val intent: Intent = helpCenterLink.newIntent(context = context)
        context.safelyStartBrowserActivity(intent)
    }
}
