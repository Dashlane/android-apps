package com.dashlane.ui.menu.item

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.ui.menu.item.MenuItem.PremiumTag
import com.dashlane.util.userfeatures.UserFeaturesChecker



class CapabilityMenuItemPremiumTagProvider(
    private val sessionManager: SessionManager,
    private val accountStatusRepository: AccountStatusRepository,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val capability: UserFeaturesChecker.Capability
) : MenuItemPremiumTagProvider {

    constructor(capability: UserFeaturesChecker.Capability) : this(
        SingletonProvider.getSessionManager(),
        SingletonProvider.getComponent().accountStatusRepository,
        SingletonProvider.getUserFeatureChecker(),
        capability
    )

    override val premiumTag: PremiumTag
        get() = sessionManager.session?.let(accountStatusRepository::getPremiumStatus).let { premiumStatus ->
            when {
                premiumStatus?.isTrial == true -> PremiumTag.Trial(premiumStatus.remainingDays)
                userFeaturesChecker.has(capability) -> PremiumTag.None
                else -> PremiumTag.PremiumOnly
            }
        }
}