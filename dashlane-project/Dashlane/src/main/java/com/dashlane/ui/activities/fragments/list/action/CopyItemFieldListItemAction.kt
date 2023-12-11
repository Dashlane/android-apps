package com.dashlane.ui.activities.fragments.list.action

import android.view.View
import com.dashlane.R
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.summary.SummaryObject

class CopyItemFieldListItemAction(
    private val item: SummaryObject,
    private val itemContainer: ItemListContext,
    private val vaultItemCopyService: VaultItemCopyService
) : ListItemAction {

    override val icon: Int = R.drawable.ic_item_action_copy

    override val contentDescription: Int = R.string.and_accessibility_copy_password

    override val visibility: Int
        get() {
            if (vaultItemCopyService.hasContent(item = item, copyField = CopyField.Password)) {
                return View.VISIBLE
            }
            return View.INVISIBLE
        }

    override fun onClickItemAction(v: View, item: SummaryObject) {
        if (!vaultItemCopyService.hasContent(item, CopyField.Password)) {
            return
        }
        vaultItemCopyService.handleCopy(item = item, copyField = CopyField.Password, itemListContext = itemContainer)
    }
}