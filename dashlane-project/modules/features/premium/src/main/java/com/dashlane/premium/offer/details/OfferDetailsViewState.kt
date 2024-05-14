package com.dashlane.premium.offer.details

import com.dashlane.premium.offer.common.model.OfferDetails

sealed class OfferDetailsViewState {
    abstract val viewData: ViewData

    
    data class Loading(override val viewData: ViewData) : OfferDetailsViewState()
    data class Success(override val viewData: ViewData) : OfferDetailsViewState()
    data class Error(override val viewData: ViewData) : OfferDetailsViewState()
}
data class ViewData(
    val offerDetails: OfferDetails? = null
)