package com.dashlane.item.v3.display.fields

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.cardBackground
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun SectionContent(editMode: Boolean, content: @Composable (ColumnScope.() -> Unit)) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (editMode) {
                    Modifier
                } else {
                    Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
                        .cardBackground()
                }
            )
            .padding(16.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Preview
@Composable
private fun SectionContentPreview() {
    DashlanePreview {
        Column {
            SectionContent(editMode = false) {
                SectionTitle(
                    title = stringResource(id = R.string.vault_organisation),
                    editMode = false
                )
                GenericField(
                    label = stringResource(id = R.string.creation_date_header),
                    data = stringResource(id = R.string.now_display_format),
                    editMode = false,
                    onValueChanged = {}
                )
            }
        }
    }
}