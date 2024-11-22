package com.dashlane.accountstatus.subscriptioncode

import com.dashlane.session.authorization
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.premium.GetSubscriptionCodeService
import com.dashlane.session.SessionManager
import javax.inject.Inject

interface SubscriptionCodeRepository {
    suspend fun get(): String
}

class SubscriptionCodeRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val getSubscriptionCodeService: GetSubscriptionCodeService,
    private val sessionManager: SessionManager,
) : SubscriptionCodeRepository {
    override suspend fun get(): String {
        val session = sessionManager.session ?: throw IllegalArgumentException("Invalid session")
        val preferences = preferencesManager[session.username]
        preferences.subscriptionCode?.let { subscriptionCode ->
            return subscriptionCode
        }
        return getSubscriptionCodeService.execute(session.authorization)
            .data
            .subscriptionCode
            .also { subscriptionCode ->
                preferences.subscriptionCode = subscriptionCode
            }
    }
}