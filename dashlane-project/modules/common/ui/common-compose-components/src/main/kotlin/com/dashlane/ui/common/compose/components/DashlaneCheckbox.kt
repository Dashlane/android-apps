package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun DashlaneCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = CheckboxDefaults.colors(
            checkedColor = DashlaneTheme.colors.containerExpressiveBrandCatchyIdle,
            uncheckedColor = DashlaneTheme.colors.borderNeutralStandardIdle,
            checkmarkColor = DashlaneTheme.colors.textInverseCatchy.value,
            disabledCheckedColor = DashlaneTheme.colors.textOddityDisabled.value,
            disabledIndeterminateColor = DashlaneTheme.colors.textOddityDisabled.value
        )
    )
}

@Preview
@Composable
fun DashlaneCheckboxPreview() {
    DashlanePreview {
        Column {
            DashlaneCheckbox(checked = true, onCheckedChange = {})
            DashlaneCheckbox(checked = true, onCheckedChange = {}, enabled = false)
            DashlaneCheckbox(checked = false, onCheckedChange = {})
            DashlaneCheckbox(checked = false, onCheckedChange = {}, enabled = false)
        }
    }
}