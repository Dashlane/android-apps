@file:OptIn(ExperimentalMaterial3Api::class)

package com.dashlane.ui.widgets.view

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.component.Icon
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.R

private val defaultHandleColor = Color(0xFF4286F4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionSearchField(prompt: String, onPromptChange: (String) -> Unit, modifier: Modifier = Modifier) {
    DashlaneTheme {
        OutlinedTextField(
            value = prompt,
            onValueChange = {
                onPromptChange(it)
            },
            label = { Text(text = stringResource(id = R.string.collection_search_field_label)) },
            placeholder = { Text(text = stringResource(id = R.string.collection_search_field_placeholder)) },
            trailingIcon = if (prompt.isNotEmpty()) {
                {
                    IconButton(onClick = { onPromptChange("") }) {
                        Icon(
                            token = IconTokens.actionClearContentFilled,
                            contentDescription = null,
                            tint = DashlaneTheme.colors.textBrandStandard
                        )
                    }
                }
            } else {
                null
            },
            modifier = modifier,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = DashlaneTheme.colors.textNeutralStandard.value,
                selectionColors = TextSelectionColors(
                    handleColor = defaultHandleColor,
                    backgroundColor = defaultHandleColor.copy(alpha = 0.4f)
                ),
                cursorColor = DashlaneTheme.colors.textBrandQuiet.value,
                focusedBorderColor = DashlaneTheme.colors.borderBrandStandardActive,
                unfocusedBorderColor = DashlaneTheme.colors.borderNeutralQuietIdle,
                focusedLabelColor = DashlaneTheme.colors.textBrandQuiet.value,
                unfocusedLabelColor = DashlaneTheme.colors.textNeutralQuiet.value,
            )
        )
    }
}

@Preview
@Composable
fun CollectionSearchFieldPreview() {
    DashlanePreview {
        CollectionSearchField("", {})
    }
}