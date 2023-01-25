package com.dashlane.notificationcenter.promotions

import android.content.Context
import android.view.View
import com.dashlane.R
import com.dashlane.notificationcenter.NotificationCenterDef
import com.dashlane.notificationcenter.NotificationCenterRepository
import com.dashlane.notificationcenter.view.ActionItemSection
import com.dashlane.notificationcenter.view.ActionItemType
import com.dashlane.notificationcenter.view.NotificationItem
import com.dashlane.notificationcenter.view.ReadStateViewHolder
import com.dashlane.premium.offer.common.model.IntroOfferType
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.drawable.CircleDrawable



class IntroOfferActionItem(
    override val actionItemsRepository: NotificationCenterRepository,
    private val introOfferType: IntroOfferType
) : NotificationItem {
    override val section: ActionItemSection = ActionItemSection.PROMOTIONS
    override val type: ActionItemType = ActionItemType.INTRODUCTORY_OFFERS
    override val action: NotificationCenterDef.Presenter.() -> Unit =
        { startUpgrade(offerType = introOfferType.offerType) }

    override fun getSpanSize(spanCount: Int): Int = spanCount

    override fun getViewType() = VIEW_TYPE

    override val trackingKey: String = introOfferType.offerId + ID_SUFFIX

    companion object {
        private const val ID_SUFFIX = "_NOTIFICATION"
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.item_actionitem,
            ViewHolder::class.java
        )
    }

    class ViewHolder(val v: View) : ReadStateViewHolder<IntroOfferActionItem>(v) {

        override fun updateView(context: Context, item: IntroOfferActionItem?) {
            super.updateView(context, item)
            item ?: return

            setText(R.id.title, item.introOfferType.getNotificationTitle(context))
            setText(R.id.description, item.introOfferType.getNotificationDescription(context))
            setImageDrawable(
                R.id.icon,
                CircleDrawable.with(
                    context = context,
                    backgroundColorRes = R.color.container_expressive_brand_quiet_idle,
                    drawableRes = R.drawable.ic_action_item_premium_star,
                    drawableTintColorRes = R.color.text_brand_standard
                )
            )
        }
    }
}
