package com.dashlane.notification.creator

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dashlane.R
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.NavigationUriBuilder
import com.dashlane.notification.AutofillReceiver
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.security.DashlaneIntent
import com.dashlane.ui.activities.SplashScreenActivity
import com.dashlane.autofill.onboarding.OnboardingInAppLoginActivity
import com.dashlane.util.clearTask
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.notification.NotificationHelper
import com.dashlane.util.notification.buildNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AutoFillNotificationCreator @Inject constructor(
    @ApplicationContext
    context: Context,
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val inAppLoginManager: InAppLoginManager,
    private val globalPreferencesManager: GlobalPreferencesManager
) {
    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    fun cancelAutofillNotificationWorkers() {
        TAG_ARRAY.forEach { tag ->
            workManager.cancelAllWorkByTag(tag)
        }
    }

    fun createForNewUser() {
        if (!isEligibleForNotification()) return
        applicationCoroutineScope.launch {
            workManager.createPeriodicIfNonExist<AutoFillNotificationWorker>(
                TAG_NEW_USER,
                duration = getDurationNewUser(),
                initialDelay = getDurationInitialDelay()
            )
        }
    }

    fun createForExistingUser() {
        if (!isEligibleForNotification()) return
        applicationCoroutineScope.launch {
            
            
            if (workManager.getWorkInfosByTag(TAG_NEW_USER).await().isNotEmpty()) return@launch
            if (isActive()) {
                
                workManager.cancelAllWorkByTag(TAG_EXISTING_USER_INACTIVE)
                workManager.createPeriodicIfNonExist<AutoFillNotificationWorker>(
                    tag = TAG_EXISTING_USER_ACTIVE,
                    duration = getDurationExistingActiveUser(),
                    initialDelay = getInitialDelayExistingActiveUser()
                )
            } else {
                workManager.createPeriodicIfNonExist<AutoFillNotificationWorker>(
                    tag = TAG_EXISTING_USER_INACTIVE,
                    duration = getDurationExistingInactiveUser(),
                    initialDelay = getDurationInitialDelay()
                )
            }
        }
    }

    private suspend fun isActive(): Boolean {
        val newUser = workManager.getWorkInfosByTag(TAG_NEW_USER)
        val isInactiveUser = workManager.getWorkInfosByTag(TAG_EXISTING_USER_INACTIVE)
        return newUser.await().isEmpty() && isInactiveUser.await().isNotEmpty()
    }

    private fun isEligibleForNotification(): Boolean {
        val isNotificationEligible = inAppLoginManager.hasAutofillApiDisabled()
        val hasNotActivatedAutofillOnce = !globalPreferencesManager.hasActivatedAutofillOnce()
        val isThresholdNotReached = globalPreferencesManager.getAutofillNotificationDismissCount() < DISMISSAL_THRESHOLD
        return isNotificationEligible && hasNotActivatedAutofillOnce && isThresholdNotReached
    }

    private fun getDurationInitialDelay(): Long {
        return getWorkManagerDuration(DURATION_DAY_INITIAL_DELAY, 19)
    }

    private fun getDurationNewUser(): Long {
        return getWorkManagerDuration(DURATION_DAY_NEW_USER, 19)
    }

    private fun getDurationExistingInactiveUser() = getWorkManagerDuration(DURATION_DAY_EXISTING_INACTIVE_USER, 19)

    private fun getInitialDelayExistingActiveUser() = getDurationExistingActiveUser()

    private fun getDurationExistingActiveUser() = getWorkManagerDuration(DURATION_DAY_EXISTING_ACTIVE_USER, 19)

    @HiltWorker
    class AutoFillNotificationWorker @AssistedInject constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val inAppLoginManager: InAppLoginManager
    ) : Worker(context, params) {

        override fun doWork(): Result {
            if (!inAppLoginManager.hasAutofillApiDisabled()) return Result.success()

            val context = applicationContext

            val uri = NavigationUriBuilder()
                .host(NavigationHelper.Destination.MainPath.IN_APP_LOGIN)
                .origin(OnboardingInAppLoginActivity.REMINDER_NOTIFICATION)
                .build()

            val notificationIntent = DashlaneIntent.newInstance(context, SplashScreenActivity::class.java).apply {
                clearTask()
                action = Intent.ACTION_VIEW
                data = uri
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val positiveAction = NotificationCompat.Action(
                R.drawable.ic_action_item_auto_fill,
                context.getString(
                    R.string.notification_autofill_action_positive
                ),
                pendingIntent
            )

            val notNowIntent = PendingIntent.getBroadcast(
                context,
                1,
                Intent(context, AutofillReceiver::class.java).apply {
                    putExtra(AutofillReceiver.NOTIFICATION_NOT_NOW_EXTRA, true)
                },
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val negativeAction = NotificationCompat.Action(
                R.drawable.close_cross,
                context.getString(
                    R.string.notification_autofill_action
                ),
                notNowIntent
            )
            val notification = buildNotification(context) {
                setIconDashlane()
                setLocalOnly()
                setContentTitle(context.getString(R.string.notification_autofill_title))
                setContentText(context.getString(R.string.notification_autofill_description), true)
                setChannel(NotificationHelper.Channel.MARKETING)
                setContentIntent(pendingIntent)
                setAutoCancel()
                addAction(positiveAction)
                addAction(negativeAction)
            }
            try {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                
            }
            return Result.success()
        }
    }

    companion object {
        const val NOTIFICATION_ID = 0x08
        const val TAG_NEW_USER = "tag_new_user"
        const val TAG_EXISTING_USER_ACTIVE = "tag_existing_user_active"
        const val TAG_EXISTING_USER_INACTIVE = "tag_existing_inactive"
        const val DISMISSAL_THRESHOLD = 2
        private val TAG_ARRAY = arrayOf(TAG_NEW_USER, TAG_EXISTING_USER_ACTIVE, TAG_EXISTING_USER_INACTIVE)
        private const val DURATION_DAY_INITIAL_DELAY = 1
        private const val DURATION_DAY_NEW_USER = 2
        private const val DURATION_DAY_EXISTING_INACTIVE_USER = 7
        private const val DURATION_DAY_EXISTING_ACTIVE_USER = 28
    }
}
