package com.dashlane.ui.menu.view.header

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.design.theme.typography.getStrongerStyle
import com.dashlane.teamspaces.model.SpaceColor
import com.dashlane.teamspaces.model.SpaceName
import com.dashlane.ui.menu.domain.MenuItemModel
import com.dashlane.ui.menu.domain.TeamspaceIcon
import com.dashlane.ui.menu.view.teamspace.MenuTeamspaceIcon

private val avatarSize = 48.dp

@Composable
fun MenuHeaderUserProfileItem(
    modifier: Modifier = Modifier,
    userProfile: MenuItemModel.Header.UserProfile,
    onUpgradeClick: (() -> Unit)?,
    onUserWrapperClick: () -> Unit
) {
    BaseMenuHeaderItem(
        modifier = modifier,
        icon = {
            Image(
                modifier = Modifier.size(avatarSize),
                painter = painterResource(id = R.drawable.ic_menu_dashlane),
                contentDescription = stringResource(id = R.string.and_accessibility_user_profile_icon)
            )
        },
        onUpgradeClick = onUpgradeClick.takeIf { userProfile.canUpgrade },
        statusText = userProfile.userName,
        statusHelper = stringResource(userProfile.userStatus),
        dropdownToken = null,
        onStatusWrapperClick = onUserWrapperClick,
    )
}

@Composable
fun MenuHeaderTeamspaceItem(
    modifier: Modifier = Modifier,
    teamspace: MenuItemModel.Header.TeamspaceProfile,
    onTeamspaceWrapperClick: () -> Unit
) {
    BaseMenuHeaderItem(
        modifier = modifier,
        icon = {
            MenuTeamspaceIcon(
                modifier = Modifier
                    .size(avatarSize)
                    .background(
                        color = when (teamspace.icon) {
                            TeamspaceIcon.Combined -> DashlaneTheme.colors.containerExpressiveBrandQuietIdle
                            is TeamspaceIcon.Space -> when (val color = teamspace.icon.spaceColor) {
                                is SpaceColor.FixColor -> colorResource(color.colorRes)
                                is SpaceColor.TeamColor -> Color(color.color)
                            }.copy(alpha = if (DashlaneTheme.colors.isLight) 0.2f else 0.3f)
                        },
                        shape = CircleShape
                    )
                    .padding(12.dp),
                icon = teamspace.icon
            )
        },
        onUpgradeClick = null,
        statusText = when (val spaceName = teamspace.name) {
            is SpaceName.FixName -> stringResource(spaceName.nameRes)
            is SpaceName.TeamName -> spaceName.value
        },
        statusHelper = stringResource(if (teamspace.mode) R.string.menu_v3_teamspace_select else R.string.menu_v3_teamspace_change),
        dropdownToken = if (teamspace.mode) IconTokens.caretUpOutlined else IconTokens.caretDownOutlined,
        onStatusWrapperClick = onTeamspaceWrapperClick,
    )
}

@Composable
fun MenuHeaderEnforcedTeamProfileItem(
    modifier: Modifier = Modifier,
    enforcedTeamspaceProfile: MenuItemModel.Header.EnforcedTeamspaceProfile
) {
    BaseMenuHeaderItem(
        modifier = modifier,
        icon = {
            Image(
                modifier = Modifier.size(avatarSize),
                painter = painterResource(id = R.drawable.ic_menu_dashlane),
                contentDescription = stringResource(id = R.string.and_accessibility_user_profile_icon)
            )
        },
        onUpgradeClick = null,
        statusText = enforcedTeamspaceProfile.userName,
        statusHelper = when (val spaceName = enforcedTeamspaceProfile.spaceName) {
            is SpaceName.FixName -> stringResource(spaceName.nameRes)
            is SpaceName.TeamName -> spaceName.value
        },
        dropdownToken = null,
        onStatusWrapperClick = { },
    )
}

