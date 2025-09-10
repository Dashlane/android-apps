package com.dashlane.autofill.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.autofill.pause.model.AutofillFormSourceIcon
import com.dashlane.autofill.pause.view.AutofillFormSourceThumbnail
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.DropdownItem
import com.dashlane.design.component.ListItem
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun AutofillCard(
    listItemHeader: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: (@Composable ColumnScope.() -> Unit)?,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = DashlaneTheme.colors.borderNeutralQuietIdle,
                shape = RoundedCornerShape(8.dp),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DashlaneTheme.colors.containerAgnosticNeutralQuiet)
                .padding(all = 8.dp),
        ) {
            listItemHeader()
        }
        if (content != null) {
            Spacer(
                modifier = Modifier.height(8.dp),
            )
            content()
            Spacer(
                modifier = Modifier.height(8.dp),
            )
        }
    }
}

@Preview
@Composable
private fun AutofillCardPreview() {
    DashlanePreview {
        Box(
            modifier = Modifier
                .background(DashlaneTheme.colors.containerAgnosticNeutralSupershy)
                .padding(16.dp),
        ) {
            AutofillCard(
                modifier = Modifier.fillMaxWidth(),
                listItemHeader = {
                    ListItem(
                        thumbnail = {
                            AutofillFormSourceThumbnail(AutofillFormSourceIcon.UnknownApplication)
                        },
                        title = "discord.com",
                        description = "randomemail@provider.com",
                        onClick = null,
                    )
                },
            ) {
                DisplayField(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    label = "App",
                    value = "Disney+",
                    actions = null,
                )
                ListItem(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    thumbnail = {
                        AutofillFormSourceThumbnail(AutofillFormSourceIcon.UnknownApplication)
                    },
                    title = "disneyplus.com",
                    description = "randomemail@provider.com",
                    onClick = null,
                )
                var isPauseSelected by remember { mutableStateOf(true) }
                DropdownItem(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    icon = null,
                    text = "Until I turn it back on",
                    selected = isPauseSelected,
                    onClick = { isPauseSelected = !isPauseSelected },
                )
            }
        }
    }
}