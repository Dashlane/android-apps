package com.dashlane.item.subview.readonly

import androidx.annotation.DrawableRes
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood

class ItemClickActionSubView(
    override var value: String,
    val mood: Mood,
    val intensity: Intensity,
    @DrawableRes
    val iconResId: Int = -1,
    val gravity: Int,
    val clickAction: () -> Unit
) : ItemReadValueSubView<String>()