package com.dashlane.core.premium

import com.dashlane.premium.current.other.CurrentPlanStatusProvider
import com.dashlane.premium.offer.common.FormattedPremiumStatusManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.getVpnAccessDeniedReason
import javax.inject.Inject

class CurrentPlanStatusProviderImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val accountStatusRepository: AccountStatusRepository,
    private val formattedPremiumStatusManager: FormattedPremiumStatusManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>
) : CurrentPlanStatusProvider {
    override fun isB2bUser() =
        teamspaceAccessorProvider.get()?.canChangeTeamspace() ?: false

    override fun getAccountStatus() =
        formattedPremiumStatusManager.getFormattedStatus().type

    override fun hasLifeTimeEntitlement() =
        getPremiumStatus()?.hasLifetimeEntitlement() ?: false

    override fun isVpnDeniedDueToNoPayment() =
        userFeaturesChecker.getVpnAccessDeniedReason() == UserFeaturesChecker.CapabilityReason.NO_PAYMENT

    private fun getPremiumStatus(): PremiumStatus? {
        val session = sessionManager.session ?: return null
        return accountStatusRepository.getPremiumStatus(session)
    }
}