package com.dashlane.item.subview.action

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.vault.model.VaultItem



class ShowAttachmentsMenuAction(private val item: VaultItem<*>) : MenuAction(
    R.string.attachments_activity_title,
    R.drawable.action_bar_menu_attach_file,
    MenuItem.SHOW_AS_ACTION_ALWAYS
) {

    var activityColor: Int = -1

    override fun onClickAction(activity: AppCompatActivity) {
        item.showAttachments(activity, activityColor)
    }
}