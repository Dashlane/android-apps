package com.dashlane.premium.offer.details

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.inappbilling.BillingManager
import com.dashlane.inappbilling.ConnectionScope
import com.dashlane.inappbilling.ServiceResult
import com.dashlane.premium.offer.common.OffersLogger
import com.dashlane.premium.offer.common.PurchaseCheckingCoordinator
import com.dashlane.premium.offer.common.model.OfferDetails
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.Offers
import com.dashlane.premium.offer.common.model.OffersState
import com.dashlane.premium.offer.common.model.ProductDetailsWrapper
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import com.dashlane.premium.offer.details.view.OfferDetailsFragmentArgs
import com.dashlane.premium.offer.list.model.OfferOverview
import com.dashlane.premium.offer.list.view.OfferListFragment.Companion.userLockedOut
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
internal class OfferDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val provider: OfferDetailsDataProvider,
    private val billingManager: BillingManager,
    override val purchaseCheckingCoordinator: PurchaseCheckingCoordinator,
    private val logger: OffersLogger,
    private val resources: Resources
) : ViewModel(), OfferDetailsViewModelContract {

    @get:StringRes
    override val titleResId: Int
        get() = provider.getTitle(offerType)

    override val showProgressFlow = MutableStateFlow(false)
    override val offerDetailsFlow = MutableSharedFlow<OfferDetails?>(replay = 1)

    private val args = OfferDetailsFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val offerType: OfferType = args.offerType
    private val intendedPeriodicity: ProductPeriodicity = args.intendedPeriodicity
    private val origin: String? = args.origin

    override val currentPageViewFlow: Flow<Pair<AnyPage, Boolean>>
        get() = logger.currentPageViewFlow

    init {
        logger.origin = origin.orEmpty()

        viewModelScope.launch {
            val deferred = viewModelScope.async {
                provider.getOffer(offerType)
            }

            
            val progressJob = launch {
                delay(getLoaderDebounceMillis())
                try {
                    showProgressFlow.tryEmit(true)
                    coroutineContext[Job]!!.join()
                } finally {
                    showProgressFlow.tryEmit(false)
                }
            }

            val offerDetails = deferred.await()
            logState(offerDetails)
            offerDetailsFlow.tryEmit(offerDetails as? OfferDetails)

            
            progressJob.cancel()
        }
    }

    private fun logState(state: OffersState) {
        val hasIntroOffers = state.containsIntroOffers()
        when (state) {
            OffersState.NoStoreOffersResultAvailable -> logger.logStoreOffersError(offerType)
            OffersState.NoValidOfferAvailable -> logger.logNoValidOfferOption(offerType)
            is Offers,
            is OfferDetails -> {
                logger.showOfferDetails(
                    productPeriodicity = intendedPeriodicity,
                    offerType = offerType,
                    hasIntroOffers = hasIntroOffers
                )
            }
        }
    }

    private fun getLoaderDebounceMillis() =
        resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()

    override fun onInAppPurchaseStarted(product: OfferDetails.Product) {
        logger.logOpenStore(
            productPeriodicity = intendedPeriodicity,
            offerType = offerType,
            sku = product.productId,
            price = product.priceInfo.baseOfferPriceValue, 
            currency = product.priceInfo.currencyCode
        )
    }

    override suspend fun getBillingServiceConnection(): ConnectionScope? =
        billingManager.getServiceConnection()

    override fun onBillingServiceResult(serviceResult: ServiceResult, product: OfferDetails.Product) =
        when (serviceResult) {
            is ServiceResult.Success.Purchases -> {
                val purchase = serviceResult.purchases.single()
                val userLockedOut = userLockedOut(origin)
                onPurchaseDone(product, purchase, userLockedOut)
                purchase to userLockedOut
            }

            else -> null
        }

    override fun onPurchaseDone(product: OfferDetails.Product, purchase: Purchase, userLockedOut: Boolean) {
        logger.logPurchaseSuccess(
            productPeriodicity = intendedPeriodicity,
            offerType = offerType,
            sku = product.productId,
            price = product.priceInfo.baseOfferPriceValue, 
            currency = product.priceInfo.currencyCode
        )
    }

    private fun OffersState.containsIntroOffers(): Boolean = when (this) {
        is OfferDetails -> monthlyProduct?.productDetails is ProductDetailsWrapper.IntroductoryOfferProduct ||
            yearlyProduct?.productDetails is ProductDetailsWrapper.IntroductoryOfferProduct

        is Offers -> (monthlyOffers + yearlyOffers).any { it is OfferOverview.IntroductoryOffer }
        else -> false
    }
}