package com.dashlane.collections.edit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.TextColor
import com.dashlane.design.theme.color.animateTextColorAsState
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.teamspaces.PersonalTeamspace
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.widgets.compose.OutlinedTeamspaceIcon

@Composable
fun SpacePicker(
    spaces: List<Teamspace>,
    onSpaceSelected: (Teamspace) -> Unit,
    selectedSpace: Teamspace,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    SpacePicker(
        enabled = enabled,
        spaces = spaces,
        onSpaceSelected = onSpaceSelected,
        selectedSpace = selectedSpace,
        expanded = expanded,
        updateExpanded = { expanded = it }
    )
}

@Suppress("LongMethod")
@Composable
fun SpacePicker(
    enabled: Boolean,
    spaces: List<Teamspace>,
    onSpaceSelected: (Teamspace) -> Unit,
    selectedSpace: Teamspace,
    expanded: Boolean,
    updateExpanded: (Boolean) -> Unit
) {
    Box {
        val interactionSource = remember { MutableInteractionSource() }
        val focused by interactionSource.collectIsFocusedAsState()
        val focusRequester = remember { FocusRequester() }
        val borderColorState = animateBorderColorAsState(enabled, focused)
        Row(
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable(enabled = enabled, interactionSource = interactionSource)
                .border(
                    width = 1.dp,
                    color = borderColorState.value,
                    shape = RoundedCornerShape(6.dp)
                )
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (enabled) {
                        DashlaneTheme.colors.containerAgnosticNeutralSupershy
                    } else {
                        DashlaneTheme.colors.containerExpressiveNeutralQuietDisabled
                    }
                )
                .clickable(enabled = enabled) {
                    focusRequester.requestFocus()
                    updateExpanded(true)
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .focusable(enabled),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val textColorState = animateHeaderTextColorAsState(enabled = enabled, focused = focused)
            Column(
                modifier = Modifier
                    .weight(weight = 1f, fill = true)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.collection_add_collection_space_field_label),
                        style = DashlaneTheme.typography.bodyHelperRegular,
                        color = textColorState.value
                    )
                    if (!enabled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            modifier = Modifier.size(10.dp),
                            token = IconTokens.lockFilled,
                            contentDescription = null,
                            tint = textColorState.value
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                TeamspaceRow(
                    teamspace = selectedSpace,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled
                )
            }

            val rotation: Float by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                label = "caret rotation"
            )

            Icon(
                token = IconTokens.caretDownOutlined,
                contentDescription = null,
                modifier = Modifier.rotate(rotation),
                tint = textColorState.value
            )
        }

        MaterialTheme(
            colors = MaterialTheme.colors.copy(
                surface = DashlaneTheme.colors.containerAgnosticNeutralSupershy
            )
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    updateExpanded(false)
                }
            ) {
                spaces.forEach {
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onSpaceSelected(it)
                            updateExpanded(false)
                        }
                    ) {
                        TeamspaceRow(teamspace = it)
                    }
                }
            }
        }
    }
}

@Composable
fun TeamspaceRow(teamspace: Teamspace, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTeamspaceIcon(
            letter = teamspace.displayLetter.firstOrNull() ?: ' ',
            color = teamspace.colorInt,
            iconSize = 24.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = teamspace.teamName ?: "",
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = if (enabled) {
                DashlaneTheme.colors.textNeutralStandard
            } else {
                DashlaneTheme.colors.textOddityDisabled
            }
        )
    }
}

internal val animationSpecOfTextFieldColor: AnimationSpec<Color>
    @Composable
    get() = tween(durationMillis = 150)

@Composable
internal fun animateHeaderTextColorAsState(
    enabled: Boolean,
    focused: Boolean
): State<TextColor> {
    val textColor = when {
        !enabled -> DashlaneTheme.colors.textOddityDisabled
        !focused -> DashlaneTheme.colors.textNeutralQuiet
        else -> DashlaneTheme.colors.textBrandQuiet
    }
    return animateTextColorAsState(targetValue = textColor, animationSpec = animationSpecOfTextFieldColor)
}

@Composable
internal fun animateBorderColorAsState(
    enabled: Boolean,
    focused: Boolean
): State<Color> {
    val color = when {
        !enabled -> DashlaneTheme.colors.borderNeutralQuietIdle
        focused -> DashlaneTheme.colors.borderBrandStandardActive
        else -> DashlaneTheme.colors.borderNeutralQuietIdle
    }
    return animateColorAsState(
        targetValue = color,
        animationSpec = animationSpecOfTextFieldColor,
        label = "border color"
    )
}

@Preview
@Composable
fun PreviewSpacePicker() {
    DashlanePreview {
        SpacePicker(
            spaces = listOf(
                PersonalTeamspace.apply {
                    teamName = "Personal"
                    displayLetter = "P"
                },
                Teamspace().apply {
                    teamId = "teamId"
                    teamName = "Dashlane Android"
                    companyName = "Dashlane"
                    color = "#FF00FF"
                    displayLetter = "D"
                }
            ),
            onSpaceSelected = {},
            selectedSpace = PersonalTeamspace
        )
    }
}

@Preview
@Composable
fun PreviewDisabledSpacePicker() {
    DashlanePreview {
        SpacePicker(
            spaces = listOf(
                PersonalTeamspace.apply {
                    teamName = "Personal"
                    displayLetter = "P"
                },
                Teamspace().apply {
                    teamId = "teamId"
                    teamName = "Dashlane Android"
                    companyName = "Dashlane"
                    color = "#FF00FF"
                    displayLetter = "D"
                }
            ),
            onSpaceSelected = {},
            selectedSpace = PersonalTeamspace,
            enabled = false
        )
    }
}