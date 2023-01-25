package com.dashlane.premium.offer.list.model

import androidx.annotation.StringRes
import com.dashlane.premium.offer.common.model.OfferType

sealed class OfferOverview {
    abstract val type: OfferType
    abstract val title: Int
    abstract val description: Int
    abstract val pricing: Pricing?
    abstract val onGoingRes: Int?

    data class BaseOffer(
        override val type: OfferType,
        @StringRes override val title: Int,
        @StringRes override val description: Int,
        override val pricing: Pricing? = null,
        @StringRes override val onGoingRes: Int? = null
    ) : OfferOverview()

    data class IntroductoryOffer(
        override val type: OfferType,
        @StringRes override val title: Int,
        @StringRes override val description: Int,
        override val pricing: Pricing? = null,
        @StringRes override val onGoingRes: Int? = null,
        val discountCallOut: DiscountCallOut
    ) : OfferOverview()
}
