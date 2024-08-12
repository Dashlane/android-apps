package com.dashlane.ui.menu.view.item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.Badge
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.menu.domain.MenuItemModel

@Suppress("LongMethod")
@Composable
fun MenuItem(item: MenuItemModel.NavigationItem) {
    Row(
        modifier = Modifier
            .semantics { role = Role.Button }
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .wrapContentHeight()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { item.callback.invoke() }
            .then(
                if (item.isSelected) {
                    Modifier.background(color = DashlaneTheme.colors.containerExpressiveNeutralSupershyActive)
                } else {
                    Modifier
                }
            )
            .padding(all = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(24.dp)
                .padding(end = 4.dp),
            token = if (item.isSelected) item.iconTokenSelected else item.iconToken,
            contentDescription = null,
            tint = if (item.isSelected) DashlaneTheme.colors.textBrandStandard else DashlaneTheme.colors.textNeutralCatchy
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = item.titleResId),
                style = DashlaneTheme.typography.titleBlockMedium,
                color = DashlaneTheme.colors.textNeutralCatchy
            )
            when (item.premiumTag) {
                MenuItemModel.NavigationItem.PremiumTag.PremiumOnly -> {
                    Badge(
                        text = stringResource(id = R.string.menu_v3_upgrade),
                        mood = Mood.Brand,
                        intensity = Intensity.Supershy
                    )
                }
                is MenuItemModel.NavigationItem.PremiumTag.Trial -> {
                    Text(
                        text = stringResource(
                            id = R.string.menu_v3_remaining_days,
                            item.premiumTag.remainingDays
                        ),
                        style = DashlaneTheme.typography.bodyHelperRegular,
                        color = DashlaneTheme.colors.textWarningQuiet
                    )
                }
                null -> Unit
            }
        }
        @Suppress("UNUSED_EXPRESSION")
        when (item.endIcon) {
            null -> null
            is MenuItemModel.NavigationItem.EndIcon.DotNotification -> {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = DashlaneTheme.colors.textDangerQuiet.value,
                            shape = CircleShape
                        )
                )
            }
            is MenuItemModel.NavigationItem.EndIcon.NewLabel -> {
                Badge(
                    text = stringResource(id = R.string.menu_v3_new_label),
                    mood = Mood.Brand,
                    intensity = Intensity.Quiet
                )
            }
        }
    }
}

@Preview
@Composable
fun MenuItemPreview() {
    DashlanePreview {
        var isSelected: Boolean by rememberSaveable { mutableStateOf(false) }
        MenuItem(
            item = MenuItemModel.NavigationItem(
                iconToken = IconTokens.homeOutlined,
                iconTokenSelected = IconTokens.homeFilled,
                titleResId = R.string.menu_v2_home,
                isSelected = isSelected,
            ) { isSelected = !isSelected }
        )
    }
}