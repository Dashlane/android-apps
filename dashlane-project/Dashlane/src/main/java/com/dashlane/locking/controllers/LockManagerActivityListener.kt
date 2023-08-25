package com.dashlane.locking.controllers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.dashlane.lock.LockWatcher
import com.dashlane.lock.ScreenOverLockProtectionView
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.LoginActivity
import com.dashlane.login.lock.LockManager
import com.dashlane.navigation.NavigationConstants
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogConstant
import javax.inject.Inject

class LockManagerActivityListener @Inject constructor(
    private val lockManager: LockManager,
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) :
    AbstractActivityLifecycleListener(), LockWatcher.Listener {

    private val inactivityCheck = InactivityCheck(lockManager)

    private var lastActivityResumed: Activity? = null

    private val lastResumeActivityRequireUnlock: Boolean
        get() = (lastActivityResumed as? DashlaneActivity)?.requireUserUnlock ?: false

    private val openLockScreenHandler = Handler(Looper.myLooper()!!)
    private var openLockScreenPendingRunnable: Runnable? = null

    init {
        lockManager.register(this)
    }

    override fun onFirstActivityStarted() {
        super.onFirstActivityStarted()
        inactivityCheck.start()
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        
        
        if (activityUnlockFailed(activity)) {
            
            activity.finish()
            return
        }
        lastActivityResumed = activity
        if (lastResumeActivityRequireUnlock) {
            lockManager.stopAutoLockGracePeriod()
            if (lockManager.isLocked) {
                scheduleOpenLockActivity(activity)
            } else {
                lockManager.setLastActionTimestampToNow()
            }
        }
        (activity as? DashlaneActivity)?.let { ScreenOverLockProtectionView.showOrHide(it, lockManager) }
    }

    override fun onFirstLoggedInActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onFirstLoggedInActivityCreated(activity, savedInstanceState)
        
        if (savedInstanceState != null) return
        
        val extras = activity.intent.extras ?: return
        val shouldUnlock = extras.size() <= 1 && extras.containsKey(NavigationConstants.STARTED_WITH_INTENT)
        if (shouldUnlock) return
        
        val sessionRestored =
            extras.getBoolean(NavigationConstants.FORCED_LOCK_SESSION_RESTORED, false) ||
                    extras.getBoolean(NavigationConstants.SESSION_RESTORED_FROM_BOOT, false)
        if (!sessionRestored) return
        
        lockManager.lockWithoutEvents()

        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode35(
                    type = UsageLogConstant.ViewType.Labs,
                    action = UsageLogConstant.LabsAction.keep_session_alive_reinitialize.toString()
                )
            )
    }

    private fun activityUnlockFailed(activity: Activity): Boolean =
        (activity as? DashlaneActivity)?.requireUserUnlock == true &&
                activity.localClassName != lastActivityResumed?.localClassName &&
                lockManager.isLocked && lastActivityResumed is LoginActivity && lastActivityResumed!!.isFinishing

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        cancelScheduledLockActivity()
        (activity as? DashlaneActivity)?.let { ScreenOverLockProtectionView.showOrHide(it, lockManager) }
    }

    override fun onActivityResult(activity: DashlaneActivity, requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(activity, requestCode, resultCode, data)
        lockManager.stopAutoLockGracePeriod()
    }

    override fun onActivityUserInteraction(activity: DashlaneActivity) {
        super.onActivityUserInteraction(activity)
        lockManager.setLastActionTimestampToNow()
    }

    override fun onLastActivityStopped() {
        lastActivityResumed = null
        lockManager.onAppInBackground()
        inactivityCheck.stop()
    }

    override fun onLock() {
        if (lastResumeActivityRequireUnlock) {
            lockManager.showLockActivity(lastActivityResumed!!)
        }
    }

    override fun onUnlockEvent(unlockEvent: UnlockEvent) {
        
    }

    private fun scheduleOpenLockActivity(activity: Activity) {
        openLockScreenPendingRunnable = Runnable {
            lockManager.showLockActivity(activity)
            openLockScreenPendingRunnable = null
        }.apply {
            
            
            
            openLockScreenHandler.post(this)
        }
    }

    private fun cancelScheduledLockActivity() {
        openLockScreenPendingRunnable?.let {
            
            openLockScreenHandler.removeCallbacks(it)
            openLockScreenPendingRunnable = null
        }
    }

    private class InactivityCheck(private val lockManager: LockManager) : Runnable {

        private val handler = Handler(Looper.myLooper()!!)
        private var running = false

        override fun run() {
            if (!running) {
                return
            }
            lockManager.checkForInactivityLock()
            handler.postDelayed(this, CHECK_INTERVAL_MS)
        }

        fun start() {
            if (running) {
                return
            }
            running = true
            run()
        }

        fun stop() {
            running = false
            handler.removeCallbacksAndMessages(this)
        }

        companion object {

            private const val CHECK_INTERVAL_MS = 500L
        }
    }
}