package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.PasswordField
import com.dashlane.design.component.PasswordFieldFeedback
import com.dashlane.design.component.UiPasswordStrength
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.utils.passwordFieldActions
import com.dashlane.ui.commoncomposecomponents.R

@SuppressWarnings("kotlin:S107") 
@Composable
fun ColumnScope.CreatePasswordContent(
    passwordFieldLabel: String,
    passwordFieldFeedback: String,
    confirmPasswordFieldLabel: String,
    password: TextFieldValue,
    confirmPassword: TextFieldValue,
    isConfirming: Boolean,
    isMatching: Boolean,
    passwordStrength: UiPasswordStrength?,
    onFocusChanged: () -> Unit,
    onPasswordChanged: (TextFieldValue) -> Unit,
    onConfirmPasswordChanged: (TextFieldValue) -> Unit,
    onNextClicked: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val modifierWithoutFocus = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
    val modifierWithFocus = modifierWithoutFocus
        .focusRequester(focusRequester)
        .onGloballyPositioned { focusRequester.requestFocus() }
    val interactionSource = remember { MutableInteractionSource() }
        .also { interactionSource ->
            LaunchedEffect(interactionSource) {
                interactionSource.interactions.collect {
                    if (it is PressInteraction.Release) onFocusChanged()
                }
            }
        }

    val newMPObfuscatedState = rememberSaveable { mutableStateOf(true) }
    PasswordField(
        modifier = if (isConfirming) modifierWithoutFocus else modifierWithFocus,
        value = password,
        onValueChange = onPasswordChanged,
        label = passwordFieldLabel,
        readOnly = isConfirming,
        obfuscated = newMPObfuscatedState.value,
        actions = passwordFieldActions(obfuscatedState = newMPObfuscatedState),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { onNextClicked() }),
        feedback = passwordStrength
            ?.let { PasswordFieldFeedback.Strength(it) }
            ?: PasswordFieldFeedback.Text(passwordFieldFeedback),
        interactionSource = interactionSource,
    )
    if (isConfirming) {
        val confirmationObfuscatedState = rememberSaveable { mutableStateOf(true) }
        PasswordField(
            modifier = modifierWithFocus,
            value = confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            label = confirmPasswordFieldLabel,
            obfuscated = confirmationObfuscatedState.value,
            actions = passwordFieldActions(obfuscatedState = confirmationObfuscatedState),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = if (isMatching) KeyboardActions(onDone = { onNextClicked() }) else KeyboardActions.Default,
            feedback = confirmPasswordFeedback(confirmPassword.text, isMatching),
            isError = !isMatching && confirmPassword.text.isNotEmpty(),
        )
    }
}

@Composable
private fun confirmPasswordFeedback(
    confirmPassword: String,
    isMatching: Boolean,
): PasswordFieldFeedback? = when {
    confirmPassword.isEmpty() -> null
    isMatching -> PasswordFieldFeedback.Text(stringResource(id = R.string.change_master_password_confirm_text_edit_feedback_matching))
    else -> PasswordFieldFeedback.Text(stringResource(id = R.string.change_master_password_confirm_text_edit_feedback_error))
}

@Preview
@Composable
private fun CreatePasswordContentPreview() {
    DashlanePreview {
        Column {
            CreatePasswordContent(
                passwordFieldLabel = "Password",
                passwordFieldFeedback = "Password feedback",
                confirmPasswordFieldLabel = "Confirm password",
                password = TextFieldValue("password"),
                confirmPassword = TextFieldValue("password"),
                isConfirming = true,
                isMatching = true,
                passwordStrength = UiPasswordStrength.GOOD,
                onFocusChanged = {},
                onPasswordChanged = {},
                onConfirmPasswordChanged = {},
                onNextClicked = {},
            )
        }
    }
}