package com.dashlane.ui.widgets.compose

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.theme.color.Intensity

@Suppress("kotlin:S107") 
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButtonClick: () -> Unit,
    confirmButtonText: String,
    modifier: Modifier = Modifier,
    dismissButtonClick: () -> Unit,
    dismissButtonText: String,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties()
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ButtonMedium(
                onClick = confirmButtonClick,
                intensity = Intensity.Catchy,
                layout = ButtonLayout.TextOnly(text = confirmButtonText)
            )
        },
        modifier = modifier,
        dismissButton = {
            ButtonMedium(
                onClick = dismissButtonClick,
                intensity = Intensity.Quiet,
                layout = ButtonLayout.TextOnly(text = dismissButtonText)
            )
        },
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties
    )
}