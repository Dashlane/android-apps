package com.dashlane.ui.activities.onboarding

import android.content.Context
import android.content.Intent
import com.dashlane.events.AppEvents
import com.dashlane.events.clearLastEvent
import com.dashlane.lock.UnlockEvent
import com.dashlane.security.DashlaneIntent
import com.dashlane.ui.PostAccountCreationCoordinator
import com.dashlane.ui.activities.HomeActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class HomeActivityIntentCoordinator @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val appEvents: AppEvents
) : PostAccountCreationCoordinator {

    override fun getHomeScreenAfterAccountCreationIntent(): Intent {
        appEvents.clearLastEvent<UnlockEvent>()
        return newHomeIntent()
    }

    override fun newHomeIntent(): Intent =
        DashlaneIntent.newInstance(applicationContext, HomeActivity::class.java)
}
