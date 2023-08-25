package com.dashlane.item.subview.readonly

import com.dashlane.design.component.InfoboxButton
import com.dashlane.design.theme.color.Mood

class ItemInfoboxSubView(
    override var value: String,
    val mood: Mood? = null,
    val description: String? = null,
    val primaryButton: InfoboxButton? = null,
    val secondaryButton: InfoboxButton? = null
) : ItemReadValueSubView<String>()