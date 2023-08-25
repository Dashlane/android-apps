package com.dashlane.help

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import com.dashlane.R
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.util.applyAppTheme
import com.dashlane.util.fallbackCustomTab
import com.dashlane.util.usagelogs.ViewLogger

fun HelpCenterLink.newIntent(
    context: Context,
    viewLogger: ViewLogger,
    sendLogs: Boolean = true
): Intent {
    if (sendLogs) {
        val usageLogRepository = UserActivityComponent(context).currentSessionUsageLogRepository
        when (this) {
            HelpCenterLink.Base -> {
                viewLogger.log(UsageLogConstant.HomePageSubtype.help) 
                usageLogRepository?.enqueue(UsageLogCode75(type = "helpCenter", action = "getStarted"))
            }
            is HelpCenterLink.Article -> {
                usageLogRepository?.enqueue(
                    UsageLogCode75(
                        type = "helpCenter",
                        action = "havingTrouble",
                        subaction = id
                    )
                )
            }
        }
    }

    return CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setExitAnimations(context, -1, R.anim.fadeout_fragment)
        .applyAppTheme()
        .build()
        .intent
        .setData(uri)
        .fallbackCustomTab(context.packageManager)
}
