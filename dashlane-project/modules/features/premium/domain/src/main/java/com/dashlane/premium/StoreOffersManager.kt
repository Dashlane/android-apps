package com.dashlane.premium

import com.dashlane.server.api.endpoints.payments.GetAccessibleStoreOffersService
import com.dashlane.server.api.exceptions.DashlaneApiException
import kotlinx.coroutines.CoroutineScope

interface StoreOffersManager {
    fun prefetchProductsForCurrentUser(coroutineScope: CoroutineScope)

    @Throws(DashlaneApiException::class, UserNotLoggedException::class)
    suspend fun fetchProductsForCurrentUser(): GetAccessibleStoreOffersService.Data

    class UserNotLoggedException : Exception("Session is null")
}