package com.dashlane.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.ViewCompat
import com.dashlane.R
import com.dashlane.databinding.ActivityHomeActivityLayoutBinding
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.menu.domain.MenuItemModel
import com.dashlane.ui.menu.domain.TeamspaceIcon
import com.dashlane.ui.menu.view.footer.MenuLockFooterItem
import com.dashlane.ui.menu.view.header.MenuHeaderTeamspaceItem
import com.dashlane.ui.menu.view.header.MenuHeaderUserProfileItem
import com.dashlane.ui.menu.view.item.MenuItem
import com.dashlane.ui.menu.view.separator.MenuSectionHeaderItem
import com.dashlane.ui.menu.view.separator.MenuSeparatorItem
import com.dashlane.ui.menu.view.teamspace.MenuTeamspaceItem
import com.google.accompanist.systemuicontroller.rememberSystemUiController

fun menuScreen(
    homeViewBinding: ActivityHomeActivityLayoutBinding,
    viewModel: MenuViewModel
) {
    val composeView: ComposeView = homeViewBinding.menuFrame
    
    composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    
    composeView.consumeWindowInsets = false
    
    ViewCompat.setOnApplyWindowInsetsListener(composeView) { _, windowInsets ->
        
        windowInsets
    }
    composeView.setContent {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = false
        SideEffect {
            systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = useDarkIcons)
        }
        DashlaneTheme {
            Box {
                val uiState by viewModel.uiState.collectAsState()
                MenuContent(
                    innerPadding = WindowInsets.systemBars.asPaddingValues(),
                    uiState = uiState,
                    onUpgradeClick = viewModel::onUpgradeClick,
                    onHeaderTeamspaceSelectorClick = viewModel::onHeaderTeamspaceSelectorClick,
                    onHeaderProfileClick = viewModel::onHeaderProfileClick,
                    onLockout = viewModel::onLockout
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f))
                        .statusBarsPadding()
                )
            }
        }
    }
}

@Composable
private fun MenuContent(
    innerPadding: PaddingValues,
    uiState: MenuState,
    onUpgradeClick: (() -> Unit)?,
    onHeaderTeamspaceSelectorClick: () -> Unit,
    onHeaderProfileClick: (canUpgrade: Boolean) -> Unit,
    onLockout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .background(color = DashlaneTheme.colors.containerAgnosticNeutralSupershy)
            .testTag("menu screen")
            .fillMaxHeight(),
        contentPadding = innerPadding
    ) {
        items(uiState.items) { item ->
            when (item) {
                is MenuItemModel.Header.UserProfile -> {
                    MenuHeaderUserProfileItem(
                        userProfile = item,
                        onUpgradeClick = onUpgradeClick,
                        onUserWrapperClick = { onHeaderProfileClick(item.canUpgrade) }
                    )
                }

                is MenuItemModel.Header.Teamspace -> {
                    MenuHeaderTeamspaceItem(
                        teamspace = item,
                        onTeamspaceWrapperClick = onHeaderTeamspaceSelectorClick
                    )
                }

                MenuItemModel.Divider -> {
                    MenuSeparatorItem()
                }

                is MenuItemModel.SectionHeader -> {
                    MenuSectionHeaderItem(item = item)
                }

                is MenuItemModel.NavigationItem -> {
                    MenuItem(item = item)
                }

                MenuItemModel.LockoutFooter -> {
                    MenuLockFooterItem { onLockout() }
                }

                is MenuItemModel.Teamspace -> {
                    MenuTeamspaceItem(item = item)
                }
            }
        }
    }
}

@Preview
@Composable
private fun B2cMenuContentPreview() {
    DashlanePreview {
        var index by rememberSaveable { mutableStateOf(0) }
        MenuContent(
            innerPadding = PaddingValues(),
            uiState = MenuState.Loaded(
                items = listOf(
                    MenuItemModel.Header.UserProfile(
                        userName = "randomemail@provider.com",
                        userStatus = R.string.menu_user_profile_status_trial,
                        canUpgrade = true
                    ),
                    MenuItemModel.NavigationItem(
                        iconToken = IconTokens.homeOutlined,
                        iconTokenSelected = IconTokens.homeFilled,
                        titleResId = R.string.menu_v2_home,
                        isSelected = index == 0
                    ) { index = 0 },
                    MenuItemModel.NavigationItem(
                        iconToken = IconTokens.notificationOutlined,
                        iconTokenSelected = IconTokens.notificationFilled,
                        titleResId = R.string.talk_to_me_menu_entry_title,
                        isSelected = index == 1,
                        endIcon = MenuItemModel.NavigationItem.EndIcon.DotNotification(
                            contentDescription = R.string.and_accessibility_notification
                        )
                    ) { index = 1 },
                    MenuItemModel.Divider,
                    MenuItemModel.SectionHeader(R.string.menu_v3_header_security_boosters),
                    MenuItemModel.NavigationItem(
                        iconToken = IconTokens.featureDarkWebMonitoringOutlined,
                        iconTokenSelected = IconTokens.featureDarkWebMonitoringOutlined,
                        titleResId = R.string.menu_v3_section_dark_web_monitoring,
                        isSelected = index == 2,
                        premiumTag = MenuItemModel.NavigationItem.PremiumTag.Trial(remainingDays = 21)
                    ) { index = 2 },
                    MenuItemModel.NavigationItem(
                        iconToken = IconTokens.featureVpnOutlined,
                        iconTokenSelected = IconTokens.featureVpnFilled,
                        titleResId = R.string.menu_vpn,
                        isSelected = index == 3,
                        premiumTag = MenuItemModel.NavigationItem.PremiumTag.PremiumOnly,
                        endIcon = MenuItemModel.NavigationItem.EndIcon.NewLabel
                    ) { index = 3 },
                    MenuItemModel.LockoutFooter
                )
            ),
            onUpgradeClick = { },
            onHeaderTeamspaceSelectorClick = { },
            onHeaderProfileClick = { },
            onLockout = { }
        )
    }
}

@Preview
@Composable
private fun B2bMenuContentPreview() {
    DashlanePreview {
        MenuContent(
            innerPadding = PaddingValues(),
            uiState = MenuState.Loaded(
                items = listOf(
                    MenuItemModel.Header.Teamspace(
                        icon = TeamspaceIcon.Combined,
                        name = "All spaces",
                        mode = true
                    ),
                    MenuItemModel.Teamspace(
                        name = "Personal",
                        icon = TeamspaceIcon.Space(
                            displayLetter = "P",
                            colorInt = 0xFFD000AF.toInt()
                        ),
                        onClick = {}
                    ),
                    MenuItemModel.Teamspace(
                        name = "Upshit",
                        icon = TeamspaceIcon.Space(
                            displayLetter = "U",
                            colorInt = 0xFF3366FF.toInt()
                        ),
                        onClick = {}
                    )
                )
            ),
            onUpgradeClick = { },
            onHeaderTeamspaceSelectorClick = { },
            onHeaderProfileClick = { },
            onLockout = { }
        )
    }
}
