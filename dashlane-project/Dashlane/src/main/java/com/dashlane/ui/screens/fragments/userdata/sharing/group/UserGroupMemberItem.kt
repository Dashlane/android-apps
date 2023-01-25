package com.dashlane.ui.screens.fragments.userdata.sharing.group

import android.content.Context
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingContactItem
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingUserGroupUser



class UserGroupMemberItem(
    context: Context,
    private val user: SharingUserGroupUser,
) : SharingContactItem(context, user.user.userId) {

    override fun isContentTheSame(item: SharingContactItem): Boolean {
        return super.isContentTheSame(item) &&
                item is UserGroupMemberItem &&
                user.sharingStatusResource == item.user.sharingStatusResource
    }

    override fun getLine2(): String = context.getString(user.sharingStatusResource)
}
