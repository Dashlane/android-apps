package com.dashlane.login.pages.enforce2fa

import android.annotation.SuppressLint
import android.os.Bundle
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Enforce2faLimitActivity : DashlaneActivity() {

    @Inject
    lateinit var presenter: Enforce2faLimitPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        presenter.setView(IntroScreenViewProxy(activity = this))
    }

    override fun onStart() {
        super.onStart()
        presenter.onViewStarted()
    }

    override fun onResume() {
        presenter.onViewResumed()
        super.onResume()
    }

    override fun onPause() {
        presenter.onViewPaused()
        super.onPause()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        
    }
}
