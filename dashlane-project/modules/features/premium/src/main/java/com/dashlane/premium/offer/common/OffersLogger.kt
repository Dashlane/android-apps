package com.dashlane.premium.offer.common

import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.ProductPeriodicity
import kotlinx.coroutines.flow.Flow

interface OffersLogger {

    val currentPageViewFlow: Flow<AnyPage>

    fun showOfferList(productPeriodicity: ProductPeriodicity?, displayedOffers: List<OfferType>, hasIntroOffers: Boolean)

    fun onPeriodicityClicked(selectedPeriodicity: ProductPeriodicity, displayedOffers: List<OfferType>)

    fun onOfferDetailsClicked(selectedOffer: OfferType)

    fun onOfferListFinishing()

    fun showOfferDetails(
        productPeriodicity: ProductPeriodicity?,
        offerType: OfferType,
        hasIntroOffers: Boolean
    )

    fun logOpenStore(
        productPeriodicity: ProductPeriodicity?,
        offerType: OfferType,
        sku: String,
        price: Float,
        currency: String
    )

    fun logPurchaseSuccess(
        productPeriodicity: ProductPeriodicity?,
        offerType: OfferType,
        sku: String,
        price: Float,
        currency: String
    )

    fun logStoreOffersError(offerType: OfferType? = null)

    fun logSkuDetailsError(offerType: OfferType? = null)

    fun logNoValidOfferOption(offerType: OfferType? = null)


    fun logVerifyingReceipt(planId: String)
    fun logVerifyingReceiptPreparePurchase(planId: String)
    fun logVerifyingReceiptAcknowledgeNeeded(planId: String?)
    fun logVerifyingReceiptHasSession(planId: String?)
    fun logVerifyingReceiptStart(planId: String?)
    fun logVerifyingReceiptSuccess(planId: String?)
    fun logVerifyingReceiptOnHomeStarted()
    fun logVerifyingReceiptError(planId: String)
    fun logPurchaseComplete(planId: String)
}