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
import com.dashlane.item.v3.viewmodels.State

internal fun ComposeView.setupToolbar(state: State, isNameVisible: Boolean) = setContent {
    DashlaneTheme {
        if (state.isEditMode) {
            Text(
                text = if (state.isNew) {
                    stringResource(id = R.string.action_bar_title_credential_create_step1)
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
                    text = state.formData.name,
                    modifier = Modifier.semantics { heading() },
                    style = DashlaneTheme.typography.titleSectionMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}