package com.dashlane.item.subview.action.payment

import android.view.MenuItem
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.item.BaseUiUpdateListener
import com.dashlane.item.subview.action.ItemEditMenuAction
import com.dashlane.ui.adapters.viewedit.CreditCardColorArrayAdapter
import com.dashlane.ui.fragments.BottomSheetListDialogFragment
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

class CreditCardColorMenuAction(
    item: SummaryObject.PaymentCreditCard,
    private val colorSelectAction: (SyncObject.PaymentCreditCard.Color) -> Unit = {},
    updateAction: (VaultItem<*>) -> VaultItem<*>?
) : ItemEditMenuAction(
    R.string.toolbar_menu_title_secure_note_color,
    R.drawable.color,
    MenuItem.SHOW_AS_ACTION_ALWAYS,
    valueUpdate = updateAction
) {
    var selectedColor = item.color ?: SyncObject.PaymentCreditCard.Color.NO_TYPE
        set(value) {
            field = value
            colorSelectAction.invoke(value)
        }

    override fun onClickAction(activity: AppCompatActivity) {
        val allColors =
            SyncObject.PaymentCreditCard.Color.values().filter { it != SyncObject.PaymentCreditCard.Color.NO_TYPE }
        val colorPos = allColors.indexOfFirst { it == selectedColor }
        val dialog = BottomSheetListDialogFragment().apply {
            adapter = CreditCardColorArrayAdapter(activity, allColors, colorPos)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedColor = allColors[position]
                dismiss()
            }
        }

        dialog.show(activity.supportFragmentManager, BaseUiUpdateListener.BOTTOM_SHEET_DIALOG_TAG)
    }
}