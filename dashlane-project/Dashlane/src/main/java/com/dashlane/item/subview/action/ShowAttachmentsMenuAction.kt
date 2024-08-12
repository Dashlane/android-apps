package com.dashlane.item.subview.action

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.vault.summary.SummaryObject

class ShowAttachmentsMenuAction(
    private val item: SummaryObject,
    enabled: Boolean = true
) : MenuAction(
    text = R.string.attachments_activity_title,
    icon = R.drawable.action_bar_menu_attach_file,
    displayFlags = MenuItem.SHOW_AS_ACTION_ALWAYS,
    enabled = enabled
) {

    private var activityColor: Int = -1

    override fun onClickAction(activity: AppCompatActivity) {
        item.showAttachments(activity, activityColor)
    }
}