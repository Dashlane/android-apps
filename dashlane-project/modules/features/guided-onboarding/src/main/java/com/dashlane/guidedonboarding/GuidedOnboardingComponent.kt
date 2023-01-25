package com.dashlane.guidedonboarding

import android.content.Context
import com.dashlane.ui.PostAccountCreationCoordinator

interface GuidedOnboardingComponent {
    val postAccountCreationCoordinator: PostAccountCreationCoordinator
}

@Suppress("FunctionName")
fun GuidedOnboardingComponent(context: Context) = (context.applicationContext as GuidedOnboardingApplication).component