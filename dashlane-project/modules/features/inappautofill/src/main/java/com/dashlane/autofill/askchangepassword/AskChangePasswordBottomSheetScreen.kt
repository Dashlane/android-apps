package com.dashlane.autofill.askchangepassword

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.autofill.api.R
import com.dashlane.autofill.ui.AutofillCard
import com.dashlane.autofill.ui.DashlaneAutofillDecoration
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.ui.common.compose.components.StatusBarBackground
import com.dashlane.ui.thumbnail.ThumbnailDomainIcon

@Composable
internal fun AskChangePasswordBottomSheetScreen(
    credentialThumbnailUrl: String?,
    credentialTitle: String,
    credentialLogin: String,
    onChangePasswordClick: () -> Unit,
    onDismissOrCancel: () -> Unit,
) {
    Box {
        StatusBarBackground()
        AskChangePasswordBottomSheet(
            credentialThumbnailUrl = credentialThumbnailUrl,
            credentialTitle = credentialTitle,
            credentialLogin = credentialLogin,
            onChangePasswordClick = onChangePasswordClick,
            onDismissOrCancel = onDismissOrCancel,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AskChangePasswordBottomSheet(
    credentialThumbnailUrl: String?,
    credentialTitle: String,
    credentialLogin: String,
    onChangePasswordClick: () -> Unit,
    onDismissOrCancel: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(),
        onDismissRequest = onDismissOrCancel,
    ) {
        AskChangePasswordBottomSheetContent(
            credentialUrl = credentialThumbnailUrl,
            credentialTitle = credentialTitle,
            credentialLogin = credentialLogin,
            onChangePasswordClick = onChangePasswordClick,
            onDismissOrCancel = onDismissOrCancel,
        )
    }
}

@Composable
private fun AskChangePasswordBottomSheetContent(
    credentialUrl: String?,
    credentialTitle: String,
    credentialLogin: String,
    onChangePasswordClick: () -> Unit,
    onDismissOrCancel: () -> Unit,
) {
    BottomSheetContent(
        decoration = {
            DashlaneAutofillDecoration()
        },
        title = stringResource(R.string.autofill_change_password_ask_dialog_title),
        description = stringResource(R.string.autofill_change_password_ask_dialog_description),
        extraContent = {
            AutofillCard(
                listItemHeader = {
                    ListItem(
                        thumbnail = {
                            val thumbnailDescription = stringResource(R.string.datatype_authentifiant)
                            ThumbnailDomainIcon(
                                modifier = Modifier.semantics { contentDescription = thumbnailDescription },
                                urlDomain = credentialUrl,
                            )
                        },
                        title = credentialTitle,
                        description = credentialLogin,
                        onClick = null,
                    )
                },
                content = null,
            )
        },
        actions = BottomSheetActions(
            primaryButtonLayout = ButtonLayout.TextOnly(stringResource(R.string.autofill_change_password_ask_dialog_positive_button)),
            onPrimaryButtonClick = onChangePasswordClick,
            secondaryButtonLayout = ButtonLayout.TextOnly(stringResource(R.string.autofill_change_password_ask_dialog_negative_button)),
            onSecondaryButtonClick = onDismissOrCancel,
        ),
    )
}

@Preview
@Composable
private fun AskChangePasswordBottomSheetContentPreview() {
    DashlaneModalBottomSheetPreview {
        AskChangePasswordBottomSheetContent(
            credentialUrl = "discord.com",
            credentialTitle = "discord.com",
            credentialLogin = "randomemail@provider.com",
            onChangePasswordClick = {},
            onDismissOrCancel = {},
        )
    }
}