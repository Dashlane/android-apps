package com.dashlane.premium.paywall.common

import com.dashlane.navigation.Navigator
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.skocken.presentation.presenter.BasePresenter

class PaywallPresenter(
    paywallIntro: PaywallIntro,
    private val navigator: Navigator,
    private val logger: PaywallLogger
) : BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
    IntroScreenContract.Presenter,
    PaywallIntro by paywallIntro {

    @Suppress("SpreadOperator")
    override fun onViewChanged() {
        super.onViewChanged()
        view.apply {
            provideDetailsView(context)?.let {
                view.setDetailView(it)
            }
            setImageResource(image)
            setTitle(title)
            setDescription(resources.getString(message, *messageFormatArgs))
            setLinks(R.string.paywall_intro_see_plan_options_cta)
            setPositiveButton(R.string.paywall_intro_upgrade_premium_cta)
            setNegativeButton(R.string.paywall_intro_close_cta)
        }
    }

    override fun onClickPositiveButton() {
        logger.onClickUpgrade()
        navigator.goToOffers(offerType = OfferType.PREMIUM.toString(), origin = trackingKey)
        activity?.finish()
    }

    override fun onClickNegativeButton() {
        logger.onClickClose()
        activity?.finish()
    }

    override fun onClickNeutralButton() {
        
    }

    override fun onClickLink(position: Int, label: Int) {
        logger.onClickSeeAllOptions()
        navigator.goToOffers(origin = trackingKey)
        activity?.finish()
    }
}