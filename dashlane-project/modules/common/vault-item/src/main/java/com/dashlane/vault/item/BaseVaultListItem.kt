package com.dashlane.vault.item

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.ListItemActions

@Composable
@SuppressWarnings("kotlin:S107") 
fun BaseVaultListItem(
    modifier: Modifier = Modifier,
    title: AnnotatedString,
    description: AnnotatedString,
    checked: Boolean? = null,
    titleExtraContent: @Composable (RowScope.() -> Unit),
    thumbnail: @Composable (() -> Unit),
    actions: ListItemActions?,
    onClick: (() -> Unit)?,
    onLongClick: () -> Unit = {},
) {
    ListItem(
        modifier = modifier,
        title = title,
        description = description,
        checked = checked,
        titleExtraContent = titleExtraContent,
        thumbnail = thumbnail,
        actions = actions,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}