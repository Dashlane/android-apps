package com.dashlane.item.subview

import com.dashlane.ui.action.Action

class ItemSubViewWithActionWrapper<T>(val itemSubView: ItemSubView<T>, val action: Action) : ItemSubView<T> by itemSubView