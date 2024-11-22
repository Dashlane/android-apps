package com.dashlane.ui.widgets.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.dashlane.design.component.Text
import com.dashlane.design.component.Toggle
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.BooleanProvider
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun SettingField(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Toggle(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckedChange
    ) {
        Column {
            Text(
                text = title,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard
            )
            description?.let {
                Text(
                    text = description,
                    style = DashlaneTheme.typography.bodyHelperRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet
                )
            }
        }
    }
}

@Preview
@Composable
private fun SettingFieldPreview(@PreviewParameter(BooleanProvider::class) checked: Boolean) {
    DashlanePreview {
        SettingField(
            title = "Title",
            description = "Description is usually going to be a longer field that may span over a few lines",
            checked = checked,
            onCheckedChange = {}
        )
    }
}
