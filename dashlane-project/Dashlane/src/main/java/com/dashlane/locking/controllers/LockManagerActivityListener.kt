package com.dashlane.locking.controllers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.login.LoginActivity
import com.dashlane.navigation.NavigationConstants
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.ScreenOverLockProtectionView
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CHECK_INTERVAL_MS = 500L

class LockManagerActivityListener @Inject constructor(
    @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    private val lockManager: LockManager
) :
    AbstractActivityLifecycleListener() {

    private var inactivityCheckJob: Job? = null
    private var lastActivityResumed: Activity? = null

    private val lastResumeActivityRequireUnlock: Boolean
        get() = (lastActivityResumed as? DashlaneActivity)?.requireUserUnlock ?: false

    private val openLockScreenHandler = Handler(Looper.myLooper()!!)
    private var openLockScreenPendingRunnable: Runnable? = null

    init {
        applicationCoroutineScope.launch {
            lockManager.lockEventFlow.collect { lockEvent ->
                when (lockEvent) {
                    is LockEvent.Lock -> {
                        if (lastResumeActivityRequireUnlock) {
                            lockManager.showLockActivity(lastActivityResumed!!)
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onFirstActivityStarted() {
        super.onFirstActivityStarted()
        inactivityCheckJob = flow {
            delay(CHECK_INTERVAL_MS)
            while (true) {
                emit(Unit)
                delay(CHECK_INTERVAL_MS)
            }
        }
            .map { lockManager.checkForInactivityLock() }
            .launchIn(applicationCoroutineScope)
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
        inactivityCheckJob?.cancel()
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
}