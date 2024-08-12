package com.dashlane.ui.menu.domain

import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.design.iconography.IconTokens
import com.dashlane.navigation.NavigationUtils
import com.dashlane.navigation.Navigator
import com.dashlane.premium.offer.common.model.UserBenefitStatus
import com.dashlane.teamspaces.model.TeamSpace

class BuildMenuNavigationUseCase(
    private val navigator: Navigator,
    private val menuConfiguration: MenuConfiguration
) {

    private val menuItemPersonalPlan: MenuItemModel.NavigationItem?
        get() = if (menuConfiguration.isPersonalPlanVisible) {
            MenuItemModel.NavigationItem(
                iconToken = IconTokens.tipOutlined,
                iconTokenSelected = IconTokens.tipFilled,
                titleResId = R.string.menu_v3_section_personal_plan,
                isSelected = isSelected(arrayOf(R.id.nav_personal_plan))
            ) {
                navigator.goToPersonalPlanOrHome()
            }
        } else {
            null
        }

    private val menuItemVaultHome: MenuItemModel.NavigationItem
        get() = MenuItemModel.NavigationItem(
            iconToken = IconTokens.homeOutlined,
            iconTokenSelected = IconTokens.homeFilled,
            titleResId = R.string.menu_v2_home,
            isSelected = isSelected(arrayOf(R.id.nav_home))
        ) {
            navigator.goToHome()
        }

    private val notificationCenterEndIcon: MenuItemModel.NavigationItem.EndIcon?
        get() = if (menuConfiguration.hasUnReadActionItems) {
            MenuItemModel.NavigationItem.EndIcon.DotNotification(
                contentDescription = R.string.and_accessibility_notification
            )
        } else {
            null
        }

    private val menuItemActionCenter: MenuItemModel.NavigationItem
        get() = MenuItemModel.NavigationItem(
            iconToken = IconTokens.notificationOutlined,
            iconTokenSelected = IconTokens.notificationFilled,
            titleResId = R.string.talk_to_me_menu_entry_title,
            isSelected = isSelected(arrayOf(R.id.nav_notif_center)),
            endIcon = notificationCenterEndIcon
        ) {
            navigator.goToActionCenter()
        }

    private val menuItemPasswordGenerator: MenuItemModel.NavigationItem
        get() = MenuItemModel.NavigationItem(
            iconToken = IconTokens.featurePasswordGeneratorOutlined,
            iconTokenSelected = IconTokens.featurePasswordGeneratorFilled,
            titleResId = R.string.generated_password,
            isSelected = isSelected(arrayOf(R.id.nav_password_generator))
        ) {
            navigator.goToPasswordGenerator()
        }

    private val menuItemSharingCenter: MenuItemModel.NavigationItem
        get() = MenuItemModel.NavigationItem(
            iconToken = IconTokens.sharedOutlined,
            iconTokenSelected = IconTokens.sharedFilled,
            titleResId = R.string.menu_v3_section_sharing_center,
            isSelected = isSelected(arrayOf(R.id.nav_sharing_center))
        ) {
            navigator.goToPasswordSharing()
        }

    private val menuItemPasswordHealth: MenuItemModel.NavigationItem
        get() = MenuItemModel.NavigationItem(
            iconToken = IconTokens.featurePasswordHealthOutlined,
            iconTokenSelected = IconTokens.featurePasswordHealthOutlined,
            titleResId = R.string.menu_v3_section_security_dashboard,
            isSelected = isSelected(
                arrayOf(
                    R.id.nav_identity_dashboard,
                    R.id.nav_password_analysis
                )
            )
        ) {
            navigator.goToIdentityDashboard()
        }

    private val dwmPremiumTag: MenuItemModel.NavigationItem.PremiumTag?
        get() =
            when {
                menuConfiguration.isTrial ->
                    MenuItemModel.NavigationItem.PremiumTag.Trial(menuConfiguration.remainingDays)

                menuConfiguration.hasDataLeak -> null
                else -> MenuItemModel.NavigationItem.PremiumTag.PremiumOnly
            }

    private val menuItemDarkWebMonitoring: MenuItemModel.NavigationItem
        get() = MenuItemModel.NavigationItem(
            iconToken = IconTokens.featureDarkWebMonitoringOutlined,
            iconTokenSelected = IconTokens.featureDarkWebMonitoringOutlined,
            titleResId = R.string.menu_v3_section_dark_web_monitoring,
            isSelected = isSelected(arrayOf(R.id.nav_dark_web_monitoring)),
            premiumTag = dwmPremiumTag
        ) {
            navigator.goToDarkWebMonitoring()
        }

    private val menuItemAuthenticator: MenuItemModel.NavigationItem
        get() = MenuItemModel.NavigationItem(
            iconToken = IconTokens.featureAuthenticatorOutlined,
            iconTokenSelected = IconTokens.featureAuthenticatorFilled,
            titleResId = R.string.menu_authenticator,
            isSelected = isSelected(
                arrayOf(
                    R.id.nav_authenticator_dashboard,
                    R.id.nav_authenticator_suggestions
                )
            )
        ) {
            navigator.goToAuthenticator()
        }

    private val vpnPremiumTag: MenuItemModel.NavigationItem.PremiumTag?
        get() = MenuItemModel.NavigationItem.PremiumTag.PremiumOnly.takeIf {
            menuConfiguration.canUpgradeToGetVPN
        }

    private val menuItemVpn: MenuItemModel.NavigationItem?
        get() = if (menuConfiguration.isVPNVisible) {
            MenuItemModel.NavigationItem(
                iconToken = IconTokens.featureVpnOutlined,
                iconTokenSelected = IconTokens.featureVpnFilled,
                titleResId = R.string.menu_vpn,
                isSelected = isSelected(arrayOf(R.id.nav_vpn_third_party)),
                premiumTag = vpnPremiumTag
            ) {
                navigator.goToVpn()
            }
        } else {
            null
        }

    private val menuItemCollections: MenuItemModel.NavigationItem
        get() = MenuItemModel.NavigationItem(
            iconToken = IconTokens.folderOutlined,
            iconTokenSelected = IconTokens.folderFilled,
            titleResId = R.string.menu_collections,
            isSelected = isSelected(arrayOf(R.id.nav_collections_list))
        ) {
            navigator.goToCollectionsList()
        }

    private val menuItemSettings: MenuItemModel.NavigationItem
        get() = MenuItemModel.NavigationItem(
            iconToken = IconTokens.settingsOutlined,
            iconTokenSelected = IconTokens.settingsFilled,
            titleResId = R.string.menu_v2_settings_button,
            isSelected = isSelected(arrayOf(R.id.nav_settings))
        ) {
            navigator.goToSettings()
        }

    private val menuItemHelpCenter: MenuItemModel.NavigationItem
        get() = MenuItemModel.NavigationItem(
            iconToken = IconTokens.feedbackHelpOutlined,
            iconTokenSelected = IconTokens.feedbackHelpFilled,
            titleResId = R.string.menu_help_center,
            isSelected = false
        ) {
            navigator.goToHelpCenter()
        }

    fun build(onSpaceClick: (teamspace: TeamSpace) -> Unit): List<MenuItemModel> {
        return buildList {
            val header = buildHeader(menuConfiguration)
            add(header)
            val otherItems = if (menuConfiguration.teamspaceSelectionMode) {
                buildItemsModeTeamspaceSelection(onSpaceClick)
            } else {
                buildItemsModeDefault()
            }
            addAll(otherItems)
        }
    }

    private fun buildItemsModeTeamspaceSelection(onSpaceClick: (teamspace: TeamSpace) -> Unit): List<MenuItemModel> {
        val teamspaces = menuConfiguration.selectableTeamspaces
        return if (teamspaces.isNullOrEmpty()) {
            emptyList()
        } else {
            teamspaces.map { space ->
                MenuItemModel.TeamspaceItem(
                    spaceName = space.name,
                    icon = getTeamSpaceIcon(space),
                    onClick = { onSpaceClick(space) }
                )
            }
        }
    }

    private fun buildHeader(menuConfiguration: MenuConfiguration): MenuItemModel.Header {
        return when {
            menuConfiguration.canChangeSpace && menuConfiguration.currentSpace != null -> MenuItemModel.Header.TeamspaceProfile(
                icon = getTeamSpaceIcon(menuConfiguration.currentSpace),
                name = menuConfiguration.currentSpace.name,
                mode = menuConfiguration.teamspaceSelectionMode
            )
            menuConfiguration.enforcedTeamspace != null -> MenuItemModel.Header.EnforcedTeamspaceProfile(
                userName = menuConfiguration.userAlias,
                spaceName = menuConfiguration.enforcedTeamspace.name
            )
            else -> MenuItemModel.Header.UserProfile(
                userName = menuConfiguration.userAlias,
                userStatus = getAccountStatusText(menuConfiguration.userPremiumStatusType),
                canUpgrade = menuConfiguration.canUpgradePremium
            )
        }
    }

    @StringRes
    private fun getAccountStatusText(statusType: UserBenefitStatus.Type) = when (statusType) {
        UserBenefitStatus.Type.Legacy -> R.string.menu_user_profile_status_legacy
        UserBenefitStatus.Type.Trial -> R.string.menu_user_profile_status_trial
        UserBenefitStatus.Type.AdvancedIndividual -> R.string.plans_advanced_title
        UserBenefitStatus.Type.PremiumIndividual -> R.string.menu_user_profile_status_premium
        UserBenefitStatus.Type.PremiumPlusIndividual -> R.string.menu_user_profile_status_premium_plus
        is UserBenefitStatus.Type.Family -> R.string.menu_user_profile_status_premium_family
        is UserBenefitStatus.Type.FamilyPlus -> R.string.menu_user_profile_status_premium_plus_family
        UserBenefitStatus.Type.Free, UserBenefitStatus.Type.Unknown -> R.string.menu_user_profile_status_free
    }

    private fun getTeamSpaceIcon(teamspace: TeamSpace) = when (teamspace) {
        TeamSpace.Combined -> TeamspaceIcon.Combined
        else -> TeamspaceIcon.Space(teamspace.displayLetter, teamspace.color)
    }

    private fun buildItemsModeDefault() = buildList {
        add(menuItemPersonalPlan)
        add(menuItemVaultHome)
        add(menuItemActionCenter)
        add(menuItemPasswordGenerator)
        addSeparator()

        
        addHeader(R.string.menu_v3_header_security_boosters)
        add(menuItemSharingCenter)
        add(menuItemPasswordHealth)
        add(menuItemDarkWebMonitoring)
        add(menuItemAuthenticator)
        add(menuItemVpn)
        add(menuItemCollections)
        addSeparator()

        
        addHeader(R.string.menu_v3_header_manage_account)
        add(menuItemSettings)
        add(menuItemHelpCenter)

        add(MenuItemModel.LockoutFooter)
    }.filterNotNull()

    private fun isSelected(destinationIds: Array<Int>): Boolean {
        val currentDestination = navigator.currentDestination ?: return false
        return NavigationUtils.matchDestination(currentDestination, destinationIds)
    }

    private fun MutableList<MenuItemModel?>.addHeader(@StringRes textResId: Int) {
        add(MenuItemModel.SectionHeader(textResId))
    }

    private fun MutableList<MenuItemModel?>.addSeparator() {
        add(MenuItemModel.Divider)
    }
}
