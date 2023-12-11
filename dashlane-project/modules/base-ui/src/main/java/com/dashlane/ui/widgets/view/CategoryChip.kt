package com.dashlane.ui.widgets.view

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
                    style = DashlaneTheme.typography.bodyReducedRegular
                )
            },
            trailingIcon = when {
                editMode -> {
                    {
                        Icon(
                            token = IconTokens.actionCloseOutlined,
                            contentDescription = stringResource(id = R.string.and_accessibility_remove_category),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                shared -> {
                    {
                        Icon(
                            token = IconTokens.sharedOutlined,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                else -> null
            },
            modifier = modifier,
            colors = InputChipDefaults.inputChipColors(
                containerColor = DashlaneTheme.colors.containerExpressiveNeutralQuietIdle,
                disabledContainerColor = DashlaneTheme.colors.containerExpressiveNeutralQuietDisabled,
            ),
            shape = RoundedCornerShape(4.dp),
            border = InputChipDefaults.inputChipBorder(
                borderColor = Color.Transparent,
                selectedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                disabledSelectedBorderColor = Color.Transparent,
                borderWidth = 0.dp,
                selectedBorderWidth = 0.dp,
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
fun PreviewCategoryGroupShared(@PreviewParameter(BooleanProvider::class) shared: Boolean) {
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
                CategoryChip(label = it, onClick = {}, shared = shared)
            }
        }
    }
}