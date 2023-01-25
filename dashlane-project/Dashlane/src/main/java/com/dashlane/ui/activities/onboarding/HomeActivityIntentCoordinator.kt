package com.dashlane.ui.activities.onboarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.events.clearLastEvent
import com.dashlane.lock.UnlockEvent
import com.dashlane.navigation.NavigationConstants
import com.dashlane.security.DashlaneIntent
import com.dashlane.ui.PostAccountCreationCoordinator
import com.dashlane.ui.activities.HomeActivity



class HomeActivityIntentCoordinator {
    companion object : PostAccountCreationCoordinator {

        

        private const val EXTRA_ORIGIN_ACCOUNT_CREATION = "from_account_creation"

        

        override fun startHomeScreenAfterAccountCreation(activity: Activity) {
            SingletonProvider.getAppEvents().clearLastEvent<UnlockEvent>()
            val startedWithIntent =
                activity.intent.getParcelableExtra<Parcelable?>(NavigationConstants.STARTED_WITH_INTENT)
            activity.startActivity(newHomeIntent(activity, startedWithIntent))
        }

        override fun newHomeIntent(
            context: Context,
            startedWithIntent: Parcelable?,
            fromAccountCreation: Boolean
        ): Intent = DashlaneIntent.newInstance(context, HomeActivity::class.java)
            .putExtra(NavigationConstants.STARTED_WITH_INTENT, startedWithIntent)
            .putExtra(EXTRA_ORIGIN_ACCOUNT_CREATION, fromAccountCreation)
    }
}