package com.dashlane.autofill.linkservices.rememberaccount.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.autofill.api.R
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.autofill.linkservices.rememberaccount.RememberAccountState.View.Loaded
import com.dashlane.autofill.pause.model.AutofillFormSourceIcon
import com.dashlane.autofill.pause.view.AutofillFormSourceThumbnail
import com.dashlane.autofill.ui.AutofillCard
import com.dashlane.autofill.ui.DashlaneAutofillDecoration
import com.dashlane.design.component.Badge
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.tooling.fromHtml
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.StatusBarBackground
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.model.urlDomain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RememberAccountBottomSheet(
    state: Loaded,
    onAcceptServiceLink: () -> Unit,
    onRefuseServiceLink: () -> Unit,
    onDismiss: () -> Unit,
) {
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = false,
    )
    Box {
        StatusBarBackground()
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
        ) {
            RememberAccountContent(
                state = state,
                onAcceptServiceLink = onRefuseServiceLink,
                onRefuseServiceLink = onAcceptServiceLink,
            )
        }
    }
}

@Composable
private fun RememberAccountContent(
    state: Loaded,
    onAcceptServiceLink: () -> Unit,
    onRefuseServiceLink: () -> Unit,
) {
    BottomSheetContent(
        decoration = {
            DashlaneAutofillDecoration()
        },
        title = state.title,
        description = state.description.fromHtml(DashlaneTheme.typography.bodyStandardRegular),
        extraContent = {
            LinkServiceContent(
                serviceIcon = state.itemIcon,
                itemName = state.itemName,
                itemDescription = state.itemDescription,
                linkServiceIcon = state.linkServiceIcon,
                linkServiceName = state.linkServiceName,
                linkServiceBadge = state.linkServiceBadge,
            )
        },
        actions = BottomSheetActions(
            primaryButtonText = state.primaryCtaText,
            onPrimaryButtonClick = onAcceptServiceLink,
            secondaryButtonText = stringResource(id = R.string.autofill_cancel),
            onSecondaryButtonClick = onRefuseServiceLink,
        ),
    )
}

@Composable
private fun LinkServiceContent(
    serviceIcon: AutofillFormSourceIcon,
    itemName: String,
    itemDescription: String?,
    linkServiceIcon: AutofillFormSourceIcon,
    linkServiceName: String,
    linkServiceBadge: String,
) {
    AutofillCard(
        listItemHeader = {
            ListItem(
                thumbnail = { AutofillFormSourceThumbnail(serviceIcon) },
                title = itemName,
                description = itemDescription,
                onClick = null,
            )
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ListItem(
                modifier = Modifier.weight(1f),
                thumbnail = { AutofillFormSourceThumbnail(linkServiceIcon) },
                title = linkServiceName,
                description = null,
                onClick = null,
            )
            Badge(
                text = linkServiceBadge,
                mood = Mood.Positive,
                intensity = Intensity.Quiet,
            )
        }
    }
}

@Composable
@Preview
private fun AutofillLinkServiceBottomSheetAppPreview() {
    DashlanePreview {
        val discordItem = createAuthentifiant(title = "Discord", deprecatedUrl = "discord.com")
        val appFormSource = ApplicationFormSource(packageName = "tv.twitch.android.app")
        RememberAccountBottomSheet(
            state = Loaded(
                title = stringResource(R.string.autofill_link_app_title),
                description = stringResource(
                    R.string.autofill_link_app_description,
                    discordItem.urlDomain ?: "",
                    "Twitch",
                ),
                itemName = discordItem.titleForListNormalized ?: "",
                itemDescription = discordItem.loginForUi ?: "",
                itemIcon = AutofillFormSourceIcon.WebsiteUrl(url = discordItem.urlDomain ?: ""),
                formSource = appFormSource,
                linkServiceIcon = AutofillFormSourceIcon.UnknownApplication,
                linkServiceName = "Twitch",
                linkServiceBadge = stringResource(R.string.autofill_link_app),
                primaryCtaText = stringResource(R.string.autofill_link_app),
            ),
            onAcceptServiceLink = {},
            onRefuseServiceLink = {},
            onDismiss = {},
        )
    }
}

@Composable
@Preview
private fun AutofillLinkServiceBottomSheetWebsitePreview() {
    DashlanePreview {
        val discordItem = createAuthentifiant(title = "Discord", deprecatedUrl = "discord.com")
        val webFormSource = WebDomainFormSource(
            packageName = "org.mozilla.firefox",
            webDomain = "twitch.tv",
        )
        RememberAccountBottomSheet(
            state = Loaded(
                title = stringResource(R.string.autofill_link_website_title),
                description = stringResource(
                    R.string.autofill_link_website_description,
                    discordItem.urlDomain ?: "",
                    webFormSource.webDomain,
                ),
                itemIcon = AutofillFormSourceIcon.WebsiteUrl(discordItem.urlDomain ?: ""),
                itemName = discordItem.titleForListNormalized ?: "",
                itemDescription = discordItem.loginForUi ?: "",
                formSource = webFormSource,
                linkServiceIcon = AutofillFormSourceIcon.UnknownApplication,
                linkServiceName = webFormSource.webDomain,
                linkServiceBadge = stringResource(R.string.autofill_link_website),
                primaryCtaText = stringResource(R.string.autofill_link_website),
            ),
            onAcceptServiceLink = {},
            onRefuseServiceLink = {},
            onDismiss = {},
        )
    }
}