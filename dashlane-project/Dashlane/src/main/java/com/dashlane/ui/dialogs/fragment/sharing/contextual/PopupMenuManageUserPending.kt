package com.dashlane.ui.dialogs.fragment.sharing.contextual

import android.content.Context
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.dashlane.R

class PopupMenuManageUserPending(
    context: Context,
    anchor: View,
    private val onResendInvite: () -> Unit,
    private val onCancelInvite: () -> Unit
) : PopupMenu(context, anchor), PopupMenu.OnMenuItemClickListener {

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.resend_invite -> onResendInvite()
            R.id.cancel_invite -> onCancelInvite()
            else -> Unit
        }
        return true
    }

    init {
        menuInflater.inflate(R.menu.sharing_action_user_pending_menu, menu)
        setOnMenuItemClickListener(this)
    }
}