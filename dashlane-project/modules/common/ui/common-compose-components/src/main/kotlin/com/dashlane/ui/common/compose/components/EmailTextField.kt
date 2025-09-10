package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.TextField
import com.dashlane.design.component.tooling.TextFieldActions
import com.dashlane.ui.commoncomposecomponents.R
import com.dashlane.util.isNotSemanticallyNull

@Composable
fun EmailTextField(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String? = null,
    email: TextFieldValue,
    error: String?,
    onEmailChange: (TextFieldValue) -> Unit,
    onClickContinue: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldLoaded by remember { mutableStateOf(false) }

    TextField(
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onGloballyPositioned {
                if (!textFieldLoaded) {
                    focusRequester.requestFocus()
                    textFieldLoaded = true
                }
            }
            .padding(top = 16.dp, bottom = 16.dp),
        value = email,
        onValueChange = onEmailChange,
        placeholder = placeholder,
        label = label,
        isError = error.isNotSemanticallyNull(),
        feedbackText = error,
        disablePersonalizedLearning = false,
        actions = TextFieldActions.ClearField(stringResource(id = R.string.and_accessibility_action_text_clear)) {
            onEmailChange(TextFieldValue(""))
            true
        },
        keyboardActions = KeyboardActions(
            onDone = { onClickContinue() },
        ),
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
        ),
    )
}