package com.dashlane.ui.menu.view.teamspace

import android.widget.ImageView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.dashlane.R
import com.dashlane.design.component.Icon
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.teamspaces.adapter.TeamspaceIconDrawable
import com.dashlane.teamspaces.model.SpaceColor
import com.dashlane.ui.menu.domain.TeamspaceIcon

@Composable
fun MenuTeamspaceIcon(modifier: Modifier = Modifier, icon: TeamspaceIcon) {
    when (icon) {
        TeamspaceIcon.Combined -> {
            Icon(
                modifier = modifier,
                token = IconTokens.spacesAllOutlined,
                tint = DashlaneTheme.colors.textBrandStandard,
                contentDescription = stringResource(id = R.string.and_accessibility_user_profile_icon),
            )
        }
        is TeamspaceIcon.Space -> {
            val colorInt = when (val color = icon.spaceColor) {
                is SpaceColor.FixColor -> colorResource(color.colorRes).toArgb()
                is SpaceColor.TeamColor -> color.color
            }
            AndroidView(
                
                
                
                modifier = modifier
                    .aspectRatio(1f)
                    .fillMaxSize()
                    .wrapContentSize(align = Alignment.Center)
                    .fillMaxSize(0.75f),
                factory = { context ->
                    val drawable = TeamspaceIconDrawable(context, icon.displayLetter, colorInt)
                    ImageView(context).apply {
                        setImageDrawable(drawable)
                    }
                },
                update = { imageView ->
                    val drawable = TeamspaceIconDrawable(imageView.context, icon.displayLetter, colorInt)
                    imageView.setImageDrawable(drawable)
                }
            )
        }
    }
}