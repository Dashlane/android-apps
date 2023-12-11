package com.dashlane.ui.menu.view.teamspace

import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.menu.domain.MenuItemModel
import com.dashlane.ui.menu.domain.TeamspaceIcon

@Composable
fun MenuTeamspaceItem(item: MenuItemModel.Teamspace) {
    Row(
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .wrapContentHeight()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { item.onClick.invoke() }
            .padding(all = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MenuTeamspaceIcon(
            modifier = Modifier
                .size(24.dp)
                .padding(end = 4.dp),
            icon = item.icon
        )
        Text(
            text = item.name,
            style = DashlaneTheme.typography.titleBlockMedium,
            color = DashlaneTheme.colors.textNeutralCatchy
        )
    }
}

@Preview
@Composable
fun MenuTeamspaceItemPreview() {
    DashlanePreview {
        MenuTeamspaceItem(
            item = MenuItemModel.Teamspace(
                name = "Personnel",
                icon = TeamspaceIcon.Space(displayLetter = "P", colorInt = Color.MAGENTA)
            ) { }
        )
    }
}