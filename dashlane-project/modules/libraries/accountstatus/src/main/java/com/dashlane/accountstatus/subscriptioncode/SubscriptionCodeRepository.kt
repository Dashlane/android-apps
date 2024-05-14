package com.dashlane.accountstatus.subscriptioncode

import com.dashlane.accountstatus.subscriptioncode.service.SubscriptionCodeService
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import javax.inject.Inject

interface SubscriptionCodeRepository {
    suspend fun get(): String
}

class SubscriptionCodeRepositoryImpl @Inject constructor(
    private val preferencesManager: UserPreferencesManager,
    private val subscriptionCodeService: SubscriptionCodeService,
    private val sessionManager: SessionManager,
) : SubscriptionCodeRepository {
    override suspend fun get(): String {
        preferencesManager.subscriptionCode?.let { subscriptionCode ->
            return subscriptionCode
        }

        val session = sessionManager.session ?: throw IllegalArgumentException("Invalid session")
        val subscriptionCode = subscriptionCodeService.getSubscriptionCode(
            login = session.userId,
            uki = session.uki,
        ).content.subscriptionCode

        preferencesManager.subscriptionCode = subscriptionCode
        return subscriptionCode
    }
}