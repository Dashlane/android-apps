package com.dashlane.ui.activities.fragments.list.wrapper

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.dashlane.R
import com.dashlane.item.subview.quickaction.QuickActionProvider
import com.dashlane.navigation.Navigator
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextResolver
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.graphics.RoundRectDrawable
import com.dashlane.vault.model.getColorResource
import com.dashlane.vault.summary.SummaryObject

class PaymentCreditCardWrapper(
    vaultItemCopyService: VaultItemCopyService,
    quickActionProvider: QuickActionProvider,
    summaryObject: SummaryObject.PaymentCreditCard,
    itemListContext: ItemListContext,
    dataIdentifierListTextResolver: DataIdentifierListTextResolver,
    navigator: Navigator,
    teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    currentSpaceFilterRepository: CurrentTeamSpaceUiFilter
) : DefaultVaultItemWrapper<SummaryObject.PaymentCreditCard>(
    vaultItemCopyService,
    quickActionProvider,
    summaryObject,
    itemListContext,
    navigator,
    dataIdentifierListTextResolver,
    teamSpaceAccessorProvider,
    currentSpaceFilterRepository
) {

    override fun getImageDrawable(context: Context): Drawable {
        val color = summaryObject.color.getColorResource()
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