package com.dashlane.premium.offer.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonLarge
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
internal fun PurchaseButton(
    modifier: Modifier = Modifier,
    state: PurchaseButtonState
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ButtonLarge(
            modifier = modifier,
            layout = ButtonLayout.TextOnly(text = state.ctaString),
            onClick = state.onClick,
            mood = Mood.Brand,
            intensity = if (state.isPrimary) {
                Intensity.Catchy
            } else {
                Intensity.Quiet
            },
            enabled = state.enabled
        )

        if (state.priceInfoString != null) {
            Text(
                modifier = modifier,
                text = state.priceInfoString,
                style = DashlaneTheme.typography.bodyHelperRegular,
                color = DashlaneTheme.colors.textNeutralQuiet,
                textAlign = TextAlign.Center
            )
        }
    }
}

internal data class PurchaseButtonState(
    val enabled: Boolean,
    val ctaString: String,
    val priceInfoString: String? = null,
    val isPrimary: Boolean,
    val onClick: () -> Unit
)

@Preview
@Composable
private fun PurchaseButtonPreview() = DashlanePreview {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        PurchaseButton(
            state = PurchaseButtonState(
                enabled = true,
                ctaString = "5.49€ for 1 month",
                priceInfoString = "Or 4.42€ per month",
                isPrimary = false,
            ) {}
        )
        PurchaseButton(
            state = PurchaseButtonState(
                enabled = true,
                ctaString = "52.99€ for 12 months",
                priceInfoString = "Or 4.42€ per month",
                isPrimary = true
            ) {}
        )
    }
}