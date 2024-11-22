package com.dashlane.item.v3.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.dashlane.R
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.viewmodels.ItemEditState

internal fun ComposeView.setupToolbar(state: ItemEditState<out FormData>, isNameVisible: Boolean) = setContent {
    DashlaneTheme {
        if (state.isEditMode) {
            Text(
                text = if (state.isNew) {
                    when (state.datas?.current?.formData) {
                        is CredentialFormData -> {
                            stringResource(id = R.string.action_bar_title_credential_create_step1)
                        }
                        is SecureNoteFormData -> stringResource(id = R.string.vault_toolbar_secure_note_create_title)
                        is SecretFormData -> stringResource(id = R.string.vault_toolbar_secret_create_title)
                        else -> {
                            error("Unhandled form data type. Set a title for adding items of this type")
                        }
                    }
                } else {
                    stringResource(id = R.string.edit)
                },
                modifier = Modifier.semantics { heading() },
                style = DashlaneTheme.typography.titleSectionMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            AnimatedVisibility(
                visible = isNameVisible,
                enter = fadeIn(animationSpec = tween(1000)),
                exit = fadeOut(animationSpec = tween(1000))
            ) {
                Text(
                    text = state.datas?.current?.commonData?.name ?: "",
                    modifier = Modifier.semantics { heading() },
                    style = DashlaneTheme.typography.titleSectionMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}