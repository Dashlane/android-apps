package com.dashlane.ui.common.compose.components.fab

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Badge
import com.dashlane.design.component.BadgeIcon
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview

sealed class FabItem {
    abstract val textRes: Int
    abstract val iconToken: IconToken
    abstract val badgeContent: BadgeContent?

    data class Action(
        @StringRes override val textRes: Int,
        override val iconToken: IconToken,
        override val badgeContent: BadgeContent? = null,
        val actionKey: Any,
    ) : FabItem()

    data class Menu(
        @StringRes override val textRes: Int,
        override val iconToken: IconToken,
        override val badgeContent: BadgeContent? = null,
        val items: List<FabItem>,
    ) : FabItem()

    data class BadgeContent(
        @StringRes val textRes: Int,
        val icon: BadgeIcon,
        val mood: Mood,
        val intensity: Intensity,
    )
}

@Composable
fun FabItem(
    text: String,
    token: IconToken,
    modifier: Modifier = Modifier,
    badgeSlot: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(4.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .clickable(
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
            ) { onClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = DashlaneTheme.colors.containerAgnosticNeutralStandard,
                    shape = RoundedCornerShape(4.dp),
                )
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(modifier = Modifier.padding(vertical = 4.dp), text = text)

            badgeSlot?.invoke()
        }
        Icon(
            token = token,
            tint = DashlaneTheme.colors.textInverseCatchy,
            contentDescription = null,
            modifier = Modifier
                .background(
                    shape = CircleShape,
                    color = DashlaneTheme.colors.containerExpressiveBrandCatchyIdle,
                )
                .clip(CircleShape)
                .padding(8.dp),
        )
    }
}

@Composable
@Preview
private fun FabActionItemPreview() {
    DashlanePreview {
        Column(
            modifier = Modifier
                .background(color = DashlaneTheme.colors.containerDecorativeBlack)
                .padding(16.dp),
        ) {
            FabItem(
                text = "Login",
                token = IconTokens.itemLoginOutlined,
                onClick = {},
            )
            FabItem(
                text = "Login",
                token = IconTokens.itemLoginOutlined,
                badgeSlot = {
                    Badge(
                        modifier = Modifier,
                        text = "Upgrade",
                        badgeIcon = BadgeIcon.Leading(iconToken = IconTokens.premiumOutlined),
                        mood = Mood.Warning,
                        intensity = Intensity.Quiet,
                    )
                },
                onClick = {},
            )
        }
    }
}