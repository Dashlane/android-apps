package com.dashlane.ui.screens.settings.item

import android.content.Context
import com.dashlane.ui.adapter.util.DiffUtilComparator

interface SettingItem : DiffUtilComparator<SettingItem> {

    val id: String

    val header: SettingHeader?

    val title: String

    val description: String?

    fun isEnable(): Boolean

    fun isVisible(): Boolean

    fun onClick(context: Context)

    override fun isItemTheSame(item: SettingItem): Boolean = item.id == id

    override fun isContentTheSame(item: SettingItem): Boolean =
        item.header == header &&
            item.title == title &&
            item.description == description
}
