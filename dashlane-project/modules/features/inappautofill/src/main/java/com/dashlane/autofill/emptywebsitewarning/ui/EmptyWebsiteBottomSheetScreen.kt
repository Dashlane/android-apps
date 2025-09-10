package com.dashlane.autofill.emptywebsitewarning.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.autofill.api.R
import com.dashlane.autofill.emptywebsitewarning.EmptyWebsiteWarningState
import com.dashlane.autofill.emptywebsitewarning.EmptyWebsiteWarningViewModel
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.ui.AutofillCard
import com.dashlane.autofill.ui.DashlaneAutofillDecoration
import com.dashlane.design.component.Badge
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.Thumbnail
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.design.component.tooling.DisplayFieldActions
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.ui.common.compose.components.StatusBarBackground

@Composable
fun EmptyWebsiteBottomSheetScreen(
    viewModel: EmptyWebsiteWarningViewModel,
    onAddWebsiteSuccess: (String, AuthentifiantItemToFill) -> Unit,
    onAddWebsiteError: () -> Unit,
    onCancel: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                EmptyWebsiteWarningState.SideEffect.OnCancel -> {
                    onCancel()
                }
                is EmptyWebsiteWarningState.SideEffect.OnSuccess -> {
                    onAddWebsiteSuccess(sideEffect.message, sideEffect.itemToFill)
                }
                is EmptyWebsiteWarningState.SideEffect.OnError -> {
                    onAddWebsiteError()
                }
            }
        }
    }
    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    when (val viewState = uiState) {
        EmptyWebsiteWarningState.View.Initial -> {
            
        }
        is EmptyWebsiteWarningState.View.Loaded -> {
            Box {
                StatusBarBackground()
                EmptyWebsiteWarningBottomSheet(
                    credentialTitle = viewState.viewData.credentialTitle,
                    credentialLogin = viewState.viewData.credentialLogin,
                    websiteToAdd = viewState.viewData.websiteToAdd,
                    onAddWebsiteClick = viewModel::onAddWebsiteClick,
                    onDismissOrCancel = viewModel::onDismissOrCancel,
                )
            }
        }
        EmptyWebsiteWarningState.View.Hidden -> {
            
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptyWebsiteWarningBottomSheet(
    credentialTitle: String,
    credentialLogin: String,
    websiteToAdd: String,
    onAddWebsiteClick: () -> Unit,
    onDismissOrCancel: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(),
        onDismissRequest = onDismissOrCancel,
    ) {
        EmptyWebsiteWarningBottomSheetContent(
            credentialTitle = credentialTitle,
            credentialLogin = credentialLogin,
            websiteToAdd = websiteToAdd,
            onAddWebsiteClick = onAddWebsiteClick,
            onCancel = onDismissOrCancel,
        )
    }
}

@Composable
private fun EmptyWebsiteWarningBottomSheetContent(
    credentialTitle: String,
    credentialLogin: String,
    websiteToAdd: String,
    onAddWebsiteClick: () -> Unit,
    onCancel: () -> Unit,
) {
    BottomSheetContent(
        decoration = {
            DashlaneAutofillDecoration()
        },
        title = stringResource(R.string.autofill_empty_website_title_v2),
        description = stringResource(R.string.autofill_empty_website_description),
        extraContent = {
            EmptyWebsiteWarningExtraContent(
                credentialTitle = credentialTitle,
                credentialLogin = credentialLogin,
                websiteToAdd = websiteToAdd,
            )
        },
        actions = BottomSheetActions(
            primaryButtonText = stringResource(R.string.autofill_empty_website_cta_positive),
            secondaryButtonText = stringResource(R.string.autofill_empty_website_cta_negative),
            onPrimaryButtonClick = onAddWebsiteClick,
            onSecondaryButtonClick = onCancel,
        ),
    )
}

@Composable
private fun EmptyWebsiteWarningExtraContent(
    credentialTitle: String,
    credentialLogin: String,
    websiteToAdd: String,
) {
    AutofillCard(
        listItemHeader = {
            ListItem(
                thumbnail = {
                    val thumbnailDescription = stringResource(R.string.datatype_authentifiant)
                    Thumbnail(
                        modifier = Modifier.semantics { contentDescription = thumbnailDescription },
                        type = ThumbnailType.VaultItem.DomainIcon(),
                    )
                },
                title = credentialTitle,
                description = credentialLogin,
                onClick = null,
            )
        },
    ) {
        DisplayField(
            modifier = Modifier.padding(horizontal = 16.dp),
            label = stringResource(R.string.autofill_empty_website_tag_website_label),
            value = websiteToAdd,
            actions = object : DisplayFieldActions {
                @Composable
                override fun Content() {
                    Badge(
                        text = stringResource(R.string.autofill_empty_website_tag_link_website),
                        mood = Mood.Positive,
                        intensity = Intensity.Quiet,
                    )
                }

                override fun getCustomAccessibilityActions(): List<CustomAccessibilityAction> {
                    return emptyList()
                }
            },
        )
    }
}

@Preview
@Composable
private fun EmptyWebsiteBottomSheetContentPreview() {
    DashlaneModalBottomSheetPreview {
        CompositionLocalProvider(
            LocalInspectionMode provides true,
        ) {
            EmptyWebsiteWarningBottomSheetContent(
                credentialTitle = "Discord",
                credentialLogin = "randomemail@provider.com",
                websiteToAdd = "discord.com",
                onAddWebsiteClick = {},
                onCancel = {},
            )
        }
    }
}