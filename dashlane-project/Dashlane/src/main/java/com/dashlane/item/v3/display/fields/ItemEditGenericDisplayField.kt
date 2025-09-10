package com.dashlane.item.v3.display.fields

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.tooling.DisplayFieldActions
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.iconography.IconTokens

@Composable
fun ItemEditGenericDisplayField(
    modifier: Modifier = Modifier,
    label: String,
    data: String?,
    onValueCopy: (() -> Unit)? = null,
    onValueOpen: (() -> Unit)? = null,
) {
    DisplayField(
        modifier = modifier,
        label = label,
        value = data,
        actions = DisplayFieldActions.newInstance(
            action1 = if (onValueCopy != null) {
                FieldAction.Generic(
                    iconLayout = ButtonLayout.IconOnly(
                        iconToken = IconTokens.actionCopyOutlined,
                        contentDescription = stringResource(
                            id = R.string.vault_action_copy_field,
                            label,
                        ),
                    ),
                    onClick = {
                        onValueCopy()
                        true
                    },
                )
            } else {
                null
            },
            action2 = if (onValueOpen != null) {
                FieldAction.Generic(
                    iconLayout = ButtonLayout.IconOnly(
                        iconToken = IconTokens.actionOpenExternalLinkOutlined,
                        contentDescription = stringResource(id = R.string.vault_action_open, label),
                    ),
                    onClick = {
                        onValueOpen()
                        true
                    },
                )
            } else {
                null
            },
        ),
    )
}