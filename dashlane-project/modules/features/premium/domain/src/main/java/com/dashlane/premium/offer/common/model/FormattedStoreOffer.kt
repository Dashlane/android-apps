package com.dashlane.premium.offer.common.model

import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams.ReplacementMode

data class FormattedStoreOffer(
    val offerType: OfferType,
    val monthly: Option?,
    val yearly: Option?,
) {
    data class Option(
        val productDetails: ProductDetailsWrapper,
        val enable: Boolean,
        @ReplacementMode val mode: Int?,
    )
}