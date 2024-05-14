package com.dashlane.premium.offer.details

import android.app.Activity
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.Legal
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.inappbilling.BillingManager
import com.dashlane.inappbilling.ConnectionScope
import com.dashlane.inappbilling.ServiceResult
import com.dashlane.navigation.Navigator
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfferDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val provider: OfferDetailsDataProvider,
    private val billingManager: BillingManager,
    private val purchaseCheckingCoordinator: PurchaseCheckingCoordinator,
    private val logger: OffersLogger,
    private val navigator: Navigator
) : ViewModel() {

    @get:StringRes
    val titleResId: Int
        get() = provider.getTitle(offerType)

    private val stateFlow =
        MutableStateFlow<OfferDetailsViewState>(OfferDetailsViewState.Loading(ViewData()))
    val uiState = stateFlow.asStateFlow()

    private val args = OfferDetailsFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val offerType: OfferType = args.offerType
    private val intendedPeriodicity: ProductPeriodicity = args.intendedPeriodicity

    val currentPageViewFlow: Flow<AnyPage>
        get() = logger.currentPageViewFlow

    init {
        viewModelScope.launch {
            stateFlow.emit(OfferDetailsViewState.Loading(ViewData()))
            val offerState = provider.getOffer(offerType)
            logState(offerState)
            when (offerState) {
                is OfferDetails -> stateFlow.emit(
                    OfferDetailsViewState.Success(
                        ViewData(
                            offerDetails = offerState
                        )
                    )
                )
                is OffersState.NoStoreOffersResultAvailable,
                is OffersState.NoValidOfferAvailable,
                is Offers -> {
                    stateFlow.emit(
                        OfferDetailsViewState.Error(
                            viewData = stateFlow.value.viewData.copy()
                        )
                    )
                }
            }
        }
    }

    fun goToPrivacy() = navigator.goToWebView(Legal.URL_PRIVACY_POLICY)

    fun goToTos() = navigator.goToWebView(Legal.URL_TERMS_OF_SERVICE)

    fun goBack() = navigator.navigateUp()

    fun startPurchase(activity: Activity, product: OfferDetails.Product) {
        onInAppPurchaseStarted(product)
        viewModelScope.launch {
            val priceInfo = product.priceInfo
            val serviceConnection = getBillingServiceConnection() ?: return@launch
            val serviceResult = serviceConnection.startPurchaseFlow(
                activity = activity,
                productDetails = product.productDetails.originalProductDetails,
                offerToken = product.priceInfo.subscriptionOfferToken,
                updateReference = product.update
            )
            val purchase = onBillingServiceResult(serviceResult, product)
            if (purchase != null) {
                
                purchaseCheckingCoordinator.openPlayStorePurchaseChecking(
                    context = activity,
                    sku = product.productId,
                    currencyCode = priceInfo.currencyCode,
                    price = priceInfo.baseOfferPriceValue,
                    purchaseOriginalJson = purchase.originalJson,
                    signature = purchase.signature
                )
                activity.finish()
            }
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

    private fun onInAppPurchaseStarted(product: OfferDetails.Product) {
        logger.logOpenStore(
            productPeriodicity = intendedPeriodicity,
            offerType = offerType,
            sku = product.productId,
            price = product.priceInfo.baseOfferPriceValue, 
            currency = product.priceInfo.currencyCode
        )
    }

    private suspend fun getBillingServiceConnection(): ConnectionScope? =
        billingManager.getServiceConnection()

    private fun onBillingServiceResult(
        serviceResult: ServiceResult,
        product: OfferDetails.Product
    ) =
        when (serviceResult) {
            is ServiceResult.Success.Purchases -> {
                val purchase = serviceResult.purchases.single()
                onPurchaseDone(product)
                purchase
            }

            else -> null
        }

    private fun onPurchaseDone(product: OfferDetails.Product) {
        logger.logPurchaseSuccess(
            productPeriodicity = intendedPeriodicity,
            offerType = offerType,
            sku = product.productId,
            price = product.priceInfo.baseOfferPriceValue, 
            currency = product.priceInfo.currencyCode
        )
    }

    private fun OffersState.containsIntroOffers(): Boolean = when (this) {
        is OfferDetails ->
            monthlyProduct?.productDetails is ProductDetailsWrapper.IntroductoryOfferProduct ||
                yearlyProduct?.productDetails is ProductDetailsWrapper.IntroductoryOfferProduct

        is Offers -> (monthlyOffers + yearlyOffers).any { it is OfferOverview.IntroductoryOffer }
        else -> false
    }

    fun getCtaString(priceInfo: OfferDetails.PriceInfo?) = provider.getCtaString(priceInfo)

    fun getMonthlyInfoString(priceInfo: OfferDetails.PriceInfo?) =
        provider.getMonthlyInfoString(priceInfo)

    fun getYearlyInfoString(priceInfo: OfferDetails.PriceInfo?) =
        provider.getYearlyInfoString(priceInfo)
}