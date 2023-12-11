package com.dashlane.ui.premium.inappbilling

import com.dashlane.breach.BreachManager
import com.dashlane.core.premium.PremiumStatus
import com.dashlane.core.premium.PremiumStatusManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RefreshPremiumAfterPurchase @Inject constructor(
    private val premiumStatusManager: PremiumStatusManager,
    private val breachManager: BreachManager,
    private val accountStatusRepository: AccountStatusRepository,
    private val sessionManager: SessionManager,
    @MainCoroutineDispatcher private val mainCoroutineDispatcher: CoroutineDispatcher
) {

    @OptIn(DelicateCoroutinesApi::class)
    fun execute(errorListener: () -> Unit) {
        GlobalScope.launch(mainCoroutineDispatcher) {
            val session = sessionManager.session
            if (session == null) {
                notifyError(errorListener)
                return@launch
            }
            val previousPremiumStatus = accountStatusRepository.getPremiumStatus(session)
            val success = premiumStatusManager.refreshPremiumStatus(session)
            if (success) {
                val newPremiumStatus = accountStatusRepository.getPremiumStatus(session)
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
        if (previousPremiumStatus.premiumType != newPremiumStatus.premiumType &&
            !previousPremiumStatus.isPremium && newPremiumStatus.isPremium
        ) {
            
            breachManager.refreshIfNecessary(true)
        }
    }

    private fun notifyError(errorListener: () -> Unit = {}) {
        
        errorListener.invoke()
    }
}
