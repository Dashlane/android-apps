package com.dashlane.notification.badge

import android.app.Activity
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.drawable.BadgeDrawerArrowDrawable
import com.dashlane.ui.menu.DashlaneMenuView
import javax.inject.Inject



class NotificationBadgeActivityListener @Inject constructor(private val actor: NotificationBadgeActor) :
    AbstractActivityLifecycleListener() {

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        if (activity is DashlaneActivity) {
            actor.subscribe(activity.lifecycleScope, object : NotificationBadgeListener {
                override fun onNotificationBadgeUpdated() {
                    val menuView =
                        activity.findViewById(R.id.menu_frame) as? DashlaneMenuView ?: return
                    val drawable: BadgeDrawerArrowDrawable =
                        activity.actionBarUtil.drawerArrowDrawable ?: return
                    drawable.isEnabled = actor.hasUnread
                    menuView.refresh()
                }
            })
            actor.refresh()
        }
    }
}