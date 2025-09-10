package com.dashlane.autofill.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.autofill.api.R
import com.dashlane.design.component.ExpressiveIcon
import com.dashlane.design.component.ExpressiveIconSize
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
internal fun DashlaneAutofillDecoration() {
    val contentDescription = stringResource(R.string.dashlane_main_app_name)
    ExpressiveIcon(
        modifier = Modifier.semantics { this.contentDescription = contentDescription },
        icon = IconToken(R.drawable.vd_logo_dashlane_micro_logomark),
        size = ExpressiveIconSize.Medium,
        mood = Mood.Brand,
    )
}

@Preview
@Composable
private fun DashlaneAutofillDecorationPreview() {
    DashlanePreview {
        DashlaneAutofillDecoration()
    }
}