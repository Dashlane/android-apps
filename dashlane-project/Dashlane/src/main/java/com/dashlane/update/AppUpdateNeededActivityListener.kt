package com.dashlane.update

import android.app.Activity
import com.dashlane.R
import com.dashlane.events.AppEvents
import com.dashlane.events.clearLastEvent
import com.dashlane.events.register
import com.dashlane.events.unregister
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.util.SnackbarUtils
import java.lang.ref.WeakReference
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class AppUpdateNeededActivityListener @Inject constructor(
    private val appEvents: AppEvents,
    private val appUpdateInstaller: AppUpdateInstaller,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager
) : AbstractActivityLifecycleListener() {

    private lateinit var activity: WeakReference<DashlaneActivity>

    private val userPreferencesManager: UserPreferencesManager
        get() = preferencesManager[sessionManager.session?.username]

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        if (activity !is HomeActivity) return
        this.activity = WeakReference(activity)
        appEvents.register<AppUpdateNeededEvent>(this, true) { showUpdateNeeded() }
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        if (activity !is HomeActivity) return
        this.activity.clear()
        appEvents.unregister<AppUpdateNeededEvent>(this)
    }

    private fun showUpdateNeeded() {
        val homeActivity = activity.get() ?: return
        
        
        if (homeActivity.applicationLocked || shouldNotShowAgain()) return
        userPreferencesManager.lastShownAvailableUpdateDate = Instant.now()
        appEvents.clearLastEvent<AppUpdateNeededEvent>()

        SnackbarUtils
            .showSnackbar(homeActivity, homeActivity.getString(R.string.app_update_needed), 10_000)
            .setAction(R.string.app_update_needed_button) {
                appUpdateInstaller.installUpdate(homeActivity)
            }
    }

    private fun shouldNotShowAgain(): Boolean {
        val nextReminderDate = userPreferencesManager.lastShownAvailableUpdateDate.plus(Duration.ofDays(30))
        return Instant.now().isBefore(nextReminderDate)
    }
}