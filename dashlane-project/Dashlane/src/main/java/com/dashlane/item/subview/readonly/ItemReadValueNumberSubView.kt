package com.dashlane.item.subview.readonly

import androidx.annotation.ColorInt



class ItemReadValueNumberSubView(
    val header: String,
    override var value: String,
    @ColorInt
    val textColorResId: Int = 0,
    val protected: Boolean = false,
    val protectedStateListener: (Boolean) -> Unit = {}
) : ItemReadValueSubView<String>()