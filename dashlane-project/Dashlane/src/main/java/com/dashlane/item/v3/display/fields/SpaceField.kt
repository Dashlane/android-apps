package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.collections.edit.SpacePicker
import com.dashlane.design.component.DisplayField
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CommonData
import com.dashlane.teamspaces.model.SpaceColor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.stringName
import com.dashlane.ui.widgets.compose.OutlinedTeamspaceIcon

@Composable
fun SpaceField(
    commonData: CommonData,
    editMode: Boolean,
    isEditable: Boolean,
    onSpaceSelected: (TeamSpace) -> Unit
) {
    if (commonData.space == null) {
        
        return
    }
    if (editMode) {
        if (commonData.availableSpaces.isNotEmpty()) {
            SpacePicker(
                modifier = Modifier.fillMaxWidth(),
                spaces = commonData.availableSpaces,
                onSpaceSelected = { onSpaceSelected(it) },
                selectedSpace = commonData.space,
                readOnly = isEditable.not()
            )
        }
    } else {
        DisplayField(
            label = stringResource(id = R.string.teamspaces_selector_label),
            value = commonData.space.stringName(LocalContext.current),
            advancedLeadingIcon = {
                OutlinedTeamspaceIcon(
                    letter = commonData.space.displayLetter,
                    color = when (val color = commonData.space.color) {
                        is SpaceColor.FixColor -> colorResource(color.colorRes).toArgb()
                        is SpaceColor.TeamColor -> color.color
                    },
                    iconSize = 16.dp,
                )
            }
        )
    }
}

@Preview
@Composable
private fun SpaceFieldPreview() {
    val commonData = CommonData(
        space = TeamSpace.Personal,
        availableSpaces = listOf(
            TeamSpace.Personal
        )
    )
    DashlanePreview {
        Column {
            SpaceField(commonData = commonData, editMode = false, isEditable = true) {
                
            }
            SpaceField(commonData = commonData, editMode = true, isEditable = false) {
                
            }
            SpaceField(commonData = commonData, editMode = true, isEditable = true) {
                
            }
        }
    }
}
