package com.dashlane.premium.offer.list.model

import com.dashlane.premium.offer.common.model.OfferType

sealed class OfferOverview {
    abstract val type: OfferType
    abstract val title: String
    abstract val description: String
    abstract val billedPrice: String?
    abstract val currentPlanLabel: String?
    abstract val currencyCode: String

    data class BaseOffer(
        override val type: OfferType,
        override val title: String,
        override val description: String,
        override val billedPrice: String? = null,
        override val currentPlanLabel: String? = null,
        override val currencyCode: String
    ) : OfferOverview()

    data class IntroductoryOffer(
        override val type: OfferType,
        override val title: String,
        override val description: String,
        override val billedPrice: String? = null,
        override val currentPlanLabel: String? = null,
        override val currencyCode: String,
        val barredPrice: String? = null,
        val additionalInfo: String? = null,
        val discountCallOut: String?,
    ) : OfferOverview()
}
