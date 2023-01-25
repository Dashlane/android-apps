package com.dashlane.notificationcenter.view

import android.content.Context
import android.view.View
import android.widget.TextView
import com.dashlane.R
import com.dashlane.breach.Breach
import com.dashlane.notificationcenter.NotificationCenterDef
import com.dashlane.notificationcenter.NotificationCenterRepository
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.dashlane.ui.drawable.CircleDrawable

data class AlertActionItem(
    val breachWrapper: BreachWrapper,
    override val actionItemsRepository: NotificationCenterRepository,
    override val section: ActionItemSection = ActionItemSection.BREACH_ALERT,
    override val type: ActionItemType = ActionItemType.BREACH_ALERT,
    override val trackingKey: String = breachWrapper.publicBreach.id
) : NotificationItem, DiffUtilComparator<AlertActionItem> {

    override val action: NotificationCenterDef.Presenter.() -> Unit = { startAlertDetails(breachWrapper) }

    override fun getSpanSize(spanCount: Int): Int = spanCount

    override fun getViewType() = VIEW_TYPE

    override fun isItemTheSame(item: AlertActionItem) =
        breachWrapper.publicBreach.id == item.breachWrapper.publicBreach.id

    override fun isContentTheSame(item: AlertActionItem) = this == item

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType<AlertActionItem>(
            R.layout.item_actionitem,
            ViewHolder::class.java
        )
    }

    class ViewHolder(val v: View) : ReadStateViewHolder<AlertActionItem>(v) {

        override fun updateView(context: Context, item: AlertActionItem?) {
            super.updateView(context, item)

            val publicBreach = item?.breachWrapper?.publicBreach ?: return

            val darkWebBreach = publicBreach.isDarkWebBreach()
            when {
                darkWebBreach -> updateDarkWebBreachView(context, publicBreach)
                else -> updateSecurityBreachView(context, publicBreach)
            }
        }

        private fun updateDarkWebBreachView(
            context: Context,
            publicBreach: Breach
        ) {
            setText(R.id.title, R.string.action_item_dm_alert_title)
            setDateField(publicBreach)
            setText(R.id.description, R.string.action_item_dm_alert_description)
            setImageDrawable(
                R.id.icon,
                CircleDrawable.with(
                    context = context,
                    backgroundColorRes = R.color.container_expressive_danger_quiet_idle,
                    drawableRes = R.drawable.ic_action_item_dm_alert,
                    drawableTintColorRes = R.color.text_danger_standard
                )
            )
        }

        private fun updateSecurityBreachView(
            context: Context,
            publicBreach: Breach
        ) {
            setText(R.id.title, R.string.action_item_security_alert_title)
            setDateField(publicBreach)
            setText(
                R.id.description,
                context.getString(R.string.action_item_security_alert_description, publicBreach.title)
            )
            setImageDrawable(
                R.id.icon,
                CircleDrawable.with(
                    context = context,
                    backgroundColorRes = R.color.container_expressive_danger_quiet_idle,
                    drawableRes = R.drawable.ic_action_item_security_alert,
                    drawableTintColorRes = R.color.text_danger_standard
                )
            )
        }

        private fun getCreationDate(publicBreach: Breach): String {
            val accessTimestamp = publicBreach.breachCreationDate * 1000L
            val currentTimeMillis = System.currentTimeMillis()

            return formatDateForNotification(accessTimestamp, currentTimeMillis)
        }

        private fun setDateField(breach: Breach) {
            val dateField = findViewByIdEfficient<TextView>(R.id.date) ?: return
            dateField.text = getCreationDate(breach)
            dateField.visibility = View.VISIBLE
        }
    }
}