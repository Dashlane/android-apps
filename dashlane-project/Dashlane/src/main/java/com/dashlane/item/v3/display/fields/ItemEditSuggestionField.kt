package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.component.SuggestionField
import com.dashlane.design.theme.tooling.DashlanePreview

@Suppress("kotlin:S107")
@Composable
fun ItemEditSuggestionField(
    label: String,
    data: String?,
    editMode: Boolean,
    options: List<String> = emptyList(),
    isEditable: Boolean = true,
    
    keyboardType: KeyboardType = KeyboardType.Password,
    imeAction: ImeAction = ImeAction.Unspecified,
    onValueChanged: (String) -> Unit,
    onValueCopy: (() -> Unit)? = null,
    onValueOpen: (() -> Unit)? = null,
    feedbackText: String? = null,
    isError: Boolean = false,
    isRequired: Boolean = false,
) {
    if (editMode) {
        var isExpanded by remember { mutableStateOf(false) }
        SuggestionField(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            modifier = Modifier.fillMaxWidth(),
            value = data ?: "",
            label = label,
            options = options,
            enabled = true,
            readOnly = isEditable.not(),
            labelPersists = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            onValueChange = onValueChanged,
            feedbackText = feedbackText,
            isError = isError,
            isRequired = isRequired,
        )
    } else {
        ItemEditGenericDisplayField(
            label = label,
            data = data,
            onValueCopy = onValueCopy,
            onValueOpen = onValueOpen,
        )
    }
}

@Preview
@Composable
private fun SuggestionFieldViewPreview() = DashlanePreview {
    ItemEditSuggestionField(
        label = "Label",
        data = "Edit Mode",
        editMode = false,
        onValueChanged = {},
    )
}

@Preview
@Composable
private fun SuggestionFieldEditPreview() = DashlanePreview {
    ItemEditSuggestionField(
        label = "Label",
        data = "View Mode",
        editMode = true,
        onValueChanged = {},
    )
}