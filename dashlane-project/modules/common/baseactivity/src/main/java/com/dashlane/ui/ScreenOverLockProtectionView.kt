package com.dashlane.ui

import android.annotation.SuppressLint
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockWatcher
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.getThemeAttrColor
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
class ScreenOverLockProtectionView(activity: DashlaneActivity) : View(activity) {

    companion object {
        fun showOrHide(activity: DashlaneActivity, lockWatcher: LockWatcher) {
            val decorView = activity.window.decorView as ViewGroup

            
            val overlay = decorView.children.filterIsInstance<ScreenOverLockProtectionView>().firstOrNull()

            if (!activity.requireUserUnlock || !activity.applicationLocked) {
                
                overlay?.removeFromParent()
            } else if (overlay == null) {
                
                val screenOverLockProtectionView = ScreenOverLockProtectionView(activity)

                activity.lifecycleScope.launch {
                    lockWatcher.lockEventFlow.take(1).collect { lockEvent ->
                        when (lockEvent) {
                            is LockEvent.Unlock -> screenOverLockProtectionView.removeFromParent()
                            else -> Unit
                        }
                    }
                }
                decorView.addView(screenOverLockProtectionView)
            }
        }
    }

    init {
        setBackgroundColor(
            ContextThemeWrapper(context, R.style.Theme_Dashlane_Transparent)
                .getThemeAttrColor(android.R.attr.colorBackground)
        )
        isClickable = true
    }

    private fun removeFromParent() {
        (parent as? ViewGroup)?.removeView(this)
    }
}