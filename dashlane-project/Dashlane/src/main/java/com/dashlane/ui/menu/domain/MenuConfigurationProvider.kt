package com.dashlane.ui.menu.domain

import com.dashlane.accountstatus.AccountStatus
import com.dashlane.accountstatus.premiumstatus.isTrial
import com.dashlane.accountstatus.premiumstatus.remainingDays
import com.dashlane.notification.badge.NotificationBadgeActor
import com.dashlane.premium.offer.common.UserBenefitStatusProvider
import com.dashlane.premium.offer.common.model.UserBenefitStatus
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.server.api.endpoints.premium.PremiumStatus.Capabilitie.Capability
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.ui.activities.fragments.checklist.ChecklistHelper
import com.dashlane.userfeatures.FeatureFlip
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.userfeatures.canShowVpn
import com.dashlane.userfeatures.canUpgradeToGetVpn
import com.dashlane.util.inject.OptionalProvider
import java.time.Clock
import javax.inject.Inject

class MenuConfigurationProvider @Inject constructor(
    private val checklistHelper: ChecklistHelper,
    private val notificationBadgeActor: NotificationBadgeActor,
    private val accountStatusProvider: OptionalProvider<AccountStatus>,
    private val sessionManager: SessionManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter,
    private val userBenefitStatusProvider: UserBenefitStatusProvider,
    private val clock: Clock
) {
    private val accountStatus: AccountStatus?
        get() = accountStatusProvider.get()
    private val premiumStatus: PremiumStatus?
        get() = accountStatusProvider.get()?.premiumStatus

    private val userPremiumStatusType: UserBenefitStatus.Type
        get() = userBenefitStatusProvider.getFormattedStatus(accountStatus).type

    private val teamSpaceAccessor: TeamSpaceAccessor?
        get() = teamSpaceAccessorProvider.get()
    private val userAlias: String
        get() = sessionManager.session?.userId ?: ""

    private val currentSpaceFilter: TeamSpace
        get() = currentTeamSpaceUiFilter.currentFilter.teamSpace

    private val canChangeSpace: Boolean
        get() = teamSpaceAccessor?.canChangeTeamspace == true

    private val enforcedTeamspace: TeamSpace?
        get() {
            val teamSpaceAccessor = teamSpaceAccessor ?: return null
            return if (teamSpaceAccessor.hasEnforcedTeamSpace) {
                teamSpaceAccessor.currentBusinessTeam
            } else {
                null
            }
        }

    private val selectableTeamspaces: List<TeamSpace>?
        get() {
            val teamSpaceAccessor = teamSpaceAccessor ?: return null
            return teamSpaceAccessor.availableSpaces.minus(currentSpaceFilter)
        }

    private val isPersonalPlanVisible: Boolean
        get() = checklistHelper.shouldDisplayChecklist()

    private val hasUnReadActionItems: Boolean
        get() = notificationBadgeActor.hasUnReadActionItems

    private val isTrial: Boolean
        get() = premiumStatus?.isTrial == true

    private val remainingDays: Long
        get() = premiumStatus?.remainingDays(clock) ?: 0L

    private val hasDataLeak: Boolean
        get() = userFeaturesChecker.has(Capability.DATALEAK)

    private val isVPNVisible: Boolean
        get() = userFeaturesChecker.canShowVpn()

    private val canUpgradeToGetVPN: Boolean
        get() = !userFeaturesChecker.has(Capability.SECUREWIFI) &&
            userFeaturesChecker.canUpgradeToGetVpn()

    private val isCollectionSharingVisible: Boolean
        get() = userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION)

    private fun canUpgradePremium() = when (val statusType = userPremiumStatusType) {
        is UserBenefitStatus.Type.Family -> statusType.isAdmin
        is UserBenefitStatus.Type.FamilyPlus -> statusType.isAdmin
        else -> true
    }

    fun getConfiguration(teamspaceSelectionMode: Boolean): MenuConfiguration =
        MenuConfiguration(
            userAlias = userAlias,
            teamspaceSelectionMode = teamspaceSelectionMode,
            isPersonalPlanVisible = isPersonalPlanVisible,
            hasUnReadActionItems = hasUnReadActionItems,
            isTrial = isTrial,
            hasDataLeak = hasDataLeak,
            remainingDays = remainingDays,
            isVPNVisible = isVPNVisible,
            isCollectionSharingVisible = isCollectionSharingVisible,
            canUpgradeToGetVPN = canUpgradeToGetVPN,
            canChangeSpace = canChangeSpace,
            currentSpace = currentSpaceFilter,
            selectableTeamspaces = selectableTeamspaces,
            userPremiumStatusType = userPremiumStatusType,
            canUpgradePremium = canUpgradePremium(),
            enforcedTeamspace = enforcedTeamspace
        )
}

data class MenuConfiguration(
    val userAlias: String,
    val teamspaceSelectionMode: Boolean,
    val isPersonalPlanVisible: Boolean,
    val hasUnReadActionItems: Boolean,
    val isTrial: Boolean,
    val hasDataLeak: Boolean,
    val remainingDays: Long,
    val isVPNVisible: Boolean,
    val isCollectionSharingVisible: Boolean,
    val canUpgradeToGetVPN: Boolean,
    val canChangeSpace: Boolean,
    val currentSpace: TeamSpace?,
    val selectableTeamspaces: List<TeamSpace>?,
    val userPremiumStatusType: UserBenefitStatus.Type,
    val canUpgradePremium: Boolean,
    val enforcedTeamspace: TeamSpace? = null
)