package com.dashlane.wear.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.LocalDashlaneMood
import com.dashlane.design.theme.color.LocalDashlaneTextColor
import com.dashlane.design.theme.color.TextColor

@Composable
fun WearIcon(
    token: IconToken,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = DashlaneTheme.colors.textInverseCatchy.value,
) = androidx.wear.compose.material.Icon(
    painter = painterResource(id = token.resource),
    contentDescription = contentDescription,
    modifier = modifier,
    tint = tint,
)

@Composable
fun WearIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Black,
) = androidx.wear.compose.material.Icon(
    painter = painter,
    contentDescription = contentDescription,
    modifier = modifier,
    tint = tint,
)