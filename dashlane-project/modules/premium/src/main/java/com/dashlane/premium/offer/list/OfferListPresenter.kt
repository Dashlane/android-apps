package com.dashlane.premium.offer.list

import androidx.navigation.NavController
import com.dashlane.premium.offer.common.OffersLogger
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.Offers
import com.dashlane.premium.offer.common.model.OffersState
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import com.dashlane.premium.offer.list.model.OfferOverview
import com.dashlane.premium.offer.list.view.OfferListFragmentDirections.Companion.goToOffersDetailsFromOffersOverview
import com.dashlane.util.coroutines.DeferredViewModel
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class OfferListPresenter(
    private val coroutineScope: CoroutineScope,
    viewModel: DeferredViewModel<OffersState?>,
    private val navController: NavController,
    private val logger: OffersLogger
) :
    OfferListContract.Presenter,
    BasePresenter<OfferListContract.DataProvider, OfferListContract.ViewProxy>() {

    private var displayedOffers: List<OfferType> = emptyList()
    private var selectedPeriodicity: ProductPeriodicity = ProductPeriodicity.MONTHLY
    private var debounceOfferClickedJob: Job? = null

    init {
        val deferred = viewModel.deferred ?: viewModel.async {
            providerOrNull?.getOffers()
        }

        coroutineScope.launch(Dispatchers.Main) {
            
            val progressJob = launch {
                delay(getLoaderDebounceMillis())
                try {
                    view.showProgress()
                    coroutineContext[Job]!!.join()
                } finally {
                    view.hideProgress()
                }
            }

            val offers = deferred.await()
            logState(offers)
            if (offers is Offers) {
                view.showAvailableOffers(offers)
            } else {
                view.showEmptyState()
            }

            
            progressJob.cancel()
        }
    }

    private fun logState(state: OffersState?) {
        when (state) {
            is Offers -> {
                displayedOffers = state.monthlyOffers.map { it.type }
                logger.showOfferList(
                    productPeriodicity = ProductPeriodicity.MONTHLY,
                    displayedOffers = displayedOffers,
                    hasIntroOffers = (state.monthlyOffers + state.yearlyOffers).any { it is OfferOverview.IntroductoryOffer }
                )
            }

            OffersState.NoStoreOffersResultAvailable -> logger.logStoreOffersError()
            OffersState.NoValidOfferAvailable -> logger.logNoValidOfferOption()
            else -> Unit
        }
    }

    private fun getLoaderDebounceMillis() =
        resources?.getInteger(android.R.integer.config_mediumAnimTime)?.toLong() ?: 300

    override fun onOfferClicked(type: OfferType) {
        debounce(scope = coroutineScope) {
            navigateToOfferDetails(type)
        }
    }

    override fun onMonthlyPeriodicityClicked() {
        selectedPeriodicity = ProductPeriodicity.MONTHLY
        logger.onPeriodicityClicked(selectedPeriodicity, displayedOffers)
    }

    override fun onYearlyPeriodicityClicked() {
        selectedPeriodicity = ProductPeriodicity.YEARLY
        logger.onPeriodicityClicked(selectedPeriodicity, displayedOffers)
    }

    private fun debounce(
        delayMillis: Long = 300L,
        scope: CoroutineScope,
        action: () -> Unit
    ) {
        if (debounceOfferClickedJob == null) {
            debounceOfferClickedJob = scope.launch {
                action()
                delay(delayMillis)
                debounceOfferClickedJob = null
            }
        }
    }

    private fun navigateToOfferDetails(type: OfferType) {
        logger.onOfferDetailsClicked(selectedOffer = type)
        navController.navigate(
            goToOffersDetailsFromOffersOverview(
                offerType = type,
                origin = logger.origin,
                intendedPeriodicity = selectedPeriodicity
            )
        )
    }
}