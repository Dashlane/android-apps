package com.dashlane.ui.dialogs.fragment.sharing.contextual

import android.content.Context
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.dashlane.R
import com.dashlane.core.domain.sharing.SharingPermission

class PopupMenuManageUserAccepted(
    context: Context,
    anchor: View,
    isAdmin: Boolean,
    private val onChangePermission: (SharingPermission) -> Unit,
    private val onAskRevokeUser: () -> Unit
) : PopupMenu(context, anchor), PopupMenu.OnMenuItemClickListener {

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.revoke -> onAskRevokeUser()
            R.id.grant_admin -> onChangePermission(SharingPermission.ADMIN)
            R.id.revoke_admin -> onChangePermission(SharingPermission.LIMITED)
            else -> Unit
        }
        return true
    }

    init {
        menuInflater.inflate(R.menu.sharing_action_admin_menu, menu)
        if (isAdmin) {
            menu.removeItem(R.id.grant_admin)
        } else {
            menu.removeItem(R.id.revoke_admin)
        }
        setOnMenuItemClickListener(this)
    }
}