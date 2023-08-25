package com.dashlane.item.subview.action.note

import android.view.MenuItem
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.item.BaseUiUpdateListener
import com.dashlane.item.subview.action.ItemEditMenuAction
import com.dashlane.ui.adapters.viewedit.SecureNoteColorArrayAdapter
import com.dashlane.ui.fragments.BottomSheetListDialogFragment
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

class SecureNoteColorMenuAction(
    item: VaultItem<SyncObject.SecureNote>,
    private val colorSelectAction: (SyncObject.SecureNoteType) -> Unit = {},
    updateAction: (VaultItem<*>) -> VaultItem<*>?
) : ItemEditMenuAction(
    R.string.toolbar_menu_title_secure_note_color,
    R.drawable.color,
    MenuItem.SHOW_AS_ACTION_NEVER,
    valueUpdate = updateAction
) {
    var selectedType = item.syncObject.type ?: SyncObject.SecureNoteType.NO_TYPE
        set(value) {
            field = value
            colorSelectAction.invoke(value)
        }

    override fun onClickAction(activity: AppCompatActivity) {
        val allNoteTypes = SyncObject.SecureNoteType.values().filter { it != SyncObject.SecureNoteType.NO_TYPE }
        val colorPos = allNoteTypes.indexOf(selectedType)
        val dialog = BottomSheetListDialogFragment().apply {
            adapter = SecureNoteColorArrayAdapter(activity, allNoteTypes, colorPos)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedType = allNoteTypes[position]
                dismiss()
            }
        }

        dialog.show(activity.supportFragmentManager, BaseUiUpdateListener.BOTTOM_SHEET_DIALOG_TAG)
    }
}