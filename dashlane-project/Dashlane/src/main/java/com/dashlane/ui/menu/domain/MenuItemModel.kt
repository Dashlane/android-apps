package com.dashlane.ui.menu.domain

import androidx.annotation.StringRes
import com.dashlane.design.iconography.IconToken
import com.dashlane.teamspaces.model.SpaceName

sealed class MenuItemModel {
    sealed class Header : MenuItemModel() {
        data class UserProfile(
            val userName: String,
            @StringRes val userStatus: Int,
            val canUpgrade: Boolean
        ) : Header()

        data class TeamspaceProfile(
            val icon: TeamspaceIcon,
            val name: SpaceName,
            val mode: Boolean
        ) : Header()

        data class EnforcedTeamspaceProfile(
            val userName: String,
            val spaceName: SpaceName
        ) : Header()
    }

    data class NavigationItem(
        val iconToken: IconToken,
        val iconTokenSelected: IconToken,
        @StringRes
        val titleResId: Int,
        val isSelected: Boolean,
        val endIcon: EndIcon? = null,
        val premiumTag: PremiumTag? = null,
        val callback: () -> Unit
    ) : MenuItemModel() {
        sealed class PremiumTag {
            data object PremiumOnly : PremiumTag()
            data class Trial(val remainingDays: Long) : PremiumTag()
        }

        sealed class EndIcon {
            data class DotNotification(@StringRes val contentDescription: Int) : EndIcon()
            data object NewLabel : EndIcon()
        }
    }

    data class TeamspaceItem(
        val spaceName: SpaceName,
        val icon: TeamspaceIcon,
        val onClick: () -> Unit
    ) : MenuItemModel()

    data object Divider : MenuItemModel()
    data class SectionHeader(@StringRes val titleResId: Int) : MenuItemModel()

    data object LockoutFooter : MenuItemModel()
}
