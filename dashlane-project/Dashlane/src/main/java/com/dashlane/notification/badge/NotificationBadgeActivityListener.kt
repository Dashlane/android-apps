package com.dashlane.notification.badge

import android.app.Activity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.menu.MenuViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationBadgeActivityListener @Inject constructor(private val actor: NotificationBadgeFlow) :
    AbstractActivityLifecycleListener() {
        override fun onActivityResumed(activity: Activity) {
            super.onActivityResumed(activity)
            if (activity is DashlaneActivity) {
                activity.lifecycleScope.launch {
                    actor.resultFlow
                        .flowWithLifecycle(activity.lifecycle, Lifecycle.State.STARTED)
                        .collect { result ->
                            
                            if (activity.findViewById<ComposeView?>(R.id.menu_frame) !is ComposeView) return@collect
                            activity.actionBarUtil.drawerArrowDrawable?.isEnabled = result.hasSharing || result.hasUnReadActionItems
                            ViewModelProvider(activity)[MenuViewModel::class.java].refresh()
                        }
                    actor.refresh()
                }
            }
        }
    }