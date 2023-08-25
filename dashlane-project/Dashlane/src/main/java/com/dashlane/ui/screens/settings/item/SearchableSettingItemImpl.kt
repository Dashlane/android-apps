package com.dashlane.ui.screens.settings.item

import android.content.Context
import com.dashlane.search.SearchableSettingItem

class SearchableSettingItemImpl(
    private val item: SettingItem,
    private val parents: List<SettingScreenItem>
) : SearchableSettingItem, SettingItem by item {

    override fun getPathDisplay(): String {
        return if (parents.isEmpty()) {
            title
        } else {
            parents.filter { it.title.isNotEmpty() }.joinToString(separator = " > ") { it.title }
        }
    }

    override fun onClick() {
        
    }

    override fun getSettingId(): String = item.id

    override fun getSettingTitle(): String = title

    override fun getSettingDescription(): String? = description

    override fun isSettingVisible(context: Context): Boolean = isVisible()
}