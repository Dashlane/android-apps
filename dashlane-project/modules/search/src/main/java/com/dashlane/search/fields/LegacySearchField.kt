package com.dashlane.search.fields

import com.dashlane.search.FieldType
import com.dashlane.search.ItemType
import com.dashlane.search.SearchField



enum class LegacySearchField(
    override val order: Int,
    override val itemType: ItemType = ItemType.SETTING,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<Any> {
    ANY_FIELD(order = 0)
}
