package com.dashlane.premium.offer.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
internal fun BenefitsList(
    modifier: Modifier = Modifier,
    warning: String?,
    benefits: List<String>,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (warning != null) {
            item {
                InfoboxMedium(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    title = warning,
                    mood = Mood.Warning
                )
            }
        }
        items(benefits) {
            BenefitRow(text = it)
        }
    }
}

@Preview
@Composable
private fun BenefitsListPreview() = DashlanePreview {
    BenefitsList(
        warning = "You have an active subscription with the Apple store, thus you cannot purchase Dashlane on the Android store at the moment",
        benefits = listOf(
            "<b>Unlimited</b> Passwords!",
            "Sync across <b>unlimited</b> active devices",
            "An autofill that is <b>always</b> working",
            "A cutting-edge VPN that <i>respects your privacy</i>",
            "Active Dark-web monitoring and alerts",
            "Our deepest thanks and unconditional respect",
            "Share passwords and Secure notes with your close ones",
            "Up to <b>1GB</b> secure storage"
        )
    )
}