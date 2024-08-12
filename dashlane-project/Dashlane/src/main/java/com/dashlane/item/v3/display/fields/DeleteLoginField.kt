package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun DeleteLoginField(canDelete: Boolean, editMode: Boolean, onDeleteAction: () -> Unit) {
    if (!editMode || !canDelete) {
        return
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        ButtonMedium(
            onClick = onDeleteAction,
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.vault_delete_login)
            ),
            mood = Mood.Danger,
            intensity = Intensity.Catchy,
        )
    }
}

@Preview
@Composable
private fun DeleteLoginFieldPreview() {
    DashlanePreview {
        Column {
            DeleteLoginField(canDelete = true, editMode = true, onDeleteAction = {
                
            })
        }
    }
}