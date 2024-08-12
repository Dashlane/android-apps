package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.design.theme.typography.LocalDashlaneAllCaps

@Composable
fun SectionTitle(title: String, editMode: Boolean) {
    Text(
        modifier = Modifier.semantics { heading() },
        text = title,
        color = if (editMode) {
            DashlaneTheme.colors.textNeutralQuiet
        } else {
            DashlaneTheme.colors.textNeutralCatchy
        },
        style = if (editMode) {
            DashlaneTheme.typography.titleSupportingSmall
        } else {
            DashlaneTheme.typography.titleBlockMedium
        },
        overflow = TextOverflow.Ellipsis,
        enforceAllCaps = editMode || LocalDashlaneAllCaps.current
    )
}

@Preview
@Composable
private fun SectionTitlePreview() {
    DashlanePreview {
        Column {
            SectionTitle(title = stringResource(id = R.string.vault_organisation), editMode = false)
        }
    }
}