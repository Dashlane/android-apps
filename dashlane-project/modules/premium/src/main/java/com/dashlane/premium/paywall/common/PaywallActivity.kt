package com.dashlane.premium.paywall.common

import android.os.Bundle
import com.dashlane.premium.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.util.getSerializableExtraCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaywallActivity : DashlaneActivity() {

    @Inject
    lateinit var paywallIntroFactory: PaywallIntroFactory

    @Inject
    lateinit var logger: PaywallLogger

    private lateinit var presenter: PaywallPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val origin = intent.getStringExtra(ORIGIN_KEY)
        val paywallType = requireNotNull(intent.getSerializableExtraCompat<PaywallIntroType>(PAYWALL_INTRO_TYPE_KEY))
        val paywallIntro = paywallIntroFactory.get(paywallType, origin)

        logger.trackingKey = paywallIntro.trackingKey

        presenter = PaywallPresenter(
            paywallIntro = paywallIntro,
            navigator = navigator,
            logger = logger
        )

        presenter.setView(IntroScreenViewProxy(activity = this))

        logger.takeIf { savedInstanceState == null }?.onShowPaywall(presenter)
    }

    override fun onDestroy() {
        if (isFinishing) {
            logger.onLeaving()
        }
        super.onDestroy()
    }

    companion object {
        const val ORIGIN_KEY = "origin"
        const val PAYWALL_INTRO_TYPE_KEY = "paywallIntroType"
    }
}
