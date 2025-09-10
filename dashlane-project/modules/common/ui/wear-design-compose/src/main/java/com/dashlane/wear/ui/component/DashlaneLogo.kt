package com.dashlane.wear.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.wear.design.compose.R
import com.dashlane.wear.ui.DashlaneWearPreview

@Composable
fun DashlaneLogo() {
    WearIcon(
        painter = painterResource(R.drawable.vd_logo_dashlane_micro_logomark),
        tint = DashlaneTheme.colors.oddityBrand,
        contentDescription = null,
    )
}

@Composable
@Preview
private fun DashlaneLogoPreview() {
    DashlaneWearPreview {
        DashlaneLogo()
    }
}