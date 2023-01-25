package com.dashlane.lock

import android.annotation.SuppressLint
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.dashlane.ui.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.getThemeAttrColor



@SuppressLint("ViewConstructor")
class ScreenOverLockProtectionView(
    activity: DashlaneActivity,
    private val lockWatcher: LockWatcher
) : View(activity) {

    companion object {
        fun showOrHide(activity: DashlaneActivity, lockWatcher: LockWatcher) {
            val decorView = activity.window.decorView as ViewGroup

            
            val overlay = decorView.children.filterIsInstance<ScreenOverLockProtectionView>().firstOrNull()

            if (!activity.requireUserUnlock || !activity.applicationLocked) {
                
                overlay?.removeFromParent()
            } else if (overlay == null) {
                
                val screenOverLockProtectionView = ScreenOverLockProtectionView(activity, lockWatcher)
                lockWatcher.register(object : LockWatcher.Listener {
                    override fun onLock() {
                        
                    }

                    override fun onUnlockEvent(unlockEvent: UnlockEvent) {
                        
                        screenOverLockProtectionView.removeFromParent()
                        lockWatcher.unregister(this)
                    }
                })
                decorView.addView(screenOverLockProtectionView)
            }
        }
    }

    init {
        setBackgroundColor(
            ContextThemeWrapper(context, R.style.Theme_Dashlane_LockedOut)
                .getThemeAttrColor(android.R.attr.colorBackground)
        )
        isClickable = true
    }

    private fun removeFromParent() {
        (parent as? ViewGroup)?.removeView(this)
    }
}