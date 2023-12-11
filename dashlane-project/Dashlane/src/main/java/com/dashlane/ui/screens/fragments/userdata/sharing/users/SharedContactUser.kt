package com.dashlane.ui.screens.fragments.userdata.sharing.users

import android.content.Context
import android.view.View
import com.dashlane.R
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingContactItem
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels

class SharedContactUser(
    context: Context,
    private val user: SharingModels.ItemUser,
    val onPendingMenuClick: (View, SharingModels.ItemUser) -> Unit,
    val onAcceptedMenuClick: (View, SharingModels.ItemUser) -> Unit
) : SharingContactItem(context, user.userId, {
    val action = findViewByIdEfficient<View?>(R.id.action)
    if (user.isAdmin && action != null) {
        setVisibility(R.id.action, View.VISIBLE)
        action.setOnClickListener { v ->
            if (user.isPending) {
                onPendingMenuClick(v, user)
            } else if (user.isAccepted) {
                onAcceptedMenuClick(v, user)
            }
        }
    } else {
        setVisibility(R.id.action, View.GONE)
    }
}) {
    override fun getLine2() = if (user.sharingStatusResource > 0) {
        context.getString(user.sharingStatusResource)
    } else {
        ""
    }

    override fun isContentTheSame(item: SharingContactItem) =
        super.isContentTheSame(item) &&
            item is SharedContactUser &&
            user.sharingStatusResource == item.user.sharingStatusResource
}