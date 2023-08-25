package com.dashlane.item.subview.action.note

import android.content.Context
import android.view.MenuItem
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.item.BaseUiUpdateListener
import com.dashlane.item.subview.action.ItemEditMenuAction
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.ui.adapters.viewedit.SecureNoteCategoryArrayAdapter
import com.dashlane.ui.fragments.BottomSheetListDialogFragment
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.alphabeticalComparator
import com.dashlane.vault.model.createSecureNoteCategory
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.util.Collections

class SecureNoteCategoryMenuAction(
    private val context: Context,
    private val genericDataQuery: GenericDataQuery,
    item: SyncObject.SecureNote,
    private val categorySelectAction: (categoryUid: String?) -> Unit = {},
    updateAction: (VaultItem<*>) -> VaultItem<*>?
) : ItemEditMenuAction(
    R.string.toolbar_menu_title_secure_note_category,
    R.drawable.category,
    MenuItem.SHOW_AS_ACTION_NEVER,
    valueUpdate = updateAction
) {
    var selectedCategory = item.category
        set(value) {
            field = value
            categorySelectAction.invoke(value)
        }
    private val allCategories by lazy { getSecureNoteCategories(context) }

    override fun onClickAction(activity: AppCompatActivity) {
        val selectedCat = allCategories.indexOfFirst { it.id == selectedCategory }
        val dialog = BottomSheetListDialogFragment().apply {
            adapter = SecureNoteCategoryArrayAdapter(
                activity,
                R.layout.list_item_secure_note_category,
                allCategories.toTypedArray(),
                if (selectedCat == -1) 0 else selectedCat
            )
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedCategory = allCategories[position].id
                dismiss()
            }
        }
        dialog.show(activity.supportFragmentManager, BaseUiUpdateListener.BOTTOM_SHEET_DIALOG_TAG)
    }

    private fun getSecureNoteCategories(context: Context): List<SummaryObject.SecureNoteCategory> {
        return mutableListOf<SummaryObject.SecureNoteCategory>().apply {
            add(
                createSecureNoteCategory(
                    dataIdentifier = CommonDataIdentifierAttrsImpl(
                        uid = "",
                        anonymousUID = ""
                    ),
                    title = context.getString(R.string.unspecified_category)
                ).toSummary()
            )
            val allSecureNoteCategories = genericDataQuery.queryAll(
                GenericFilter(dataTypeFilter = SpecificDataTypeFilter(SyncObjectType.SECURE_NOTE_CATEGORY))
            ).mapNotNull { it as? SummaryObject.SecureNoteCategory }
            Collections.sort(allSecureNoteCategories, SyncObject.SecureNoteCategory.alphabeticalComparator)
            addAll(allSecureNoteCategories)
        }
    }
}