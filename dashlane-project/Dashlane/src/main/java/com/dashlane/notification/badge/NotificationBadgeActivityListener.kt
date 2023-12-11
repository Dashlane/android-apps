package com.dashlane.notification.badge

import android.app.Activity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.menu.MenuViewModel
import javax.inject.Inject

class NotificationBadgeActivityListener @Inject constructor(private val actor: NotificationBadgeActor) :
    AbstractActivityLifecycleListener() {

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        if (activity is DashlaneActivity) {
            actor.subscribe(
                activity.lifecycleScope,
                object : NotificationBadgeListener {
                    override fun onNotificationBadgeUpdated() {
                        
                        if (activity.findViewById<ComposeView?>(R.id.menu_frame) !is ComposeView) return
                        activity.actionBarUtil.drawerArrowDrawable?.isEnabled = actor.hasUnread
                        ViewModelProvider(activity)[MenuViewModel::class.java].refresh()
                    }
                }
            )
            actor.refresh()
        }
    }
}