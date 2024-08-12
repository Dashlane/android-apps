package com.dashlane.accountstatus.subscriptioncode

import com.dashlane.network.tools.authorization
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.endpoints.premium.GetSubscriptionCodeService
import com.dashlane.session.SessionManager
import javax.inject.Inject

interface SubscriptionCodeRepository {
    suspend fun get(): String
}

class SubscriptionCodeRepositoryImpl @Inject constructor(
    private val preferencesManager: UserPreferencesManager,
    private val getSubscriptionCodeService: GetSubscriptionCodeService,
    private val sessionManager: SessionManager,
) : SubscriptionCodeRepository {
    override suspend fun get(): String {
        preferencesManager.subscriptionCode?.let { subscriptionCode ->
            return subscriptionCode
        }

        val session = sessionManager.session ?: throw IllegalArgumentException("Invalid session")
        return getSubscriptionCodeService.execute(session.authorization)
            .data
            .subscriptionCode
            .also { subscriptionCode ->
                preferencesManager.subscriptionCode = subscriptionCode
            }
    }
}