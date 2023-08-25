package com.dashlane.search

interface SearchField<T> {
    val order: Int
    val fieldType: FieldType
    val itemType: ItemType
}