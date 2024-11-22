package com.dashlane.createaccount

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import com.dashlane.debug.services.DaDaDaLogin
import com.dashlane.guidedonboarding.OnboardingQuestionnaireActivity
import com.dashlane.navigation.NavigationConstants
import com.dashlane.security.DashlaneIntent
import com.dashlane.ui.PostAccountCreationCoordinator
import com.dashlane.util.getParcelableExtraCompat
import javax.inject.Inject

class CreateAccountSuccessIntentFactory @Inject constructor(
    private val activity: Activity,
    private val coordinator: PostAccountCreationCoordinator,
    private val dadadaLogin: DaDaDaLogin
) {
    fun createIntent(): Intent {
        val intent = if (dadadaLogin.isSkipOnboardingPostAccountCreation) {
            coordinator.newHomeIntent()
        } else {
            DashlaneIntent.newInstance().apply {
                setClass(activity, OnboardingQuestionnaireActivity::class.java)
            }
        }
        return intent.apply {
            putExtra(
                NavigationConstants.STARTED_WITH_INTENT,
                activity.intent.getParcelableExtraCompat<Parcelable>(NavigationConstants.STARTED_WITH_INTENT)
            )
        }
    }
}