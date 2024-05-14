package com.dashlane.ui.widgets.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.BooleanProvider
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.R
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    label: String,
    modifier: Modifier = Modifier,
    editMode: Boolean = false,
    shared: Boolean = false,
    onClick: () -> Unit = {}
) {
    DashlaneTheme {
        InputChip(
            onClick = onClick,
            selected = false,
            label = {
                Text(
                    label,
                    color = DashlaneTheme.colors.textNeutralStandard,
                    style = DashlaneTheme.typography.bodyStandardRegular
                )
                if (shared) {
                    Icon(
                        token = IconTokens.sharedOutlined,
                        contentDescription = stringResource(id = R.string.and_accessibility_shared_category),
                        modifier = Modifier
                            .size(14.dp)
                            .padding(start = 2.dp),
                        tint = DashlaneTheme.colors.textNeutralQuiet
                    )
                }
            },
            trailingIcon = {
                if (editMode) {
                    Icon(
                        token = IconTokens.actionCloseOutlined,
                        contentDescription = stringResource(id = R.string.and_accessibility_remove_category),
                        modifier = Modifier.size(12.dp)
                    )
                }
            },
            modifier = modifier,
            colors = InputChipDefaults.inputChipColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(12.dp),
            border = InputChipDefaults.inputChipBorder(
                borderColor = DashlaneTheme.colors.borderNeutralQuietIdle,
                selectedBorderColor = DashlaneTheme.colors.borderNeutralQuietIdle,
                disabledBorderColor = DashlaneTheme.colors.borderNeutralQuietIdle,
                disabledSelectedBorderColor = DashlaneTheme.colors.borderNeutralQuietIdle,
                borderWidth = 1.dp,
                selectedBorderWidth = 1.dp,
            )
        )
    }
}

@Composable
fun CategoryChipList(modifier: Modifier = Modifier, content: @Composable (() -> Unit) = { }) {
    FlowRow(modifier = modifier, mainAxisSpacing = 16.dp) {
        content()
    }
}

@Composable
@Preview
fun PreviewCategoryChip() {
    DashlanePreview {
        CategoryChip("Finance")
    }
}

@Composable
@Preview
fun TextTooLongPreview() {
    DashlanePreview {
        CategoryChip(
            label = """This item is way too long to be displayed on 1 line. 
Unfortunately when it spans over multiple lines the trailing icon is not visible anymore 😢""",
            editMode = true
        )
    }
}

@Composable
@Preview
fun PreviewCategoryGroup(@PreviewParameter(BooleanProvider::class) editMode: Boolean) {
    val categories = listOf(
        "Finance",
        "Business",
        "Gaming",
        "Streaming",
        "Fashion",
        "Shopping",
        "Literature",
        "Amazing Stuff"
    )
    DashlanePreview {
        CategoryChipList {
            categories.forEach {
                CategoryChip(label = it, onClick = {}, editMode = editMode)
            }
        }
    }
}

@Composable
@Preview
fun PreviewCategoryGroupShared(@PreviewParameter(BooleanProvider::class) editMode: Boolean) {
    val categories = listOf(
        "Finance",
        "Business",
        "Gaming",
        "Streaming",
        "Fashion",
        "Shopping",
        "Literature",
        "Amazing Stuff"
    ).mapIndexed { index, name -> name to (index < 4) }
    DashlanePreview {
        CategoryChipList {
            categories.forEach { (name, shared) ->
                CategoryChip(label = name, onClick = {}, shared = shared, editMode = editMode)
            }
        }
    }
}