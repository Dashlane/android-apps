package com.dashlane.login.pages.enforce2fa

import android.os.Bundle
import com.dashlane.R
import com.dashlane.performancelogger.TimeToLoadLocalLogger
import com.dashlane.performancelogger.TimeToLoadRemoteLogger
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Enforce2faLimitActivity : DashlaneActivity() {

    @Inject
    lateinit var presenter: Enforce2faLimitPresenter

    @Inject
    lateinit var timeToLoadRemoteLogger: TimeToLoadRemoteLogger

    @Inject
    lateinit var timeToLoadLocalLogger: TimeToLoadLocalLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        presenter.setView(IntroScreenViewProxy(activity = this))
    }

    override fun onStart() {
        super.onStart()
        presenter.onViewStarted()
        clearLoadAccountLogger()
    }

    override fun onResume() {
        presenter.onViewResumed()
        super.onResume()
    }

    override fun onPause() {
        presenter.onViewPaused()
        super.onPause()
    }

    override fun onBackPressed() {
        
    }

    private fun clearLoadAccountLogger() {
        timeToLoadLocalLogger.clear()
        timeToLoadRemoteLogger.clear()
    }
}
