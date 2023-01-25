package com.dashlane.premium.offer.common

import com.dashlane.server.api.endpoints.payments.StoreOffersService
import com.dashlane.server.api.exceptions.DashlaneApiException
import kotlinx.coroutines.CoroutineScope

interface StoreOffersManager {

    fun prefetchProductsForCurrentUser(coroutineScope: CoroutineScope)

    @Throws(DashlaneApiException::class, UserNotLoggedException::class)
    suspend fun fetchProductsForCurrentUser(): StoreOffersService.Data

    class UserNotLoggedException : Exception("Session is null")
}