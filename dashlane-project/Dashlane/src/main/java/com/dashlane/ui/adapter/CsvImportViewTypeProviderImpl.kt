package com.dashlane.ui.adapter

import android.content.Context
import android.view.View
import android.widget.CheckBox
import com.dashlane.R
import com.dashlane.csvimport.CsvImportViewTypeProvider
import com.dashlane.ui.activities.fragments.list.ItemWrapperViewHolder
import com.dashlane.ui.activities.fragments.list.action.ListItemAction
import com.dashlane.ui.activities.fragments.list.wrapper.DefaultVaultItemWrapper
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemDoubleWrapper
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.adapter.ItemListContext.Container
import com.dashlane.util.ViewTypeUtils
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class CsvImportViewTypeProviderImpl(
    itemWrapper: VaultItemWrapper<SummaryObject.Authentifiant>,
    override var selected: Boolean
) : VaultItemDoubleWrapper<SummaryObject.Authentifiant>(itemWrapper),
    CsvImportViewTypeProvider {

    class Factory @Inject constructor() : CsvImportViewTypeProvider.Factory {

        override fun create(
            authentifiant: VaultItem<SyncObject.Authentifiant>,
            selected: Boolean
        ) = CsvImportViewTypeProviderImpl(
            DefaultVaultItemWrapper(authentifiant.toSummary(), Container.CSV_IMPORT.asListContext()),
            selected
        )
    }

    
    override fun getListItemActions(): List<ListItemAction> = emptyList()

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<VaultItemWrapper<out SummaryObject>> = VIEW_TYPE

    class ViewHolder(itemView: View) : ItemWrapperViewHolder(itemView) {

        override fun updateView(context: Context, item: VaultItemWrapper<out SummaryObject>?) {
            super.updateView(context, item)
            updateCheckbox(item)
        }

        private fun updateCheckbox(item: VaultItemWrapper<*>?) {
            val checkBox: CheckBox = findViewByIdEfficient(R.id.checkbox)!!
            if (item is CsvImportViewTypeProvider) {
                checkBox.visibility = View.VISIBLE
                checkBox.isChecked = item.selected
            }
        }
    }

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            ViewTypeUtils.DEFAULT_ITEM_WRAPPER_VIEW_TYPE.layoutResId,
            ViewHolder::class.java
        )
    }
}