@Suppress("LongMethod")
@Composable
private fun BaseMenuHeaderItem(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    onUpgradeClick: (() -> Unit)?,
    statusText: String,
    statusHelper: String?,
    dropdownToken: IconToken?,
    onStatusWrapperClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            onUpgradeClick?.let {
                Spacer(modifier = Modifier.width(8.dp))
                Spacer(modifier = Modifier.weight(1f))
                ButtonMedium(
                    layout = ButtonLayout.TextOnly(text = stringResource(R.string.menu_v3_upgrade)),
                    intensity = Intensity.Supershy,
                    onClick = onUpgradeClick
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStatusWrapperClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AnnotatedString(
                        statusText,
                        DashlaneTheme.typography.bodyStandardRegular.getStrongerStyle()
                    ),
                    color = DashlaneTheme.colors.textNeutralCatchy,
                    style = DashlaneTheme.typography.bodyStandardRegular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                dropdownToken?.let {
                    Icon(
                        modifier = Modifier.size(12.dp),
                        token = dropdownToken,
                        contentDescription = null,
                        tint = DashlaneTheme.colors.textNeutralCatchy
                    )
                }
            }
            statusHelper?.let {
                Text(
                    text = it,
                    color = DashlaneTheme.colors.textNeutralQuiet,
                    style = DashlaneTheme.typography.bodyHelperRegular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(1.dp)
                .background(DashlaneTheme.colors.borderNeutralQuietIdle)
        )
    }
}

@Preview
@Composable
private fun MenuHeaderUserProfileItemPreview() {
    DashlanePreview {
        MenuHeaderUserProfileItem(
            userProfile = MenuItemModel.Header.UserProfile(
                userName = "randomemail@provider.com",
                userStatus = R.string.plans_on_going_trial,
                canUpgrade = true
            ),
            onUpgradeClick = { },
            onUserWrapperClick = { }
        )
    }
}

@Preview
@Composable
private fun MenuHeaderTeamspaceCombinedItemPreview() {
    DashlanePreview {
        var mode: Boolean by rememberSaveable { mutableStateOf(false) }
        MenuHeaderTeamspaceItem(
            teamspace = MenuItemModel.Header.TeamspaceProfile(
                icon = TeamspaceIcon.Combined,
                name = SpaceName.FixName(R.string.teamspace_combined_title),
                mode = mode
            ),
            onTeamspaceWrapperClick = { mode = !mode }
        )
    }
}

@Preview
@Composable
private fun MenuHeaderTeamspacePersonalItemPreview() {
    DashlanePreview {
        var mode: Boolean by rememberSaveable { mutableStateOf(true) }
        MenuHeaderTeamspaceItem(
            teamspace = MenuItemModel.Header.TeamspaceProfile(
                icon = TeamspaceIcon.Space(
                    displayLetter = 'P',
                    spaceColor = SpaceColor.FixColor(R.color.teamspace_personal)
                ),
                name = SpaceName.FixName(R.string.teamspace_personal_title),
                mode = mode
            ),
            onTeamspaceWrapperClick = { mode = !mode }
        )
    }
}

@Preview
@Composable
private fun MenuHeaderTeamspaceTeamItemPreview() {
    DashlanePreview {
        var mode: Boolean by rememberSaveable { mutableStateOf(true) }
        MenuHeaderTeamspaceItem(
            teamspace = MenuItemModel.Header.TeamspaceProfile(
                icon = TeamspaceIcon.Space(
                    displayLetter = 'B',
                    spaceColor = SpaceColor.TeamColor(android.graphics.Color.BLUE)
                ),
                name = SpaceName.TeamName("Business"),
                mode = mode
            ),
            onTeamspaceWrapperClick = { mode = !mode }
        )
    }
}

@Preview
@Composable
private fun MenuHeaderEnforcedTeamspaceProfileItemPreview() {
    DashlanePreview {
        MenuHeaderEnforcedTeamProfileItem(
            enforcedTeamspaceProfile = MenuItemModel.Header.EnforcedTeamspaceProfile(
                userName = "randomemail@provider.com",
                spaceName = SpaceName.TeamName("The Dashling Dashers")
            )
        )
    }
}