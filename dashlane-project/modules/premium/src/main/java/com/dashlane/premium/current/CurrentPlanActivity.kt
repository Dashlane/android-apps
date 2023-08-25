package com.dashlane.premium.current

import android.os.Bundle
import com.dashlane.hermes.LogRepository
import com.dashlane.premium.R
import com.dashlane.premium.current.other.CurrentPlanStatusProvider
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.userfeatures.UserFeaturesChecker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CurrentPlanActivity : DashlaneActivity() {

    @Inject
    lateinit var userFeaturesChecker: UserFeaturesChecker

    @Inject
    lateinit var currentPlanStatusProvider: CurrentPlanStatusProvider

    @Inject
    lateinit var logRepository: LogRepository

    private lateinit var presenter: CurrentPlanPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_plan)

        val provider = CurrentPlanDataProvider(
            userFeaturesChecker = userFeaturesChecker,
            statusProvider = currentPlanStatusProvider
        )

        presenter = CurrentPlanPresenter(
            navigator = navigator,
            logger = CurrentPlanLogger(logRepository)
        ).apply {
            setView(CurrentPlanViewProxy(this@CurrentPlanActivity))
            setProvider(provider)
            refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}