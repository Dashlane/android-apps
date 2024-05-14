package com.dashlane.collections.edit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.DropdownField
import com.dashlane.design.component.DropdownItem
import com.dashlane.design.component.DropdownItemContent
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.teamspaces.model.SpaceColor
import com.dashlane.teamspaces.model.SpaceName
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.util.BusinessSpaceUtil
import com.dashlane.ui.widgets.compose.OutlinedTeamspaceIcon

@Composable
fun SpacePicker(
    modifier: Modifier,
    spaces: List<TeamSpace>,
    onSpaceSelected: (TeamSpace) -> Unit,
    selectedSpace: TeamSpace,
    readOnly: Boolean = false
) {
    val expandedState = remember { mutableStateOf(false) }
    DropdownField(
        modifier = modifier,
        label = stringResource(id = R.string.collection_add_collection_space_field_label),
        value = selectedSpace.displayName,
        readOnly = readOnly,
        expandedState = expandedState,
    ) {
        spaces.forEach { space ->
            DropdownSpaceItem(
                space = space,
                onClick = {
                    onSpaceSelected(space)
                    expandedState.value = false
                }
            )
        }
    }
}

@Composable
private fun DropdownSpaceItem(
    space: TeamSpace,
    onClick: () -> Unit
) {
    DropdownItem(
        content = {
            DropdownItemContent(
                leadingIcon = {
                    OutlinedTeamspaceIcon(
                        letter = space.displayLetter,
                        color = space.displayColor,
                        iconSize = 24.dp
                    )
                },
                text = space.displayName
            )
        },
        onClick = onClick
    )
}

private val TeamSpace.displayName
    @Composable
    get() = when (val name = this.name) {
        is SpaceName.FixName -> stringResource(name.nameRes)
        is SpaceName.TeamName -> name.value
    }

private val TeamSpace.displayColor
    @Composable
    get() = when (val color = this.color) {
        is SpaceColor.FixColor -> colorResource(color.colorRes).toArgb()
        is SpaceColor.TeamColor -> color.color
    }

private val upshiftTeamForPreview = BusinessSpaceUtil.createCurrentSpace(
    name = "Upshift",
    teamId = 1234,
    color = "#20B422"
)

@Preview
@Composable
fun SpacePickerPreview() {
    DashlanePreview {
        SpacePicker(
            modifier = Modifier.fillMaxWidth(),
            spaces = listOf(
                TeamSpace.Personal,
                upshiftTeamForPreview
            ),
            onSpaceSelected = {},
            selectedSpace = TeamSpace.Personal
        )
    }
}

@Preview
@Composable
fun ReadOnlySpacePickerPreview() {
    DashlanePreview {
        SpacePicker(
            modifier = Modifier.fillMaxWidth(),
            spaces = listOf(
                TeamSpace.Personal,
                upshiftTeamForPreview
            ),
            onSpaceSelected = {},
            selectedSpace = TeamSpace.Personal,
            readOnly = true
        )
    }
}

@Preview
@Composable
private fun SpaceItemPreview() {
    DashlanePreview {
        DropdownSpaceItem(
            space = upshiftTeamForPreview,
            onClick = {}
        )
    }
}