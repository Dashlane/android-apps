package com.dashlane.item.subview.readonly

open class ItemReadValueRawSubView(
    val hint: String,
    override var value: String,
    val textSize: Float
) : ItemReadValueSubView<String>()