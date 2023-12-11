package com.dashlane.ui.menu.domain

import com.dashlane.notification.badge.NotificationBadgeActor
import com.dashlane.premium.offer.common.FormattedPremiumStatusManager
import com.dashlane.premium.offer.common.model.UserBenefitStatus
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.activities.fragments.checklist.ChecklistHelper
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.Capability
import com.dashlane.util.userfeatures.UserFeaturesChecker.FeatureFlip
import com.dashlane.util.userfeatures.canShowVpn
import com.dashlane.util.userfeatures.canUpgradeToGetVpn
import javax.inject.Inject

class MenuConfigurationProvider @Inject constructor(
    private val checklistHelper: ChecklistHelper,
    private val notificationBadgeActor: NotificationBadgeActor,
    private val accountStatusRepository: AccountStatusRepository,
    private val sessionManager: SessionManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val teamspaceRepository: TeamspaceManagerRepository,
    private val premiumStatusManager: FormattedPremiumStatusManager
) {
    private val userPremiumStatusType: UserBenefitStatus.Type
        get() = premiumStatusManager.getFormattedStatus().type

    private val userAlias: String
        get() = sessionManager.session?.userId ?: ""

    private val teamspaceManager: TeamspaceManager?
        get() = sessionManager.session?.let { session -> teamspaceRepository[session] }

    private val currentSpace: Teamspace?
        get() = teamspaceManager?.current

    private val canChangeSpace: Boolean
        get() = teamspaceManager?.canChangeTeamspace() == true

    private val selectableTeamspaces: List<Teamspace>?
        get() = teamspaceManager?.let { teamspaceManager ->
            teamspaceManager.all.filterNot { it == teamspaceManager.current }
        }

    private val isPersonalPlanVisible: Boolean
        get() = checklistHelper.shouldDisplayChecklist()

    private val hasUnReadActionItems: Boolean
        get() = notificationBadgeActor.hasUnReadActionItems

    private val isTrial: Boolean
        get() = sessionManager.session?.let(accountStatusRepository::getPremiumStatus)?.isTrial == true

    private val remainingDays: Long
        get() = sessionManager.session?.let(accountStatusRepository::getPremiumStatus)
            .let { it?.remainingDays } ?: 0L

    private val hasDataLeak: Boolean
        get() = userFeaturesChecker.has(Capability.DATA_LEAK)

    private val isVPNVisible: Boolean
        get() = userFeaturesChecker.canShowVpn()

    private val canUpgradeToGetVPN: Boolean
        get() = !userFeaturesChecker.has(Capability.VPN_ACCESS) &&
            userFeaturesChecker.canUpgradeToGetVpn()

    private val isCollectionSharingVisible: Boolean
        get() = userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION_MILESTONE_3)

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
            currentSpace = currentSpace,
            selectableTeamspaces = selectableTeamspaces,
            userPremiumStatusType = userPremiumStatusType,
            canUpgradePremium = canUpgradePremium()
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
    val currentSpace: Teamspace?,
    val selectableTeamspaces: List<Teamspace>?,
    val userPremiumStatusType: UserBenefitStatus.Type,
    val canUpgradePremium: Boolean,
)