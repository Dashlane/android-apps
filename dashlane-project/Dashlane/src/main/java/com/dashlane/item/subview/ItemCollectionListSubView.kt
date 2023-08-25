package com.dashlane.item.subview

import androidx.compose.runtime.MutableState
import com.dashlane.item.ItemEditViewContract
import com.dashlane.teamspaces.model.Teamspace

class ItemCollectionListSubView(
    override var value: MutableState<List<String>>,
    val editMode: Boolean,
    val itemId: String,
    val header: String,
    val listener: ItemEditViewContract.View.UiUpdateListener,
    val teamspaceView: ItemSubView<Teamspace>?
) : ItemSubViewImpl<MutableState<List<String>>>()