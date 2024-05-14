package com.dashlane.ui.premium.inappbilling

import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.premiumstatus.isPremium
import com.dashlane.breach.BreachManager
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.session.SessionManager
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class RefreshPremiumAfterPurchase @Inject constructor(
    private val breachManager: BreachManager,
    private val accountStatusRepository: AccountStatusRepository,
    private val sessionManager: SessionManager,
    @MainCoroutineDispatcher private val mainCoroutineDispatcher: CoroutineDispatcher
) {

    val premiumStatus: PremiumStatus?
        get() = sessionManager.session?.let { accountStatusRepository[it]?.premiumStatus }

    @OptIn(DelicateCoroutinesApi::class)
    fun execute(errorListener: () -> Unit) {
        GlobalScope.launch(mainCoroutineDispatcher) {
            val session = sessionManager.session
            if (session == null) {
                notifyError(errorListener)
                return@launch
            }
            val previousPremiumStatus = premiumStatus
            val success = accountStatusRepository.refreshFor(session)
            if (success) {
                val newPremiumStatus = premiumStatus
                onPremiumStatusUpdated(previousPremiumStatus, newPremiumStatus)
            } else {
                notifyError(errorListener)
            }
        }
    }

    private fun onPremiumStatusUpdated(previousPremiumStatus: PremiumStatus?, newPremiumStatus: PremiumStatus?) {
        if (previousPremiumStatus == null || newPremiumStatus == null) {
            return
        }
        if (!previousPremiumStatus.isPremium && newPremiumStatus.isPremium) {
            
            breachManager.refreshIfNecessary(true)
        }
    }

    private fun notifyError(errorListener: () -> Unit = {}) {
        
        errorListener.invoke()
    }
}
