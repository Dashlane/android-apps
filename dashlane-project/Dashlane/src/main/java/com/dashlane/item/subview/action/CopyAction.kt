package com.dashlane.item.subview.action

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.ui.action.Action
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.summary.SummaryObject

open class CopyAction(
    private val summaryObject: SummaryObject,
    private val copyField: CopyField,
    private val action: (Activity) -> Unit = {},
    private val itemListContext: ItemListContext? = null,
    private val vaultItemCopy: VaultItemCopyService
) : Action {

    override val icon: Int = -1

    override val tintColorRes: Int? = null

    override val text: Int = R.string.copy

    override fun onClickAction(activity: AppCompatActivity) {
        doCopy(activity)
    }

    private fun doCopy(activity: Activity) {
        if (itemListContext != null) {
            vaultItemCopy.handleCopy(
                item = summaryObject,
                copyField = copyField,
                updateLocalUsage = true,
                updateFrequentSearch = true
            )
        } else {
            vaultItemCopy.handleCopy(item = summaryObject, copyField = copyField)
        }
        action.invoke(activity)
    }
}