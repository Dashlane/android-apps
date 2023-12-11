package com.dashlane.help

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import com.dashlane.R
import com.dashlane.util.applyAppTheme
import com.dashlane.util.fallbackCustomTab

fun HelpCenterLink.newIntent(
    context: Context
): Intent {
    return CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setExitAnimations(context, -1, R.anim.fadeout_fragment)
        .applyAppTheme()
        .build()
        .intent
        .setData(uri)
        .fallbackCustomTab(context.packageManager)
}
