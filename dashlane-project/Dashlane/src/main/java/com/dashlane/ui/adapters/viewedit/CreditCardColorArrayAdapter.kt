package com.dashlane.ui.adapters.viewedit

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.dashlane.R
import com.dashlane.vault.model.getColorResource
import com.dashlane.xml.domain.SyncObject

class CreditCardColorArrayAdapter(
    context: Context,
    objects: List<SyncObject.PaymentCreditCard.Color>,
    selectedPos: Int
) : ColorSelectionAdapter<SyncObject.PaymentCreditCard.Color>(context, objects, selectedPos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getViewWithIconAndLabel(
            position,
            convertView,
            parent,
            mData[position].getColorResource(),
            getColorNameStringId(mData[position])
        )
    }

    private fun getColorNameStringId(color: SyncObject.PaymentCreditCard.Color): Int {
        return when (color) {
            SyncObject.PaymentCreditCard.Color.NO_TYPE -> R.string.creditcard_color_no_type
            SyncObject.PaymentCreditCard.Color.RED -> R.string.creditcard_color_red
            SyncObject.PaymentCreditCard.Color.GOLD -> R.string.creditcard_color_gold
            SyncObject.PaymentCreditCard.Color.GREEN_1 -> R.string.creditcard_color_green
            SyncObject.PaymentCreditCard.Color.WHITE -> R.string.creditcard_color_white
            SyncObject.PaymentCreditCard.Color.GREEN_2 -> R.string.creditcard_color_amex_green
            SyncObject.PaymentCreditCard.Color.SILVER -> R.string.creditcard_color_silver
            SyncObject.PaymentCreditCard.Color.BLACK -> R.string.creditcard_color_black
            SyncObject.PaymentCreditCard.Color.BLUE_1 -> R.string.creditcard_color_blue
            SyncObject.PaymentCreditCard.Color.BLUE_2 -> R.string.creditcard_color_dark_blue
            SyncObject.PaymentCreditCard.Color.ORANGE -> R.string.creditcard_color_orange
        }
    }
}