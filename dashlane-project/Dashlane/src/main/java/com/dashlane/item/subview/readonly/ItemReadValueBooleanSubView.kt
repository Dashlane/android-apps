package com.dashlane.item.subview.readonly



class ItemReadValueBooleanSubView(
    val header: String,
    val description: String?,
    override var value: Boolean
) : ItemReadValueSubView<Boolean>()