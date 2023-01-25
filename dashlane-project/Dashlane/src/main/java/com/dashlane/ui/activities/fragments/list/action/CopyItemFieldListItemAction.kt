package com.dashlane.ui.activities.fragments.list.action

import android.view.View
import com.dashlane.R
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.vault.clipboard.VaultItemCopyUtil
import com.dashlane.vault.clipboard.VaultItemCopyUtil.handleCopy
import com.dashlane.vault.summary.SummaryObject



class CopyItemFieldListItemAction(private val item: SummaryObject, private val itemContainer: ItemListContext) :
    ListItemAction {

    override val icon: Int = R.drawable.ic_item_action_copy

    override val contentDescription: Int = R.string.and_accessibility_copy_password

    override val visibility: Int
        get() {
            if (VaultItemCopyUtil.hasContent(item, CopyField.Password)) {
                return View.VISIBLE
            }
            return View.INVISIBLE
        }

    override fun onClickItemAction(v: View, item: SummaryObject) {
        if (!VaultItemCopyUtil.hasContent(item, CopyField.Password)) {
            return
        }
        handleCopy(item, CopyField.Password, itemContainer)
        ActionItemLogger.create().sendMainContentCopiedLog(item, itemContainer)
    }
}