package com.dashlane.premium.offer.details.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonLarge
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ActionBottomBar(
    monthlyButton: PurchaseButtonState?,
    yearlyButton: PurchaseButtonState?,
    extraActionButtonState: ExtraActionButtonState? = null
) {
    val portrait =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    val buttonsContent = @Composable {
        val buttonModifier = if (portrait) {
            Modifier.fillMaxWidth()
        } else {
            Modifier
        }
        monthlyButton?.let {
            PurchaseButton(
                modifier = buttonModifier,
                state = it
            )
        }
        yearlyButton?.let {
            PurchaseButton(
                modifier = buttonModifier,
                state = it
            )
        }
        extraActionButtonState?.let {
            ButtonLarge(
                modifier = buttonModifier,
                mood = Mood.Brand,
                intensity = Intensity.Quiet,
                layout = ButtonLayout.TextOnly(text = extraActionButtonState.ctaString),
                onClick = extraActionButtonState.onClick,
            )
        }
    }
    if (portrait) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            buttonsContent()
        }
    } else {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2
        ) {
            buttonsContent()
        }
    }
}

internal data class ExtraActionButtonState(
    val ctaString: String,
    val onClick: () -> Unit
)

@Preview
@Preview(device = Devices.TABLET)
@Composable
private fun PurchaseBottomBarPreview() = DashlanePreview {
    Box(modifier = Modifier.padding(8.dp)) {
        ActionBottomBar(
            monthlyButton = PurchaseButtonState(
                enabled = true,
                ctaString = "$3 for 1 month",
                isPrimary = false,
                onClick = {}
            ),
            yearlyButton = PurchaseButtonState(
                enabled = true,
                ctaString = "$30 for 1 month",
                isPrimary = true,
                onClick = {},
                priceInfoString = "or $2.5 per month"
            ),
            extraActionButtonState = ExtraActionButtonState(ctaString = "Extra action") {}
        )
    }
}