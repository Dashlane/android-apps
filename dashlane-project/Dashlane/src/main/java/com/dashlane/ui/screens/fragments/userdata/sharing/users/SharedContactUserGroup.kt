package com.dashlane.ui.screens.fragments.userdata.sharing.users

import android.content.Context
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.dashlane.R
import com.dashlane.ui.drawable.CircleDrawable
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingContactItem
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels

class SharedContactUserGroup(
    context: Context,
    private val userGroup: SharingModels.ItemUserGroup,
    val onPendingMenuClick: (View, SharingModels.ItemUserGroup) -> Unit,
    val onAcceptedMenuClick: (View, SharingModels.ItemUserGroup) -> Unit
) : SharingContactItem(context, userGroup.name, {
    val action = findViewByIdEfficient<View?>(R.id.action)
    if (userGroup.isAdmin && action != null) {
        setVisibility(R.id.action, View.VISIBLE)
        action.setOnClickListener { v ->
            if (userGroup.isPending) {
                onPendingMenuClick(v, userGroup)
            } else if (userGroup.isAccepted) {
                onAcceptedMenuClick(v, userGroup)
            }
        }
    } else {
        setVisibility(R.id.action, View.GONE)
    }
}) {
    init {
        icon = AppCompatResources.getDrawable(context, R.drawable.ic_sharing_user_group)!!
            .mutate().let {
                it.setTint(context.getColor(R.color.text_neutral_standard))
                CircleDrawable.with(context, R.color.container_agnostic_neutral_standard, it)
            }
    }

    override fun getLine2() = if (userGroup.sharingStatusResource > 0) {
        context.getString(userGroup.sharingStatusResource)
    } else {
        ""
    }

    override fun isContentTheSame(item: SharingContactItem) =
        super.isContentTheSame(item) &&
            item is SharedContactUserGroup &&
            userGroup.sharingStatusResource == item.userGroup.sharingStatusResource
}