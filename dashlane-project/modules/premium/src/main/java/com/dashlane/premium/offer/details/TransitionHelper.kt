package com.dashlane.premium.offer.details

import com.dashlane.inappbilling.UpdateReference
import com.dashlane.premium.offer.common.InAppBillingDebugPreference
import com.dashlane.premium.offer.common.model.FormattedStoreOffer
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.server.api.endpoints.payments.StoreOffersService
import com.dashlane.server.api.endpoints.payments.StoreOffersService.Data.CurrentSubscriptionType.PLAYSTORE_RENEWABLE
import javax.inject.Inject

class TransitionHelper @Inject constructor(
    private val debugPreference: InAppBillingDebugPreference
) {

    operator fun invoke(
        option: FormattedStoreOffer.Option,
        storeOffers: StoreOffersService.Data 
    ): Pair<Boolean, UpdateReference?> {
        val productId = storeOffers.getCurrentPlayStoreSubscription()
        val oldPurchaseToken = storeOffers.purchaseToken
        val overridingEnable = debugPreference.isAllPurchaseActionsAllowed()
        val overridingMode = debugPreference.getOverridingProratedMode()
        return when {
            
            !option.enable && !overridingEnable ->
                false to null
            
            productId.isNullOrBlank() ->
                true to null
            
            productId == option.productDetails.productId ->
                true to null
            
            oldPurchaseToken.isNullOrBlank() -> {
                false to null
            }
            overridingMode != null -> {
                true to UpdateReference(productId, oldPurchaseToken, overridingMode)
            }
            
            option.mode == null -> {
                false to null
            }
            else -> {
                true to UpdateReference(productId = productId, oldPurchaseToken = oldPurchaseToken, mode = option.mode)
            }
        }
    }

    internal class MissingPurchaseToken(offerType: OfferType, currentSku: String) :
        Throwable("Missing purchase token for subscription update to $offerType from $currentSku.") {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    internal class MissingTransitionMode(offerType: OfferType, currentSku: String, newSku: String) :
        Throwable("Missing PlayStore subscription update mode for $offerType from $currentSku to $newSku.") {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}

private fun StoreOffersService.Data.getCurrentPlayStoreSubscription() = when {
    currentSubscription?.isNotBlank() == true &&
            currentSubscriptionType == PLAYSTORE_RENEWABLE -> currentSubscription
    else -> null
}