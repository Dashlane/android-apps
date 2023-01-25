package com.dashlane.item.subview.readonly



class ItemReadValueListSubView(
    val title: String,
    override var value: String,
    val values: List<String>
) : ItemReadValueSubView<String>()