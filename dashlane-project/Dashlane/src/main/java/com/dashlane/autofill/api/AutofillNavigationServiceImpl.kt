package com.dashlane.autofill.api

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentSender
import com.dashlane.autofill.api.revertactionssettings.RevertActionsActivity
import com.dashlane.autofill.api.util.AutofillNavigationService
import com.dashlane.autofill.api.util.AutofillNavigationService.Companion.REQUEST_KEYBOARD_AUTOFILL_ON_BOARDING
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.NavigationUriBuilder
import com.dashlane.security.DashlaneIntent
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.util.DevUtil
import com.dashlane.util.clearTask
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AutofillNavigationServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AutofillNavigationService {
    override fun navigateToPlansPage(activity: Activity, origin: String) {
        activity.startActivity(
            Intent(
                ACTION_VIEW, NavigationUriBuilder()
                    .host(NavigationHelper.Destination.MainPath.GET_PREMIUM)
                    .origin(origin)
                    .appendPath(NavigationHelper.Destination.SecondaryPath.GetPremium.ESSENTIALS_OFFER)
                    .build()
            )
        )
    }

    override fun navigateToAutofillSettings(activity: Activity, startAsNewTask: Boolean) {
        activity.startActivity(
            Intent(
                ACTION_VIEW, NavigationUriBuilder()
                    .host(NavigationHelper.Destination.MainPath.SETTINGS)
                    .appendPath(NavigationHelper.Destination.SecondaryPath.SettingsPath.GENERAL)
                    .origin(AutofillNavigationService.ORIGIN_OS_SETTINGS)
                    .build()
            ).apply {
                if (startAsNewTask) {
                    flags = FLAG_ACTIVITY_NEW_TASK
                }
            }
        )
    }

    override fun navigateToPasswordSection(activity: Activity, startAsNewTask: Boolean) {
        activity.startActivity(
            Intent(
                ACTION_VIEW, NavigationUriBuilder()
                    .host(NavigationHelper.Destination.MainPath.PASSWORDS)
                    .origin(AutofillNavigationService.ORIGIN_OS_SETTINGS)
                    .build()
            ).apply {
                if (startAsNewTask) {
                    flags = FLAG_ACTIVITY_NEW_TASK
                }
            }
        )
    }

    override fun navigateToRevertActions(
        activity: Activity,
        autofillFormSource: AutoFillFormSource,
        origin: String
    ) {
        val intent = RevertActionsActivity.newIntent(context, autofillFormSource, origin)
        DevUtil.startActivityOrDefaultErrorMessage(activity, intent)
    }

    override fun getLongPressActionOnInline(): PendingIntent {
        val inlineLongPressIntent =
            DashlaneIntent.newInstance(context, HomeActivity::class.java).apply {
                clearTask()
                action = ACTION_VIEW
            }

        return PendingIntent.getActivity(
            context,
            0,
            inlineLongPressIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getOnBoardingIntentSender(): IntentSender {
        val intent = DashlaneIntent.newInstance(context, HomeActivity::class.java).apply {
            clearTask()
            action = ACTION_VIEW
            putExtra(REQUEST_KEYBOARD_AUTOFILL_ON_BOARDING, true)
        }

        return PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ).intentSender
    }
}