package com.dashlane.item.subview



class ItemSubViewWithActionWrapper<T>(val itemSubView: ItemSubView<T>, val action: Action) : ItemSubView<T> by itemSubView