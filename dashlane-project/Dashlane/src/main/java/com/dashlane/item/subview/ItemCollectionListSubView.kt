package com.dashlane.item.subview

import androidx.compose.runtime.MutableState
import com.dashlane.item.ItemEditViewContract
import com.dashlane.teamspaces.model.Teamspace

class ItemCollectionListSubView(
    override var value: MutableState<List<Pair<String, Boolean>>>,
    val editMode: Boolean,
    val itemId: String,
    val header: MutableState<String>,
    val listener: ItemEditViewContract.View.UiUpdateListener? = null,
    val teamspaceView: ItemSubView<Teamspace>? = null
) : ItemSubViewImpl<MutableState<List<Pair<String, Boolean>>>>()