package com.dashlane.item.v3.data

import com.dashlane.item.subview.ItemCollectionListSubView

data class CollectionData(val id: String?, val name: String, val shared: Boolean)

fun ItemCollectionListSubView.Collection.toCollectionData() = CollectionData(id, name, shared)