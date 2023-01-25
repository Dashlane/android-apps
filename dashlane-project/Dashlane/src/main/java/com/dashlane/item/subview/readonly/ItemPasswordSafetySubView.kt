package com.dashlane.item.subview.readonly



class ItemPasswordSafetySubView(
    val header: String,
    override var value: String
) : ItemReadValueSubView<String>()