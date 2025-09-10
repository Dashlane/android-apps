package com.dashlane.notification.creator

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.dashlane.debug.services.DaDaDaNotification
import com.dashlane.inappautofill.InAppLoginManager
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.DISMISSAL_THRESHOLD
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.NOTIFICATION_ID
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.TAG_EXISTING_USER
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.TAG_NEW_USER
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AutoFillNotificationCreatorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    private val inAppLoginManager: InAppLoginManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val debugNotification: DaDaDaNotification,
) : AutoFillNotificationCreator {
    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    override fun cancelAutofillNotificationWorkers() {
        TAG_ARRAY.forEach { tag -> workManager.cancelAllWorkByTag(tag) }
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    override fun createForNewUser() {
        applicationCoroutineScope.launch {
            if (!isEligibleForNotification()) {
                cancelAutofillNotificationWorkers()
                return@launch
            }
            if (debugNotification.triggerNotificationsImmediately) cancelAutofillNotificationWorkers()
            
            TAG_ARRAY.filterNot { it == TAG_NEW_USER }.forEach { tag -> workManager.cancelAllWorkByTag(tag) }
            workManager.createPeriodicIfNonExist<AutoFillNotificationWorker>(
                TAG_NEW_USER,
                duration = getDurationNewUser(),
                initialDelay = getDurationInitialDelay(),
            )
        }
    }

    override fun createForExistingUser() {
        applicationCoroutineScope.launch {
            if (!isEligibleForNotification()) {
                cancelAutofillNotificationWorkers()
                return@launch
            }
            if (debugNotification.triggerNotificationsImmediately) cancelAutofillNotificationWorkers()
            
            val newUserNotificationScheduled = workManager.getWorkInfosByTag(TAG_NEW_USER).await().any { !it.state.isFinished }
            if (newUserNotificationScheduled) return@launch
            
            workManager.createPeriodicIfNonExist<AutoFillNotificationWorker>(
                tag = TAG_EXISTING_USER,
                duration = getDurationExistingUser(),
                initialDelay = getInitialDelayExistingUser(),
            )
        }
    }

    private fun isEligibleForNotification(): Boolean {
        val isNotificationEligible = inAppLoginManager.hasAutofillApiDisabled()
        val hasNotActivatedAutofillOnce = !globalPreferencesManager.hasActivatedAutofillOnce()
        val isThresholdNotReached = globalPreferencesManager.getAutofillNotificationDismissCount() < DISMISSAL_THRESHOLD
        return isNotificationEligible && hasNotActivatedAutofillOnce && isThresholdNotReached
    }

    private fun getDurationInitialDelay(): Long = if (!debugNotification.triggerNotificationsImmediately) {
        getWorkManagerDuration(DURATION_DAY_INITIAL_DELAY, 19)
    } else {
        getWorkManagerDuration(durationBySeconds = 0L)
    }

    private fun getDurationNewUser(): Long = getWorkManagerDuration(DURATION_DAY_NEW_USER, 19)

    private fun getInitialDelayExistingUser() = if (!debugNotification.triggerNotificationsImmediately) {
        getDurationExistingUser()
    } else {
        getWorkManagerDuration(durationBySeconds = 0L)
    }

    private fun getDurationExistingUser() = getWorkManagerDuration(DURATION_DAY_EXISTING_USER, 19)

    companion object {
        private val TAG_ARRAY = arrayOf(TAG_NEW_USER, TAG_EXISTING_USER)
        private const val DURATION_DAY_INITIAL_DELAY = 1
        private const val DURATION_DAY_NEW_USER = 2
        private const val DURATION_DAY_EXISTING_USER = 7
    }
}