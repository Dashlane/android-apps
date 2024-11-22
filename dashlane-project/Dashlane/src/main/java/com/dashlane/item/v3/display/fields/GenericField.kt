package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.TextArea
import com.dashlane.design.component.TextField
import com.dashlane.design.component.tooling.DisplayFieldActions
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview

@Suppress("kotlin:S107", "LongMethod")
@Composable
fun GenericField(
    label: String,
    data: String?,
    editMode: Boolean,
    isEditable: Boolean = true,
    multiLine: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChanged: (String) -> Unit,
    onValueCopy: (() -> Unit)? = null,
    onValueOpen: (() -> Unit)? = null,
    feedbackText: String? = null,
    isError: Boolean = false
) {
    if (editMode) {
        if (multiLine) {
            TextArea(
                modifier = Modifier.fillMaxWidth(),
                value = data ?: "",
                label = label,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                readOnly = isEditable.not(),
                onValueChange = {
                    onValueChanged(it)
                },
                feedbackText = feedbackText,
                isError = isError
            )
        } else {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = data ?: "",
                label = label,
                enabled = true,
                readOnly = isEditable.not(),
                labelPersists = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                onValueChange = {
                    onValueChanged(it)
                },
                feedbackText = feedbackText,
                isError = isError
            )
        }
    } else {
        DisplayField(
            label = label,
            value = data,
            singleLine = !multiLine,
            actions = DisplayFieldActions.newInstance(
                action1 = if (onValueCopy != null) {
                    FieldAction.Generic(
                        iconLayout = ButtonLayout.IconOnly(
                            iconToken = IconTokens.actionCopyOutlined,
                            contentDescription = stringResource(id = R.string.vault_action_copy) + " " + label
                        ),
                        onClick = {
                            onValueCopy()
                            true
                        }
                    )
                } else {
                    null
                },
                action2 = if (onValueOpen != null) {
                    FieldAction.Generic(
                        iconLayout = ButtonLayout.IconOnly(
                            iconToken = IconTokens.actionOpenExternalLinkOutlined,
                            contentDescription = stringResource(id = R.string.vault_action_open) + " " + label
                        ),
                        onClick = {
                            onValueOpen()
                            true
                        }
                    )
                } else {
                    null
                }
            )
        )
    }
}

@Suppress("kotlin:S1192")
@Preview
@Composable
private fun GenericFieldEditPreview() {
    DashlanePreview {
        Column {
            GenericField(label = "Label", data = "View Mode", editMode = false, onValueChanged = {
                
            })
            GenericField(label = "Label", data = "Edit Mode", editMode = true, onValueChanged = {
                
            })
            GenericField(
                label = "Label",
                data = "Data\non multiple lines",
                editMode = true,
                multiLine = true,
                onValueChanged = {
                    
                }
            )
        }
    }
}

@Suppress("kotlin:S1192")
@Preview
@Composable
private fun GenericFieldPreview() {
    DashlanePreview {
        Column {
            GenericField(label = "Label", data = "View Mode", editMode = false, onValueChanged = {
                
            })
            GenericField(label = "Label", data = "Edit Mode", editMode = true, onValueChanged = {
                
            })
            GenericField(
                label = "Label",
                data = "Data\non multiple lines",
                editMode = false,
                multiLine = true,
                onValueChanged = {
                    
                }
            )
        }
    }
}

@Suppress("kotlin:S1192")
@Preview
@Composable
private fun GenericFieldEditSingleLinePreview() {
    DashlanePreview {
        Column {
            GenericField(label = "Label", data = "View Mode", editMode = false, onValueChanged = {
                
            })
            GenericField(label = "Label", data = "Edit Mode", editMode = true, onValueChanged = {
                
            })
            GenericField(
                label = "Label",
                data = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum",
                editMode = true,
                multiLine = false,
                onValueChanged = {
                    
                }
            )
        }
    }
}

@Suppress("kotlin:S1192")
@Preview
@Composable
private fun GenericFieldSingleLinePreview() {
    DashlanePreview {
        Column {
            GenericField(label = "Label", data = "View Mode", editMode = false, onValueChanged = {
                
            })
            GenericField(label = "Label", data = "Edit Mode", editMode = true, onValueChanged = {
                
            })
            GenericField(
                label = "Label",
                data = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum",
                editMode = false,
                multiLine = false,
                onValueChanged = {
                    
                }
            )
        }
    }
}