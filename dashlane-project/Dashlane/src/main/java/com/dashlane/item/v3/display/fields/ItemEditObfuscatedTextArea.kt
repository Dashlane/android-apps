package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ObfuscatedDisplayField
import com.dashlane.design.component.ObfuscatedTextArea
import com.dashlane.design.component.ObfuscatedVisualTransformation
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.component.tooling.TextFieldActions
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme

@Suppress(
    
    "kotlin:S107",
)
@Composable
internal fun ItemEditObfuscatedTextArea(
    label: String,
    value: String,
    isEmpty: Boolean,
    onValueChange: (String) -> Unit,
    onHideReveal: () -> Unit,
    onCopy: (() -> Unit)? = null,
    readOnly: Boolean,
    obfuscated: Boolean,
    editMode: Boolean,
    isRequired: Boolean = false,
    fixedHiddenValueLength: Int? = null,
    readModeValueTransformation: ((String?) -> String?)? = null,
    editModeValueTransformation: ObfuscatedVisualTransformation = ObfuscatedVisualTransformation.newDefault(),
    
    keyboardType: KeyboardType = KeyboardType.Password,
    imeAction: ImeAction = ImeAction.Unspecified,
) {
    val hideRevealAction = FieldAction.HideReveal(
        resources = LocalContext.current.resources,
        onClick = {
            onHideReveal()
            true
        },
    )
    if (editMode) {
        ObfuscatedTextArea(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            isRequired = isRequired,
            label = label,
            obfuscated = obfuscated,
            actions = TextFieldActions.Obfuscated(
                hideRevealAction = hideRevealAction,
                genericAction = null,
            ),
            obfuscatedVisualTransformation = editModeValueTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
        )
    } else {
        ObfuscatedDisplayField(
            label = label,
            isEmpty = isEmpty,
            revealedValue = value,
            fixedHiddenValueLength = fixedHiddenValueLength,
            obfuscated = obfuscated,
            action1 = hideRevealAction,
            action2 = FieldAction.Generic(
                iconLayout = ButtonLayout.IconOnly(
                    iconToken = IconTokens.actionCopyOutlined,
                    contentDescription = stringResource(R.string.copy),
                ),
                onClick = {
                    onCopy?.invoke()
                    true
                },
            ),
            transformation = readModeValueTransformation,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemEditObfuscatedTextAreaViewModePreview() = DashlaneTheme {
    ItemEditObfuscatedTextArea(
        label = "Label",
        value = "Value",
        isEmpty = false,
        editMode = false,
        obfuscated = true,
        readOnly = false,
        onHideReveal = {},
        onValueChange = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun ItemEditObfuscatedTextAreaEditModePreview() = DashlaneTheme {
    ItemEditObfuscatedTextArea(
        label = "Label",
        value = "Value",
        isEmpty = false,
        editMode = true,
        obfuscated = true,
        readOnly = false,
        onHideReveal = {},
        onValueChange = {},
    )
}