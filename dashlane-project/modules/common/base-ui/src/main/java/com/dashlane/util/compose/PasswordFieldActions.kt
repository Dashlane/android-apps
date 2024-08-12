package com.dashlane.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.component.tooling.TextFieldActions
import com.dashlane.ui.R

@Composable
fun passwordFieldActions(obfuscatedState: MutableState<Boolean>): TextFieldActions.Password = TextFieldActions.Password(
    hideRevealAction = FieldAction.HideReveal(
        contentDescriptionToHide = stringResource(id = R.string.and_accessibility_text_edit_hide),
        contentDescriptionToReveal = stringResource(id = R.string.and_accessibility_text_edit_reveal),
        onClick = {
            obfuscatedState.value = !obfuscatedState.value
            true
        },
    ),
    genericAction = null,
    passwordGeneratorAction = null
)