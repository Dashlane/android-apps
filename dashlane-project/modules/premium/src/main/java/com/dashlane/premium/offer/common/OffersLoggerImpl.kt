package com.dashlane.premium.offer.common

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.events.user.CallToAction
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import com.dashlane.hermes.generated.definitions.CallToAction as CallToActionValue



class OffersLoggerImpl @Inject constructor(
    fromAutofillResolver: OffersFromAutofillResolver,
    premiumStatusManager: FormattedPremiumStatusManager,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val logRepository: LogRepository,
) : OffersLogger {
    private val legacyOffersLogger = LegacyOffersLogger(
        premiumStatusManager = premiumStatusManager,
        sessionManager = sessionManager,
        bySessionUsageLogRepository = bySessionUsageLogRepository
    )

    private var hasChosenAction = false
    private var displayedOffers: List<OfferType> = emptyList()

    override var origin: String? = null
        set(value) {
            field = value
            legacyOffersLogger.origin = value
        }

    private val currentPageViewChannel = Channel<Pair<AnyPage, Boolean>>(capacity = Channel.UNLIMITED)
    override val currentPageViewFlow = currentPageViewChannel.receiveAsFlow()
    private var fromAutofill = fromAutofillResolver.isFromAutofill(origin = origin.orEmpty())

    override fun showOfferList(
        productPeriodicity: ProductPeriodicity?,
        displayedOffers: List<OfferType>,
        hasIntroOffers: Boolean
    ) {
        legacyOffersLogger.showOfferList(productPeriodicity = productPeriodicity, displayedOffers = displayedOffers)
        hasChosenAction = false
        val page = if (hasIntroOffers) AnyPage.AVAILABLE_PLANS_INTRODUCTORY_OFFERS else AnyPage.AVAILABLE_PLANS
        currentPageViewChannel.trySend(page to fromAutofill)
        this.displayedOffers = displayedOffers
    }

    override fun onPeriodicityClicked(selectedPeriodicity: ProductPeriodicity, displayedOffers: List<OfferType>) {
        
        legacyOffersLogger.showOfferList(productPeriodicity = selectedPeriodicity, displayedOffers = displayedOffers)
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

    override fun showOfferDetails(
        productPeriodicity: ProductPeriodicity?,
        offerType: OfferType,
        hasIntroOffers: Boolean
    ) {
        legacyOffersLogger.showOfferDetails(productPeriodicity = productPeriodicity, offerType = offerType)
        currentPageViewChannel.trySend(offerType.toPage(hasIntroOffers) to fromAutofill)
    }

    override fun logOpenStore(
        productPeriodicity: ProductPeriodicity?,
        offerType: OfferType,
        sku: String,
        price: Float,
        currency: String
    ) {
        
        legacyOffersLogger.logOpenStore(
            productPeriodicity = productPeriodicity,
            offerType = offerType,
            sku = sku,
            price = price,
            currency = currency
        )
    }

    override fun logPurchaseSuccess(
        productPeriodicity: ProductPeriodicity?,
        offerType: OfferType,
        sku: String,
        price: Float,
        currency: String
    ) {
        
        legacyOffersLogger.logPurchaseSuccess(
            productPeriodicity = productPeriodicity,
            offerType = offerType,
            sku = sku,
            price = price,
            currency = currency
        )
    }

    override fun logStoreOffersError(offerType: OfferType?) {
        legacyOffersLogger.logStoreOffersError(offerType = offerType)
        if (offerType == null) {
            showOfferList(null, emptyList(), false)
        } else {
            showOfferDetails(null, offerType, false)
        }
    }

    override fun logSkuDetailsError(offerType: OfferType?) {
        legacyOffersLogger.logSkuDetailsError(offerType = offerType)
        if (offerType == null) {
            showOfferList(null, emptyList(), false)
        } else {
            showOfferDetails(null, offerType, false)
        }
    }

    override fun logNoValidOfferOption(offerType: OfferType?) {
        legacyOffersLogger.logNoValidOfferOption(offerType = offerType)
        if (offerType == null) {
            showOfferList(null, emptyList(), false)
        } else {
            showOfferDetails(null, offerType, false)
        }
    }

    override fun logVerifyingReceipt(planId: String) {
        
        legacyOffersLogger.logVerifyingReceipt(planId = planId)
    }

    override fun logVerifyingReceiptPreparePurchase(planId: String) {
        
        legacyOffersLogger.logVerifyingReceiptPreparePurchase(planId = planId)
    }

    override fun logVerifyingReceiptAcknowledgeNeeded(planId: String?) {
        planId ?: return
        
        legacyOffersLogger.logVerifyingReceiptAcknowledgeNeeded(planId = planId)
    }

    override fun logVerifyingReceiptHasSession(planId: String?) {
        planId ?: return
        
        legacyOffersLogger.logVerifyingReceiptHasSession(planId = planId)
    }

    override fun logVerifyingReceiptStart(planId: String?) {
        planId ?: return
        
        legacyOffersLogger.logVerifyingReceiptStart(planId = planId)
    }

    override fun logVerifyingReceiptSuccess(planId: String?) {
        planId ?: return
        
        legacyOffersLogger.logVerifyingReceiptSuccess(planId = planId)
    }

    override fun logVerifyingReceiptOnHomeStarted() {
        
        legacyOffersLogger.logVerifyingReceiptOnHomeStarted()
    }

    override fun logVerifyingReceiptError(planId: String) {
        
        legacyOffersLogger.logVerifyingReceiptError(planId = planId)
    }

    override fun logPurchaseComplete(planId: String) {
        
        legacyOffersLogger.logPurchaseComplete(planId = planId)
    }

    private fun OfferType.toPage(hasIntroOffers: Boolean) = if (hasIntroOffers) {
        when (this) {
            OfferType.ADVANCED -> AnyPage.AVAILABLE_PLANS_INTRODUCTORY_OFFERS_ESSENTIALS_DETAILS
            OfferType.PREMIUM -> AnyPage.AVAILABLE_PLANS_INTRODUCTORY_OFFERS_PREMIUM_DETAILS
            OfferType.FAMILY -> AnyPage.AVAILABLE_PLANS_INTRODUCTORY_OFFERS_FAMILY_DETAILS
        }
    } else {
        when (this) {
            OfferType.ADVANCED -> AnyPage.AVAILABLE_PLANS_ESSENTIALS_DETAILS
            OfferType.PREMIUM -> AnyPage.AVAILABLE_PLANS_PREMIUM_DETAILS
            OfferType.FAMILY -> AnyPage.AVAILABLE_PLANS_FAMILY_DETAILS
        }
    }

    private fun OfferType.toCallToActionValue() = when (this) {
        OfferType.ADVANCED -> CallToActionValue.ESSENTIAL_OFFER
        OfferType.PREMIUM -> CallToActionValue.PREMIUM_OFFER
        OfferType.FAMILY -> CallToActionValue.FAMILY_OFFER
    }
}