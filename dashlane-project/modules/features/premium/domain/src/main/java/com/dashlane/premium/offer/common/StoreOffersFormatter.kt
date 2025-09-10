package com.dashlane.premium.offer.common

import com.dashlane.premium.offer.common.model.FormattedStoreOffer
import com.dashlane.server.api.endpoints.payments.GetAccessibleStoreOffersService

interface StoreOffersFormatter {
    @Throws(ProductDetailsManager.NoProductDetailsResult::class)
    suspend fun build(storeOffers: GetAccessibleStoreOffersService.Data): List<FormattedStoreOffer>
}