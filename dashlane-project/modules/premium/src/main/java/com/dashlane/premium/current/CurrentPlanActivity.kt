package com.dashlane.premium.current

import android.os.Bundle
import com.dashlane.premium.R
import com.dashlane.premium.current.dagger.CurrentPlanComponent
import com.dashlane.ui.activities.DashlaneActivity

class CurrentPlanActivity : DashlaneActivity() {
    private val component by lazy(LazyThreadSafetyMode.NONE) { CurrentPlanComponent(this) }

    private lateinit var presenter: CurrentPlanPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_plan)

        val provider = CurrentPlanDataProvider(
            userFeaturesChecker = component.userFeaturesChecker,
            statusProvider = component.currentPlanStatusProvider
        )

        presenter = CurrentPlanPresenter(
            navigator = component.navigator,
            logger = CurrentPlanLogger(component.logRepository)
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