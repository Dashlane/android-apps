package com.dashlane.item.subview.readonly



open class ItemExpandableListSubView(
    override var value: List<String>,
    val summary: String,
    val showListListener: (List<String>) -> Unit
) : ItemReadValueSubView<List<String>>()