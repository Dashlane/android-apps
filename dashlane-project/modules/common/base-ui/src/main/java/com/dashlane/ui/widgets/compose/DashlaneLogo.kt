package com.dashlane.ui.widgets.compose

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.R

@Composable
fun DashlaneLogo(
    modifier: Modifier = Modifier,
    color: Color = DashlaneTheme.colors.textNeutralCatchy.value
) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.logo_lock_up),
        colorFilter = ColorFilter.tint(color),
        contentDescription = stringResource(id = R.string.dashlane_main_app_name)
    )
}