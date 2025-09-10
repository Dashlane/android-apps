package com.dashlane.ui.widgets.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Tag
import com.dashlane.design.component.TagLeadingIcon
import com.dashlane.design.component.TagTrailingIcon
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.R

@Composable
fun CollectionTag(
    label: String,
    modifier: Modifier = Modifier,
    shared: Boolean = false,
) {
    Tag(
        modifier = modifier,
        label = label,
        leadingIcon = TagLeadingIcon.Token(
            token = IconTokens.collectionOutlined,
            contentDescription = null,
        ),
        trailingIcon = if (shared) {
            TagTrailingIcon(
                token = IconTokens.sharedOutlined,
                contentDescription = stringResource(id = R.string.and_accessibility_shared_category),
            )
        } else {
            null
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectionTagList(modifier: Modifier = Modifier, content: @Composable (() -> Unit) = { }) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
@Preview
private fun PreviewCollectionTag() {
    DashlanePreview {
        CollectionTag("Finance")
    }
}

@Composable
@Preview
private fun CollectionTagTextTooLongPreview() {
    DashlanePreview {
        CollectionTag(
            label = """This item is way too long to be displayed on 1 line. 
Unfortunately when it spans over multiple lines the trailing icon is not visible anymore 😢""",
        )
    }
}

@Composable
@Preview
private fun PreviewCollectionTagList() {
    val categories = listOf(
        "Finance",
        "Business",
        "Gaming",
        "Streaming",
        "Fashion",
        "Shopping",
        "Literature",
        "Amazing Stuff",
    )
    DashlanePreview {
        CollectionTagList {
            categories.forEach {
                CollectionTag(label = it)
            }
        }
    }
}

@Composable
@Preview
private fun PreviewCollectionTagShared() {
    val collections = listOf(
        "Finance",
        "Business",
        "Gaming",
        "Streaming",
        "Fashion",
        "Shopping",
        "Literature",
        "Amazing Stuff",
    ).mapIndexed { index, name -> name to (index < 4) }
    DashlanePreview {
        CollectionTagList {
            collections.forEach { (name, shared) ->
                CollectionTag(label = name, shared = shared)
            }
        }
    }
}