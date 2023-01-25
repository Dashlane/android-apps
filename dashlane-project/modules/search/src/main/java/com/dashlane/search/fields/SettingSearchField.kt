package com.dashlane.search.fields

import com.dashlane.search.FieldType
import com.dashlane.search.ItemType
import com.dashlane.search.SearchField
import com.dashlane.search.SearchableSettingItem

enum class SettingSearchField(
    override val order: Int,
    override val itemType: ItemType = ItemType.SETTING,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SearchableSettingItem> {
    TITLE(order = 0),
    DESCRIPTION(order = 1);
}