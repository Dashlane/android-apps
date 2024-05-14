package com.dashlane.premium.offer.list.view

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import com.dashlane.premium.R
import com.dashlane.util.BadgeViewOptionalText
import com.dashlane.util.TextViewOptionalText
import com.dashlane.util.TextViewText
import com.google.android.material.card.MaterialCardView

class OfferCardWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    ContextThemeWrapper(context, R.style.ThemeOverlay_Dashlane_Shape),
    attrs,
    defStyleAttr
) {
    private val cardView: MaterialCardView

    init {
        val themeWithShape = ContextThemeWrapper(context, R.style.ThemeOverlay_Dashlane_Shape)
        inflate(themeWithShape, R.layout.offer_list_card_item, this)
        cardView = findViewById(R.id.offer_list_card_card)
    }

    var title by TextViewText(findViewById(R.id.offer_list_card_title))
    var description by TextViewText(findViewById(R.id.offer_list_card_description))
    var billedPrice by TextViewOptionalText(
        findViewById(R.id.offer_list_card_billed_price),
        INVISIBLE
    )
    var barredText by TextViewOptionalText(
        findViewById(R.id.offer_list_card_barred_text),
        INVISIBLE,
        Paint.STRIKE_THRU_TEXT_FLAG
    )
    var additionalInfo by TextViewOptionalText(
        findViewById(R.id.offer_list_card_additional_info),
        INVISIBLE
    )
    var onGoingLabel by TextViewOptionalText(
        findViewById(R.id.offer_list_card_on_going_label),
        INVISIBLE
    )
    var offerCallOut by BadgeViewOptionalText(
        findViewById(R.id.offer_list_card_offer_call_out_tag),
        INVISIBLE
    )

    override fun setOnClickListener(l: OnClickListener?) {
        cardView.setOnClickListener(l)
    }
}