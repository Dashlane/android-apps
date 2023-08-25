package com.dashlane.item

import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.ItemSubView

data class ScreenConfiguration(val itemSubViews: List<ItemSubView<*>>, val itemHeader: ItemHeader? = null)