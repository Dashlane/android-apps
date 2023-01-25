package com.dashlane.premium.offer.common

import com.dashlane.premium.offer.common.model.FormattedStoreOffer
import com.dashlane.server.api.endpoints.payments.StoreOffersService

interface StoreOffersFormatter {

    @Throws(ProductDetailsManager.NoProductDetailsResult::class)
    suspend fun build(storeOffers: StoreOffersService.Data): List<FormattedStoreOffer>
}
