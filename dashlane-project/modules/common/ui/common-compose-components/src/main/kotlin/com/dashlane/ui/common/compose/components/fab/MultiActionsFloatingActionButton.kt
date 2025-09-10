package com.dashlane.ui.common.compose.components.fab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dashlane.design.component.Badge
import com.dashlane.design.component.BadgeIcon
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.BooleanProvider
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.commoncomposecomponents.R
import kotlinx.coroutines.delay

private const val ANIMATION_DURATION_MS = 250

@Composable
fun MultiActionsFloatingActionButton(
    modifier: Modifier = Modifier,
    fabMenu: FabItem.Menu,
    onPrimaryFabClick: () -> Unit,
    onActionFabClick: (FabItem.Action) -> Unit,
    isExpanded: Boolean,
) {
    var currentSubActions: List<FabItem> by remember { mutableStateOf(fabMenu.items) }

    LaunchedEffect(fabMenu, isExpanded) {
        currentSubActions = fabMenu.items
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        
        FabActionListItem(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            showSubActions = isExpanded,
            subActions = currentSubActions,
        ) { selectedItem ->
            when (selectedItem) {
                is FabItem.Action -> onActionFabClick(selectedItem)
                is FabItem.Menu -> currentSubActions = selectedItem.items
            }
        }
        
        PrimaryFab(
            item = fabMenu,
            isExpanded = isExpanded,
            onClick = {
                onPrimaryFabClick()
            },
        )
    }
}

@Composable
private fun PrimaryFab(
    modifier: Modifier = Modifier,
    item: FabItem,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val transition = updateTransition(targetState = isExpanded, label = "ExpandFab")
    val fabColor by transition.animateColor(label = "border color") { isFabOpened ->
        if (isFabOpened) {
            DashlaneTheme.colors.containerExpressiveNeutralCatchyIdle
        } else {
            DashlaneTheme.colors.containerExpressiveBrandCatchyIdle
        }
    }

    ExtendedFloatingActionButton(
        modifier = modifier,
        text = {
            Text(
                text = stringResource(item.textRes).uppercase(),
                color = DashlaneTheme.colors.textInverseCatchy,
                style = DashlaneTheme.typography.componentButtonStandard.copy(fontSize = 16.sp),
            )
        },
        icon = {
            Crossfade(targetState = isExpanded) { isFabOpened ->
                if (isFabOpened) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        token = IconTokens.actionCloseOutlined,
                        contentDescription = null,
                        tint = DashlaneTheme.colors.textInverseCatchy,
                    )
                } else {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        token = item.iconToken,
                        contentDescription = null,
                        tint = DashlaneTheme.colors.textInverseCatchy,
                    )
                }
            }
        },
        onClick = onClick,
        expanded = !isExpanded,
        shape = RoundedCornerShape(32.dp),
        containerColor = fabColor,
    )
}

@Composable
private fun FabActionListItem(
    modifier: Modifier = Modifier,
    showSubActions: Boolean,
    subActions: List<FabItem>,
    onItemClick: (FabItem) -> Unit,
) {
    var displayedSubactions by remember { mutableStateOf(emptyList<FabItem>()) }
    var isAnimating: Boolean by remember { mutableStateOf(false) }
    var isVisible: Boolean by remember { mutableStateOf(showSubActions) }

    LaunchedEffect(subActions, showSubActions) {
        isAnimating = true
        
        if (displayedSubactions != subActions && displayedSubactions.isNotEmpty()) {
            isVisible = false
            delay(ANIMATION_DURATION_MS.toLong())
        }
        
        displayedSubactions = subActions
        isVisible = true
        delay(ANIMATION_DURATION_MS.toLong())
        isAnimating = false
    }
    if (showSubActions) {
        AnimatedVisibility(
            modifier = modifier,
            visible = isVisible,
            enter = fadeIn(tween(durationMillis = ANIMATION_DURATION_MS)),
            exit = fadeOut(tween(durationMillis = ANIMATION_DURATION_MS)),
        ) {
            LazyColumn(
                modifier = modifier.padding(end = 4.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(
                    space = 8.dp,
                    alignment = Alignment.Bottom,
                ),
            ) {
                items(
                    items = displayedSubactions,
                    key = { it.hashCode() },
                ) { item ->
                    FabItem(
                        text = stringResource(item.textRes),
                        token = item.iconToken,
                        badgeSlot = {
                            item.badgeContent?.let {
                                Badge(
                                    text = stringResource(it.textRes),
                                    modifier = Modifier,
                                    badgeIcon = it.icon,
                                    mood = it.mood,
                                    intensity = it.intensity,
                                )
                            }
                        },
                        onClick = {
                            if (!isAnimating) {
                                onItemClick(item)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun MultiActionsFloatingActionButtonPreview(
    @PreviewParameter(BooleanProvider::class) expand: Boolean,
) {
    DashlanePreview {
        Box(
            modifier = Modifier
                .width(480.dp)
                .height(500.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            MultiActionsFloatingActionButton(
                modifier = Modifier.padding(16.dp),
                fabMenu = FabItem.Menu(
                    items = listOf(
                        FabItem.Action(
                            textRes = R.string.facebook,
                            badgeContent = FabItem.BadgeContent(
                                textRes = R.string.ok,
                                icon = BadgeIcon.Leading(iconToken = IconTokens.premiumOutlined),
                                mood = Mood.Warning,
                                intensity = Intensity.Quiet,
                            ),
                            iconToken = IconTokens.itemLoginOutlined,
                            actionKey = "",
                        ),
                        FabItem.Action(
                            textRes = R.string.reddit,
                            iconToken = IconTokens.itemLoginOutlined,
                            actionKey = "",
                        ),
                        FabItem.Menu(
                            textRes = R.string.twitter,
                            iconToken = IconTokens.itemPaymentOutlined,
                            items = listOf(
                                FabItem.Action(
                                    iconToken = IconTokens.osLinuxFilled,
                                    textRes = R.string.linkedin,
                                    actionKey = "",
                                ),
                                FabItem.Action(
                                    iconToken = IconTokens.itemBankAccountOutlined,
                                    textRes = R.string.instagram,
                                    actionKey = "",
                                ),
                            ),
                        ),
                    ),
                    textRes = R.string.reddit,
                    iconToken = IconTokens.actionAddOutlined,
                ),
                isExpanded = expand,
                onPrimaryFabClick = {},
                onActionFabClick = {},
            )
        }
    }
}