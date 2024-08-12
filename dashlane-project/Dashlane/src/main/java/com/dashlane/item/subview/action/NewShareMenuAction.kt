package com.dashlane.item.subview.action

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.vault.summary.SummaryObject

class NewShareMenuAction(
    item: SummaryObject,
    restrictionNotificator: TeamSpaceRestrictionNotificator,
    enabled: Boolean = true
) : MenuAction(
    text = R.string.share_from_services_menu_title,
    icon = R.drawable.ic_share,
    displayFlags = MenuItem.SHOW_AS_ACTION_ALWAYS,
    enabled = enabled
) {
    private val newShareAction = NewShareAction(item, restrictionNotificator)

    override fun onClickAction(activity: AppCompatActivity) {
        return newShareAction.onClickAction(activity)
    }
}