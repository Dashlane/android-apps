package com.dashlane.update

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import com.dashlane.R
import com.dashlane.events.AppEvents
import com.dashlane.events.clearLastEvent
import com.dashlane.events.register
import com.dashlane.events.unregister
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.SnackbarUtils
import com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED
import java.lang.ref.WeakReference
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class AppUpdateNeededActivityListener @Inject constructor(
    private val sessionManager: SessionManager,
    private val appEvents: AppEvents,
    private val appUpdateInstaller: AppUpdateInstaller,
    private val usageLogRepository: BySessionRepository<UsageLogRepository>,
    private val userPreferencesManager: UserPreferencesManager
) : AbstractActivityLifecycleListener() {

    private lateinit var activity: WeakReference<DashlaneActivity>

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

    override fun onActivityResult(activity: DashlaneActivity, requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(activity, requestCode, resultCode, data)
        if (requestCode == AppUpdateInstaller.INSTALL_UPDATE_REQUEST_CODE) {
            onInstallResult(resultCode)
        }
    }

    fun onInstallResult(resultCode: Int) {
        when (resultCode) {
            
            RESULT_OK -> log("installSuccess")
            RESULT_CANCELED -> log("installCanceled")
            RESULT_IN_APP_UPDATE_FAILED -> log("installError")
            else -> {
                
            }
        }
    }

    private fun showUpdateNeeded() {
        val homeActivity = activity.get() ?: return
        
        
        if (homeActivity.applicationLocked || shouldNotShowAgain()) return
        val updateCode = appUpdateInstaller.availableUpdate?.availableVersionCode() ?: return
        userPreferencesManager.lastShownAvailableUpdateDate = Instant.now()
        log("display", updateCode)
        appEvents.clearLastEvent<AppUpdateNeededEvent>()

        SnackbarUtils
            .showSnackbar(homeActivity, homeActivity.getString(R.string.app_update_needed), 10_000)
            .setAction(R.string.app_update_needed_button) {
                log("installing", updateCode)
                appUpdateInstaller.installUpdate(homeActivity)
            }
    }

    private fun shouldNotShowAgain(): Boolean {
        val nextReminderDate = userPreferencesManager.lastShownAvailableUpdateDate.plus(Duration.ofDays(30))
        return Instant.now().isBefore(nextReminderDate)
    }

    private fun log(action: String, versionCode: Int? = null) =
        sessionManager.session?.let {
            usageLogRepository[it]?.enqueue(
                UsageLogCode75(
                    action = action,
                    subaction = versionCode.toString(),
                    type = "updateNeededSnackbar",
                    originStr = "home"
                )
            )
        }
}