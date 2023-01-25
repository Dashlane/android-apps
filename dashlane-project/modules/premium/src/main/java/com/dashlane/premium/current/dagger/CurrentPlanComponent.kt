package com.dashlane.premium.current.dagger

import android.content.Context
import com.dashlane.hermes.LogRepository
import com.dashlane.navigation.Navigator
import com.dashlane.premium.current.other.CurrentPlanStatusProvider
import com.dashlane.util.userfeatures.UserFeaturesChecker

interface CurrentPlanComponent {
    val navigator: Navigator
    val userFeaturesChecker: UserFeaturesChecker
    val currentPlanStatusProvider: CurrentPlanStatusProvider
    val logRepository: LogRepository

    interface Application {
        val component: CurrentPlanComponent
    }

    companion object {
        operator fun invoke(context: Context) = (context.applicationContext as Application).component
    }
}