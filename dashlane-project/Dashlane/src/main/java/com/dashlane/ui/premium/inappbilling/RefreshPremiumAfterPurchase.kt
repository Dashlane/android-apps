package com.dashlane.ui.premium.inappbilling

import com.dashlane.core.premium.PremiumStatus
import com.dashlane.core.premium.PremiumStatusManager
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.useractivity.log.usage.UsageLogConstant
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



object RefreshPremiumAfterPurchase {

    @OptIn(DelicateCoroutinesApi::class)
    @JvmStatic
    fun execute(errorListener: () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            val session = SingletonProvider.getSessionManager().session
            if (session == null) {
                notifyError(errorListener)
                return@launch
            }
            val accountStatusRepository = SingletonProvider.getComponent().accountStatusRepository
            val previousPremiumStatus = accountStatusRepository.getPremiumStatus(session)
            val verifyPremium = PremiumStatusManager.create()
            val success = withContext(Dispatchers.Default) { verifyPremium.refreshPremiumStatus(session) }
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
            UsageLogCode35GoPremium.send(UsageLogConstant.PremiumAction.boughtPremium)

            
            SingletonProvider.getBreachManager().refreshIfNecessary(true)
        }
    }

    private fun notifyError(errorListener: () -> Unit = {}) {
        UsageLogCode35GoPremium.send(UsageLogConstant.PremiumAction.errorAfterPremiumStatusValidation)
        
        errorListener.invoke()
    }
}
