package com.dashlane.premium.offer.list.model

import androidx.annotation.StringRes
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.premium.offer.common.model.ProductPeriodicity

internal data class CurrentOffer(
    val type: OfferType,
    val periodicity: ProductPeriodicity,
    @StringRes val labelResId: Int
)
