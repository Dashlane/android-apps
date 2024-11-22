package com.dashlane.item.v3.display

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.InfoboxButton
import com.dashlane.design.component.InfoboxLarge
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.InfoBoxData
import com.dashlane.item.v3.data.InfoboxStyle
import com.dashlane.ui.model.TextResource
import com.dashlane.ui.model.getText

@Suppress("LongMethod")
fun LazyListScope.displayInfoBox(
    infoBoxes: List<InfoBoxData>,
    editMode: Boolean,
    onInfoBoxActionClicked: (InfoBoxData.Button.Action) -> Unit
) {
    if (editMode) return

    items(
        items = infoBoxes,
        key = { it.hashCode() }
    ) {
        when (it.infoboxStyle) {
            InfoboxStyle.LARGE -> {
                InfoboxLarge(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    title = it.title.getText(),
                    description = it.description?.getText(),
                    mood = it.mood,
                    primaryButton = it.button?.let { button ->
                        InfoboxButton(
                            text = button.text.getText(),
                            onClick = { onInfoBoxActionClicked(button.action) },
                        )
                    }
                )
            }
            InfoboxStyle.MEDIUM -> {
                InfoboxMedium(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    title = it.title.getText(),
                    description = it.description?.getText(),
                    mood = it.mood
                )
            }
        }
    }
}

@Preview
@Composable
private fun InfoBoxPreview() {
    DashlanePreview {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            displayInfoBox(
                editMode = false,
                infoBoxes = generatePreviewInfoBoxes(),
            ) {
                
            }
        }
    }
}

private fun generatePreviewInfoBoxes() = listOf(
    InfoBoxData(
        title = TextResource.StringText(R.string.frozen_account_item_edit_banner_title),
        description = TextResource.StringText(
            stringRes = R.string.frozen_account_item_edit_banner_description,
            arg = TextResource.Arg.StringArg("25")
        ),
        mood = Mood.Danger
    ),
    InfoBoxData(
        title = TextResource.StringText(R.string.weak_password_info_title),
        description = TextResource.StringText(R.string.weak_password_info_content),
        mood = Mood.Warning,
        button = InfoBoxData.Button(
            text = TextResource.StringText(R.string.weak_password_info_action),
            action = InfoBoxData.Button.Action.LAUNCH_GUIDED_CHANGE,
        )
    ),
    InfoBoxData(
        title = TextResource.StringText(R.string.compromised_password_info_title),
        description = TextResource.StringText(R.string.compromised_password_info_content),
        mood = Mood.Warning,
        button = InfoBoxData.Button(
            text = TextResource.StringText(R.string.compromised_password_info_action),
            action = InfoBoxData.Button.Action.LAUNCH_GUIDED_CHANGE,
        )
    ),
    InfoBoxData(
        title = TextResource.StringText(R.string.reused_password_info_title),
        description = TextResource.StringText(R.string.reused_password_info_content),
        mood = Mood.Warning,
        button = InfoBoxData.Button(
            text = TextResource.StringText(R.string.weak_password_info_action),
            action = InfoBoxData.Button.Action.LAUNCH_GUIDED_CHANGE
        )
    ),
    InfoBoxData(
        title = TextResource.StringText(R.string.vault_limited_rights_infobox_title),
        description = TextResource.StringText(R.string.vault_limited_rights_infobox_content),
        mood = Mood.Neutral
    ),
    InfoBoxData(
        title = TextResource.StringText(R.string.vault_limited_rights_infobox_title),
        description = TextResource.StringText(R.string.vault_limited_rights_secure_note_infobox_content),
        mood = Mood.Neutral,
        infoboxStyle = InfoboxStyle.MEDIUM
    ),
    InfoBoxData(
        title = TextResource.StringText(R.string.secure_note_attachments_restriction_infobox_title),
        mood = Mood.Neutral,
        infoboxStyle = InfoboxStyle.MEDIUM
    ),
    InfoBoxData(
        title = TextResource.StringText(R.string.secure_note_sharing_and_collection_restriction_infobox_title),
        mood = Mood.Neutral,
        infoboxStyle = InfoboxStyle.MEDIUM
    )
)