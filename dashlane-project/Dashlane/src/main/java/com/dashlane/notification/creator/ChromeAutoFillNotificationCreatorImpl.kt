package com.dashlane.notification.creator

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.dashlane.autofillapi.ChromeAutofillDetector
import com.dashlane.debug.services.DaDaDaNotification
import com.dashlane.inappautofill.InAppLoginManager
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.CHROME_NOTIFICATION_ID
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.DISMISSAL_THRESHOLD
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.TAG_USER_CHROME
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChromeAutoFillNotificationCreatorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    private val inAppLoginManager: InAppLoginManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val debugNotification: DaDaDaNotification,
    private val packageManager: PackageManager,
    private val contentResolver: ContentResolver,
    private val chromeAutofillDetector: ChromeAutofillDetector,
) : ChromeAutofillNotificationCreator {
    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    override fun cancelAutofillNotificationWorkers() {
        TAG_ARRAY.forEach { tag -> workManager.cancelAllWorkByTag(tag) }
        NotificationManagerCompat.from(context).cancel(CHROME_NOTIFICATION_ID)
    }

    override fun createForNewUser() {
        
    }

    override fun createForExistingUser() {
        applicationCoroutineScope.launch {
            if (!isEligibleForNotification()) {
                cancelAutofillNotificationWorkers()
                return@launch
            }
            if (debugNotification.triggerNotificationsImmediately) cancelAutofillNotificationWorkers()
            workManager.createPeriodicIfNonExist<ChromeAutoFillNotificationWorker>(
                tag = TAG_USER_CHROME,
                duration = getDuration(),
                initialDelay = getInitialDelay(),
            )
        }
    }

    private fun isEligibleForNotification(): Boolean {
        val isNotificationEligible = !inAppLoginManager.hasAutofillApiDisabled()
        val isChromeThirdPartyAutofillDisabled = chromeAutofillDetector.isThirdPartyAutofillDeactivated(contentResolver, packageManager)
        val isThresholdNotReached = globalPreferencesManager.getAutofillChromeNotificationDismissCount() < DISMISSAL_THRESHOLD
        return isNotificationEligible && isChromeThirdPartyAutofillDisabled && isThresholdNotReached
    }

    private fun getInitialDelay() =
        getWorkManagerDuration(
            durationBySeconds = if (debugNotification.triggerNotificationsImmediately) 0L else DURATION_SECONDS_INITIAL_DELAY,
        )

    private fun getDuration() = getWorkManagerDuration(durationByDay = DURATION_DAY, hourOfDay = 19)

    companion object {
        private val TAG_ARRAY = arrayOf(TAG_USER_CHROME)
        private const val DURATION_SECONDS_INITIAL_DELAY = 30L
        private const val DURATION_DAY = 2
    }
}