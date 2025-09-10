package com.dashlane.premium

import android.text.format.DateUtils
import com.dashlane.premium.StoreOffersManager.UserNotLoggedException
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.payments.GetAccessibleStoreOffersService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
class StoreOffersCache(
    private val getAccessibleStoreOffersService: GetAccessibleStoreOffersService,
    private val sessionManager: SessionManager,
) : StoreOffersManager {
    private var lastOperation: LastOperation? = null
    private var prefetchJob: Job? = null

    override fun prefetchProductsForCurrentUser(coroutineScope: CoroutineScope) {
        if (prefetchJob == null || (prefetchJob?.isCompleted == true && lastOperation == null)) {
            prefetchJob = coroutineScope.launch {
                try {
                    fetchProductsForCurrentUser()
                } catch (e: DashlaneApiException) {
                    
                } catch (e: UserNotLoggedException) {
                    
                }
            }
        }
    }

    @Throws(DashlaneApiException::class, UserNotLoggedException::class)
    override suspend fun fetchProductsForCurrentUser(): GetAccessibleStoreOffersService.Data {
        
        val user = sessionManager.session?.authorization ?: throw UserNotLoggedException()

        lastOperation?.let { lastOperation ->
            if (lastOperation.isCacheHit(user.login)) {
                return lastOperation.storeOffers
            }
        }

        return fetchProducts(user)
    }

    fun flushCacheIfSubscriptionHasChanged(subscription: String?) {
        val previousSubscription = lastOperation?.storeOffers?.currentSubscription

        if (subscription != previousSubscription) {
            flushCache()
        }
    }

    fun flushCache() {
        prefetchJob?.cancel()
        prefetchJob = null
        lastOperation = null
    }

    private suspend fun fetchProducts(user: Authorization.User): GetAccessibleStoreOffersService.Data {
        val request = GetAccessibleStoreOffersService.Request(
            platform = GetAccessibleStoreOffersService.Request.Platform.PLAYSTORE_SUBSCRIPTION,
        )

        val response = getAccessibleStoreOffersService.execute(user, request)
        lastOperation = LastOperation(username = user.login, storeOffers = response.data)
        return response.data
    }

    private data class LastOperation(
        val storeOffers: GetAccessibleStoreOffersService.Data,
        private val username: String,
        private val timestamp: Long = System.currentTimeMillis(),
    ) {
        fun isCacheHit(newUsername: String): Boolean {
            return username == newUsername &&
                System.currentTimeMillis() - timestamp < CACHE_TIME_TO_LIVE
        }
    }

    companion object {
        private const val CACHE_TIME_TO_LIVE = 2 * DateUtils.DAY_IN_MILLIS
    }
}