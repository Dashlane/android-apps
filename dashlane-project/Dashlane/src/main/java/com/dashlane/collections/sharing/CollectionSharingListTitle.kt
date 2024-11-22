package com.dashlane.collections.sharing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
internal fun CollectionSharingListTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        style = DashlaneTheme.typography.titleSupportingSmall,
        enforceAllCaps = true,
        color = DashlaneTheme.colors.textNeutralQuiet,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
@Preview
private fun CollectionSharingListTitlePreview() {
    DashlanePreview {
        Column {
            CollectionSharingListTitle("Groups")
            CollectionSharingListTitle("Individuals")
        }
    }
}