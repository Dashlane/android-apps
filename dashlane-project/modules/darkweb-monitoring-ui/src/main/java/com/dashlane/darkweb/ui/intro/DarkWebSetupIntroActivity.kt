package com.dashlane.darkweb.ui.intro

import android.content.Intent
import android.os.Bundle
import com.dashlane.darkweb.registration.ui.R
import com.dashlane.darkweb.ui.setup.DarkWebSetupMailActivity
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.skocken.presentation.presenter.BasePresenter

class DarkWebSetupIntroActivity : DashlaneActivity() {
    private lateinit var presenter: Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val origin = intent?.getStringExtra(ORIGIN_KEY)

        setContentView(R.layout.activity_intro)
        presenter = Presenter()

        val usageLogRepository = UserActivityComponent(this).currentSessionUsageLogRepository
        val logger: DarkWebSetupIntroLogger = DarkWebSetupIntroLoggerImpl(usageLogRepository)
        presenter.logger = logger
        presenter.setView(IntroScreenViewProxy(this))

        logger.takeIf { savedInstanceState == null }?.logShow(origin)
    }

    companion object {

        const val ORIGIN_KEY = "origin"
    }

    private class Presenter : BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
        IntroScreenContract.Presenter {

        lateinit var logger: DarkWebSetupIntroLogger

        override fun onViewChanged() {
            super.onViewChanged()
            view.apply {
                setImageResource(R.drawable.logo_darkweb)
                setTitle(R.string.darkweb_setup_intro_title)
                setDescription(R.string.darkweb_setup_intro_body)
                setPositiveButton(R.string.darkweb_setup_button_next)
                setNegativeButton(R.string.darkweb_setup_button_cancel)
            }
        }

        override fun onClickPositiveButton() {
            val activity = activity ?: return
            val intent = Intent(activity, DarkWebSetupMailActivity::class.java)
            logger.logNext()
            activity.startActivity(intent)
            activity.finish()
        }

        override fun onClickNegativeButton() {
            logger.logCancel()
            activity?.finish()
        }

        override fun onClickNeutralButton() {
            
        }

        override fun onClickLink(position: Int, label: Int) {
            
        }
    }
}
