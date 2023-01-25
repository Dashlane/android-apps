package com.dashlane.csvimport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dashlane.csvimport.internal.onboardingchromeimport.OnboardingChromeImportContract
import com.dashlane.csvimport.internal.onboardingchromeimport.OnboardingChromeImportPresenter
import com.dashlane.csvimport.internal.onboardingchromeimport.OnboardingChromeImportViewProxy
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingChromeImportActivity : DashlaneActivity() {

    private lateinit var presenter: OnboardingChromeImportContract.Presenter

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_onboarding_chrome_import)

        val origin = intent.getStringExtra(EXTRA_ORIGIN) ?: ""

        setCurrentPageView(AnyPage.IMPORT_CHROME)

        val usageLogRepository = UserActivityComponent(this).currentSessionUsageLogRepository
        val viewProxy = OnboardingChromeImportViewProxy(this)
        presenter = OnboardingChromeImportPresenter(origin, userPreferencesManager, usageLogRepository)
            .apply {
                setView(viewProxy)
                this.onCreate(savedInstanceState)
            }
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    companion object {
        fun newIntent(context: Context, origin: UsageLogCode75.Origin?): Intent =
            newIntent(context, origin?.code)

        fun newIntent(context: Context, origin: String?): Intent =
            Intent(context, OnboardingChromeImportActivity::class.java)
                .putExtra(EXTRA_ORIGIN, origin)

        private const val EXTRA_ORIGIN = "origin"
    }
}