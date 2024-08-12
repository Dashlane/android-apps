package com.dashlane.item.subview

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.compose.runtime.MutableState
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.subview.ItemCollectionListSubView.Collection
import com.dashlane.teamspaces.model.TeamSpace
import kotlinx.parcelize.Parcelize

@Keep
class ItemCollectionListSubView(
    override var value: MutableState<List<Collection>>,
    val editMode: Boolean,
    val header: MutableState<String>,
    val listener: ItemEditViewContract.View.UiUpdateListener? = null,
    val teamspaceView: ItemSubView<TeamSpace>? = null
) : ItemSubViewImpl<MutableState<List<Collection>>>() {
    @Parcelize
    data class Collection(val id: String? = null, val name: String, val shared: Boolean) :
        Parcelable
}