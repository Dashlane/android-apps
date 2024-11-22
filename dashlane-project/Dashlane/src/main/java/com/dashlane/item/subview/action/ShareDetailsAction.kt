package com.dashlane.item.subview.action

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.ui.action.Action
import com.dashlane.vault.model.VaultItem

class ShareDetailsAction(private val vaultItem: VaultItem<*>) : Action {

    override val icon: Int = R.drawable.ic_info_24

    override val tintColorRes: Int? = null

    override val text: Int = R.string.and_accessibility_info

    override fun onClickAction(activity: AppCompatActivity) {
        vaultItem.showSharing(SHOW_SHARING_DETAILS_REQUEST_CODE, activity)
    }

    companion object {
        const val SHOW_SHARING_DETAILS_REQUEST_CODE = 6244

        const val EXTRA_NOTIFY_UID_CHANGES = "extraNotifyUidChanges"

        const val EXTRA_UID_CHANGED = "extraUidChanged"
    }
}