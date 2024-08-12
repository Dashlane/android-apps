package com.dashlane.ui.screens.fragments.userdata.sharing.center

import android.content.Context
import com.dashlane.R
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingContactItem

class SharingCenterUserGroupItem(
    context: Context,
    val userGroup: SharingContact.UserGroup
) : SharingContactItem(context, userGroup.name) {

    private val memberCount: Int
        get() = userGroup.memberCount
    private val itemCount: Int
        get() = userGroup.itemCount

    override fun getLine2(): String =
        getSharingDetailsText(context, itemCount, memberCount)

    private fun getSharingDetailsText(context: Context, itemCount: Int, memberCount: Int): String {
        val resources = context.resources
        val itemCountString: String = if (itemCount == 0) {
            context.getString(R.string.sharing_shared_group_items_zero)
        } else {
            resources.getQuantityString(
                R.plurals.sharing_shared_group_items,
                itemCount,
                itemCount
            )
        }
        val memberCountString = resources.getQuantityString(
            R.plurals.sharing_shared_group_members,
            memberCount,
            memberCount
        )
        return if (memberCount == 0) {
            itemCountString
        } else {
            context.getString(
                R.string.sharing_shared_group_items_and_members,
                itemCountString,
                memberCountString
            )
        }
    }

    override fun isContentTheSame(item: SharingContactItem): Boolean {
        return super.isContentTheSame(item) &&
                item is SharingCenterUserGroupItem &&
                itemCount == item.itemCount &&
                memberCount == item.memberCount
    }
}
