package com.dashlane.item.v3.data

import com.dashlane.item.collection.CollectionSelectorState

data class CollectionData(val id: String?, val name: String, val shared: Boolean)

fun CollectionSelectorState.Collection.toCollectionData() = CollectionData(id, name, shared)