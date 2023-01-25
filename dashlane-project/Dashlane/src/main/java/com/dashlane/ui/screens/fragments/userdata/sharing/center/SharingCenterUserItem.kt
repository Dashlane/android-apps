package com.dashlane.ui.screens.fragments.userdata.sharing.center

import android.content.Context
import com.dashlane.R
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingContactItem



class SharingCenterUserItem(
    context: Context,
    val user: SharingContact.User
) : SharingContactItem(context, user.name) {
    val itemCount: Int
        get() = user.count

    override fun getLine2(): String {
        return if (itemCount > 0) {
            context.resources.getQuantityString(
                R.plurals.sharing_shared_item_list_item_number_item_shared,
                itemCount,
                itemCount
            )
        } else {
            ""
        }
    }

    override fun isContentTheSame(item: SharingContactItem): Boolean {
        return super.isContentTheSame(item) &&
                item is SharingCenterUserItem && itemCount == item.itemCount
    }
}