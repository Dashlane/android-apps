package com.dashlane.premium.offer.common.model

import com.android.billingclient.api.BillingFlowParams

data class FormattedStoreOffer(
    val offerType: OfferType,
    val monthly: Option?,
    val yearly: Option?
) {
    data class Option(
        val productDetails: ProductDetailsWrapper,
        val enable: Boolean,
        @BillingFlowParams.ProrationMode val mode: Int?
    )
}
