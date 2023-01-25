package com.dashlane.item.subview.view

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dashlane.design.component.InfoboxButton
import com.dashlane.design.component.compat.view.InfoboxLargeView
import com.dashlane.design.theme.color.Mood
import com.dashlane.ui.widgets.view.StepInfoBox



object InfoboxViewProvider {

    fun create(
        activity: AppCompatActivity,
        mood: Mood?,
        title: String,
        description: String? = null,
        primaryButton: InfoboxButton? = null,
        secondaryButton: InfoboxButton? = null,
    ) = InfoboxLargeView(context = activity).also { infoboxView ->
        mood?.let { infoboxView.mood = mood }
        infoboxView.title = title
        infoboxView.description = description
        infoboxView.primaryButton = primaryButton
        infoboxView.secondaryButton = secondaryButton
    }

    fun create(
        context: Context,
        content: String,
        enable: Boolean,
        buttonText: String?,
        @DrawableRes iconImage: Int? = null,
        iconText: String? = null,
        buttonAction: (() -> Unit)? = null
    ) = StepInfoBox(context).apply {
        text = content
        if (buttonText != null) {
            primaryButton.isVisible = true
            primaryButton.apply {
                text = buttonText
                setOnClickListener { buttonAction?.invoke() }
            }
        } else {
            primaryButton.isVisible = false
        }
        secondaryButton.isVisible = false
        setIcon(iconImage, iconText)
        isEnabled = enable
    }
}