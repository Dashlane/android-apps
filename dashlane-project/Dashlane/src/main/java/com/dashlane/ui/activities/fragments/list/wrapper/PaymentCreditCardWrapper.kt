package com.dashlane.ui.activities.fragments.list.wrapper

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.dashlane.R
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.graphics.RoundRectDrawable
import com.dashlane.vault.model.getColorResource
import com.dashlane.vault.summary.SummaryObject



class PaymentCreditCardWrapper(
    vaultItem: SummaryObject.PaymentCreditCard,
    container: ItemListContext
) : DefaultVaultItemWrapper<SummaryObject.PaymentCreditCard>(vaultItem, container) {

    override fun getImageDrawable(context: Context): Drawable {
        val color = itemObject.color.getColorResource()
        val creditCardColor = ContextCompat.getColor(context, color)
        val cardDrawableResId: Int
        val isWhite =
            Color.red(creditCardColor) > 230 && Color.green(creditCardColor) > 230 && Color.blue(creditCardColor) > 230
        cardDrawableResId = if (isWhite) {
            R.drawable.ico_list_card_on_white
        } else {
            R.drawable.ico_list_card
        }
        return RoundRectDrawable.newWithImage(context, creditCardColor, cardDrawableResId)
    }
}