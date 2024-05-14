package com.dashlane.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.dashlane.accountrecoverykey.enforce.AccountRecoveryKeyEnforcer
import com.dashlane.announcements.AnnouncementsActivityLifecycle
import com.dashlane.applinkfetcher.AuthentifiantAppLinkDownloader
import com.dashlane.authenticator.IsSettingUp2faChecker
import com.dashlane.breach.BreachManagerActivityListener
import com.dashlane.collections.sharing.CollectionSharingResultActivityListener
import com.dashlane.hermes.LogFlush
import com.dashlane.hermes.LogFlushLifecycleObserver
import com.dashlane.limitations.DeviceLimitActivityListener
import com.dashlane.limitations.Enforce2faLimiter
import com.dashlane.locking.controllers.LockManagerActivityListener
import com.dashlane.locking.controllers.LockSelfCheckActivityListener
import com.dashlane.login.controller.NumberOfRunsActivityListener
import com.dashlane.login.controller.PasswordResetActivityListener
import com.dashlane.login.controller.TokenReceiverActivityListener
import com.dashlane.navigation.Navigator
import com.dashlane.notification.LocalNotificationCenterActivityListener
import com.dashlane.notification.badge.NotificationBadgeActivityListener
import com.dashlane.securearchive.BackupCoordinatorImpl
import com.dashlane.security.HideOverlayWindowActivityListener
import com.dashlane.security.TouchFilterActivityListener
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.update.AppUpdateNeededActivityListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalActivityLifecycleListener @Inject constructor(
    private val sessionManager: SessionManager,
    announcementsActivityLifecycle: AnnouncementsActivityLifecycle,
    lockManagerActivityListener: LockManagerActivityListener,
    applicationForegroundChecker: ApplicationForegroundChecker,
    backupIntentCoordinator: BackupCoordinatorImpl,
    tokenReceiverActivityListener: TokenReceiverActivityListener,
    numberOfRunsActivityListener: NumberOfRunsActivityListener,
    authentifiantAppLinkDownloader: AuthentifiantAppLinkDownloader,
    passwordResetActivityListener: PasswordResetActivityListener,
    notificationBadgeActivityListener: NotificationBadgeActivityListener,
    localNotificationCenterActivityListener: LocalNotificationCenterActivityListener,
    breachManagerActivityListener: BreachManagerActivityListener,
    appUpdateNeededActivityListener: AppUpdateNeededActivityListener,
    lockSelfCheckActivityListener: LockSelfCheckActivityListener,
    deviceLimitActivityListener: DeviceLimitActivityListener,
    touchFilterActivityListener: TouchFilterActivityListener,
    hideOverlayWindowActivityListener: HideOverlayWindowActivityListener,
    collectionSharingResultActivityListener: CollectionSharingResultActivityListener,
    logFlush: LogFlush,
    navigator: Navigator,
    enforce2faLimiter: Enforce2faLimiter,
    accountRecoveryKeyEnforcer: AccountRecoveryKeyEnforcer,
    isSettingUp2faChecker: IsSettingUp2faChecker
) : ActivityLifecycleListener {

    private val activityLifecycleListeners = mutableListOf<ActivityLifecycleListener>()

    private val allListeners: Array<ActivityLifecycleListener>
        get() = synchronized(activityLifecycleListeners) {
            activityLifecycleListeners.toTypedArray()
        }

    private var loggedIn = false

    init {
        
        ProcessLifecycleOwner.get().lifecycle.apply {
            addObserver(ApplicationProcessLifecycleObserver(this@GlobalActivityLifecycleListener))
            addObserver(LogFlushLifecycleObserver(logFlush))
        }

        
        register(hideOverlayWindowActivityListener)
        register(touchFilterActivityListener)
        register(announcementsActivityLifecycle)
        register(lockManagerActivityListener)
        register(ActivityLifecycleLoggerListener())
        register(applicationForegroundChecker)
        register(backupIntentCoordinator.activityLifecycleListener)
        register(tokenReceiverActivityListener)
        register(numberOfRunsActivityListener)
        register(authentifiantAppLinkDownloader)
        register(passwordResetActivityListener)
        register(notificationBadgeActivityListener)
        register(localNotificationCenterActivityListener)
        register(breachManagerActivityListener)
        register(appUpdateNeededActivityListener)
        register(lockSelfCheckActivityListener)
        register(navigator as ActivityLifecycleListener)
        register(deviceLimitActivityListener)
        register(enforce2faLimiter)
        register(isSettingUp2faChecker.activityLifecycleListener)
        register(collectionSharingResultActivityListener)
        register(accountRecoveryKeyEnforcer)
    }

    override fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun unregister(application: Application) {
        application.unregisterActivityLifecycleCallbacks(this)
    }

    fun register(callback: ActivityLifecycleListener) {
        synchronized(activityLifecycleListeners) {
            activityLifecycleListeners.add(callback)
        }
    }

    fun unregister(callback: ActivityLifecycleListener) {
        synchronized(activityLifecycleListeners) {
            activityLifecycleListeners.remove(callback)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        allListeners.forEach { it.onActivityCreated(activity, savedInstanceState) }
        if (sessionManager.session == null) {
            loggedIn = false
        } else if (!loggedIn) {
            loggedIn = true
            allListeners.forEach { it.onFirstLoggedInActivityCreated(activity, savedInstanceState) }
        }
    }

    override fun onActivityStarted(activity: Activity) {
        allListeners.forEach { it.onActivityStarted(activity) }
    }

    override fun onActivityResult(activity: DashlaneActivity, requestCode: Int, resultCode: Int, data: Intent?) {
        allListeners.forEach { it.onActivityResult(activity, requestCode, resultCode, data) }
    }

    override fun onActivityResumed(activity: Activity) {
        allListeners.forEach { it.onActivityResumed(activity) }
    }

    override fun onActivityUserInteraction(activity: DashlaneActivity) {
        allListeners.forEach { it.onActivityUserInteraction(activity) }
    }

    override fun onActivityPaused(activity: Activity) {
        allListeners.forEach { it.onActivityPaused(activity) }
    }

    override fun onActivityStopped(activity: Activity) {
        allListeners.forEach { it.onActivityStopped(activity) }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        allListeners.forEach { it.onActivitySaveInstanceState(activity, outState) }
    }

    override fun onActivityDestroyed(activity: Activity) {
        allListeners.forEach { it.onActivityDestroyed(activity) }
    }

    override fun onFirstActivityCreated() {
        allListeners.forEach { it.onFirstActivityCreated() }
    }

    override fun onFirstActivityStarted() {
        allListeners.forEach { it.onFirstActivityStarted() }
    }

    override fun onFirstActivityResumed() {
        allListeners.forEach { it.onFirstActivityResumed() }
    }

    override fun onLastActivityPaused() {
        allListeners.forEach { it.onLastActivityPaused() }
    }

    override fun onLastActivityStopped() {
        allListeners.forEach { it.onLastActivityStopped() }
    }

    override fun onFirstLoggedInActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        allListeners.forEach { it.onFirstLoggedInActivityCreated(activity, savedInstanceState) }
    }

    private class ApplicationProcessLifecycleObserver(
        private val globalActivityLifecycleListener: GlobalActivityLifecycleListener
    ) : LifecycleEventObserver {

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_CREATE -> globalActivityLifecycleListener.onFirstActivityCreated()
                Lifecycle.Event.ON_START -> globalActivityLifecycleListener.onFirstActivityStarted()
                Lifecycle.Event.ON_RESUME -> globalActivityLifecycleListener.onFirstActivityResumed()
                Lifecycle.Event.ON_PAUSE -> globalActivityLifecycleListener.onLastActivityPaused()
                Lifecycle.Event.ON_STOP -> globalActivityLifecycleListener.onLastActivityStopped()
                else -> {
                    
                }
            }
        }
    }
}
