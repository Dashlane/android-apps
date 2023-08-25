package com.dashlane.premium.offer.common

import com.dashlane.premium.offer.common.model.ProductDetailsWrapper

interface ProductDetailsManager {

    @Throws(NoProductDetailsResult::class)
    suspend fun getProductDetailsMap(productIds: List<String>): Map<String, ProductDetailsWrapper>

    @Throws(NoProductDetailsResult::class)
    suspend fun getProductDetails(productId: String): ProductDetailsWrapper?

    object NoProductDetailsResult : Throwable()
}