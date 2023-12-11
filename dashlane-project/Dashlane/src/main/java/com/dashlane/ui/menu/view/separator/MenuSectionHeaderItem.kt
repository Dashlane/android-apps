package com.dashlane.ui.menu.view.separator

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.menu.domain.MenuItemModel

@Composable
fun MenuSectionHeaderItem(item: MenuItemModel.SectionHeader) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { heading() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = stringResource(id = item.titleResId),
        style = DashlaneTheme.typography.titleSupportingSmall,
        enforceAllCaps = true,
        color = DashlaneTheme.colors.textNeutralQuiet
    )
}

@Preview
@Composable
private fun MenuSectionHeaderItemPreview() {
    DashlanePreview {
        MenuSectionHeaderItem(MenuItemModel.SectionHeader(titleResId = R.string.menu_v3_header_security_boosters))
    }
}
