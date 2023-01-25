package com.dashlane.item.subview.readonly

import androidx.annotation.ColorInt



open class ItemReadValueTextSubView(
    val header: String,
    override var value: String,
    @ColorInt
    val textColorResId: Int = 0,
    val protected: Boolean = false,
    val allowReveal: Boolean = true,
    val multiline: Boolean = false,
    val coloredCharacter: Boolean = false,
    val protectedStateListener: (Boolean) -> Unit = {}
) : ItemReadValueSubView<String>()