package com.dashlane.item.subview.view

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.compat.view.ButtonMediumView
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood

object ButtonInputProvider {

    fun create(
        context: Context,
        buttonText: String,
        @DrawableRes buttonIcon: Int,
        mood: Mood,
        intensity: Intensity,
        buttonAction: () -> Unit
    ): View {
        return ButtonMediumView(context, null, -1).apply {
            this.mood = mood
            this.intensity = intensity
            this.buttonLayout = if (buttonIcon != -1) {
                ButtonLayout.IconLeading(IconToken(buttonIcon), buttonText)
            } else {
                ButtonLayout.TextOnly(buttonText)
            }
            this.onClick = buttonAction
        }
    }

    fun createIconButton(
        context: Context,
        buttonText: String,
        @DrawableRes buttonIcon: Int,
        mood: Mood,
        intensity: Intensity,
        buttonAction: () -> Unit
    ): View {
        return ButtonMediumView(context, null, -1).apply {
            this.mood = mood
            this.intensity = intensity
            this.buttonLayout = ButtonLayout.IconOnly(IconToken(buttonIcon), buttonText)
            this.onClick = buttonAction
        }
    }
}