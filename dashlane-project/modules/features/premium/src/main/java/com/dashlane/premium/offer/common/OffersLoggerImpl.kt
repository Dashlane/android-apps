package com.dashlane.premium.offer.common

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.Button
import com.dashlane.hermes.generated.definitions.ClickOrigin
import com.dashlane.hermes.generated.events.user.CallToAction
import com.dashlane.hermes.generated.events.user.Click
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import com.dashlane.hermes.generated.definitions.CallToAction as CallToActionValue

class OffersLoggerImpl @Inject constructor(
    private val logRepository: LogRepository,
) : OffersLogger {

    private var hasChosenAction = false
    private var displayedOffers: List<OfferType> = emptyList()

    private val currentPageViewChannel = Channel<AnyPage>(capacity = Channel.UNLIMITED)
    override val currentPageViewFlow = currentPageViewChannel.receiveAsFlow()

    override fun showOfferList(
        productPeriodicity: ProductPeriodicity?,
        displayedOffers: List<OfferType>,
        hasIntroOffers: Boolean
    ) {
        hasChosenAction = false
        val page = if (hasIntroOffers) AnyPage.AVAILABLE_PLANS_INTRODUCTORY_OFFERS else AnyPage.AVAILABLE_PLANS
        currentPageViewChannel.trySend(page)
        this.displayedOffers = displayedOffers
    }

    override fun onPeriodicityClicked(selectedPeriodicity: ProductPeriodicity, displayedOffers: List<OfferType>) {
        
    }

    override fun onOfferDetailsClicked(selectedOffer: OfferType) {
        hasChosenAction = true
        logRepository.queueEvent(
            CallToAction(
                callToActionList = displayedOffers.map { it.toCallToActionValue() },
                hasChosenNoAction = false,
                chosenAction = selectedOffer.toCallToActionValue()
            )
        )
    }

    override fun onOfferListFinishing() {
        if (!hasChosenAction) {
            logRepository.queueEvent(
                CallToAction(
                    callToActionList = displayedOffers.map { it.toCallToActionValue() },
                    hasChosenNoAction = true,
                )
            )
        }
    }

    override fun onManagePasswordClicked() {
        logRepository.queueEvent(
            Click(
                button = Button.MANAGE_LOGINS,
                clickOrigin = ClickOrigin.FREE_PLAN_DETAILS
            )
        )
    }

    override fun showOfferDetails(
        productPeriodicity: ProductPeriodicity?,
        offerType: OfferType,
        hasIntroOffers: Boolean
    ) {
        currentPageViewChannel.trySend(offerType.toPage(hasIntroOffers))
    }

    override fun logOpenStore(
        productPeriodicity: ProductPeriodicity?,
        offerType: OfferType,
        sku: String,
        price: Float,
        currency: String
    ) {
        
    }

    override fun logPurchaseSuccess(
        productPeriodicity: ProductPeriodicity?,
        offerType: OfferType,
        sku: String,
        price: Float,
        currency: String
    ) {
        
    }

    override fun logStoreOffersError(offerType: OfferType?) {
        if (offerType == null) {
            showOfferList(null, emptyList(), false)
        } else {
            showOfferDetails(null, offerType, false)
        }
    }

    override fun logSkuDetailsError(offerType: OfferType?) {
        if (offerType == null) {
            showOfferList(null, emptyList(), false)
        } else {
            showOfferDetails(null, offerType, false)
        }
    }

    override fun logNoValidOfferOption(offerType: OfferType?) {
        if (offerType == null) {
            showOfferList(null, emptyList(), false)
        } else {
            showOfferDetails(null, offerType, false)
        }
    }

    override fun logVerifyingReceipt(planId: String) {
        
    }

    override fun logVerifyingReceiptPreparePurchase(planId: String) {
        
    }

    override fun logVerifyingReceiptAcknowledgeNeeded(planId: String?) {
        planId ?: return
        
    }

    override fun logVerifyingReceiptHasSession(planId: String?) {
        planId ?: return
        
    }

    override fun logVerifyingReceiptStart(planId: String?) {
        planId ?: return
        
    }

    override fun logVerifyingReceiptSuccess(planId: String?) {
        planId ?: return
        
    }

    override fun logVerifyingReceiptOnHomeStarted() {
        
    }

    override fun logVerifyingReceiptError(planId: String) {
        
    }

    override fun logPurchaseComplete(planId: String) {
        
    }

    private fun OfferType.toPage(hasIntroOffers: Boolean) = if (hasIntroOffers) {
        when (this) {
            OfferType.ADVANCED -> AnyPage.AVAILABLE_PLANS_INTRODUCTORY_OFFERS_ESSENTIALS_DETAILS
            OfferType.PREMIUM -> AnyPage.AVAILABLE_PLANS_INTRODUCTORY_OFFERS_PREMIUM_DETAILS
            OfferType.FAMILY -> AnyPage.AVAILABLE_PLANS_INTRODUCTORY_OFFERS_FAMILY_DETAILS
            OfferType.FREE -> AnyPage.AVAILABLE_PLANS_FREE_DETAILS
        }
    } else {
        when (this) {
            OfferType.ADVANCED -> AnyPage.AVAILABLE_PLANS_ESSENTIALS_DETAILS
            OfferType.PREMIUM -> AnyPage.AVAILABLE_PLANS_PREMIUM_DETAILS
            OfferType.FAMILY -> AnyPage.AVAILABLE_PLANS_FAMILY_DETAILS
            OfferType.FREE -> AnyPage.AVAILABLE_PLANS_FREE_DETAILS
        }
    }

    private fun OfferType.toCallToActionValue() = when (this) {
        OfferType.ADVANCED -> CallToActionValue.ESSENTIAL_OFFER
        OfferType.PREMIUM -> CallToActionValue.PREMIUM_OFFER
        OfferType.FAMILY -> CallToActionValue.FAMILY_OFFER
        OfferType.FREE -> CallToActionValue.FREE_OFFER
    }
}