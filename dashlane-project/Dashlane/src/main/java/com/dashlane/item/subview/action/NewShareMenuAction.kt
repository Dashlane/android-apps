package com.dashlane.item.subview.action

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.toSummary

class NewShareMenuAction(item: VaultItem<*>, teamspaceManager: TeamspaceManager?) : MenuAction(
    R.string.share_from_services_menu_title,
    R.drawable.ic_share,
    MenuItem.SHOW_AS_ACTION_ALWAYS
) {
    private val newShareAction = NewShareAction(item.toSummary(), teamspaceManager)

    override fun onClickAction(activity: AppCompatActivity) {
        return newShareAction.onClickAction(activity)
    }
}