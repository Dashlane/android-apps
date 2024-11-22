package com.dashlane.item.subview.action

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.ui.action.Action
import com.dashlane.vault.model.VaultItem

class AttachmentDetailsAction(private val item: VaultItem<*>) : Action {
    override val icon: Int = R.drawable.ic_info_24

    override val tintColorRes: Int? = null

    override val text: Int = R.string.and_accessibility_info

    override fun onClickAction(activity: AppCompatActivity) {
        item.showAttachments(activity)
    }
}