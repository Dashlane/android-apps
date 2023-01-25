package com.dashlane.autofill.api.util

import android.app.slice.Slice
import android.graphics.drawable.Icon

interface DashlaneInlinePresentationBuilder {
    fun setTitle(title: CharSequence): DashlaneInlinePresentationBuilder
    fun setSubtitle(subtitle: CharSequence): DashlaneInlinePresentationBuilder
    fun setStartIcon(startIcon: Icon): DashlaneInlinePresentationBuilder
    fun setEndIcon(endIcon: Icon): DashlaneInlinePresentationBuilder
    fun setContentDescription(description: CharSequence): DashlaneInlinePresentationBuilder
    fun build(): Slice
}