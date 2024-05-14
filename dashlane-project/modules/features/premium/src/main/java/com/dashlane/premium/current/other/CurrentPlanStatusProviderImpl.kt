package com.dashlane.premium.current.other

import com.dashlane.accountstatus.AccountStatus
import com.dashlane.accountstatus.premiumstatus.hasLifetimeEntitlement
import com.dashlane.premium.offer.common.UserBenefitStatusProvider
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.userfeatures.getVpnAccessDeniedReason
import com.dashlane.util.inject.OptionalProvider
import java.time.Clock
import javax.inject.Inject

class CurrentPlanStatusProviderImpl @Inject constructor(
    private val accountStatusProvider: OptionalProvider<AccountStatus>,
    private val userBenefitStatusProvider: UserBenefitStatusProvider,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val clock: Clock
) : CurrentPlanStatusProvider {
    private val accountStatus: AccountStatus?
        get() = accountStatusProvider.get()

    override fun isB2bUser() = teamSpaceAccessorProvider.get()?.isB2bUser ?: false

    override fun getAccountStatus() =
        userBenefitStatusProvider.getFormattedStatus(accountStatus).type

    override fun hasLifeTimeEntitlement() =
        accountStatus?.premiumStatus?.hasLifetimeEntitlement(clock) ?: false

    override fun isVpnDeniedDueToNoPayment() =
        userFeaturesChecker.getVpnAccessDeniedReason() == PremiumStatus.Capabilitie.Info.Reason.NO_PAYMENT
}