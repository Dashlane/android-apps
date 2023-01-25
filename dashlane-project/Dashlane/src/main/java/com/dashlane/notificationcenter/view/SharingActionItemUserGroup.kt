package com.dashlane.notificationcenter.view

import android.content.Context
import android.view.View
import com.dashlane.R
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.databinding.ItemActionitemBinding
import com.dashlane.notificationcenter.NotificationCenterDef
import com.dashlane.notificationcenter.NotificationCenterRepository
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.getUser
import com.dashlane.storage.DataStorageProvider
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.dashlane.ui.drawable.CircleDrawable
import java.time.Instant

data class SharingActionItemUserGroup(
    val sharing: UserGroup,
    val xmlConverter: DataIdentifierSharingXmlConverter,
    val dataStorageProvider: DataStorageProvider,
    val sessionManager: SessionManager,
    override val actionItemsRepository: NotificationCenterRepository,
    override val section: ActionItemSection = ActionItemSection.SHARING,
    override val type: ActionItemType = ActionItemType.SHARING,
    override val trackingKey: String = sharing.groupId
) : NotificationItem, DiffUtilComparator<SharingActionItemUserGroup> {
    override val action: NotificationCenterDef.Presenter.() -> Unit = { startSharingRedirection() }

    var firstDisplayedDate: Instant = Instant.now()

    override fun getViewType() = VIEW_TYPE

    override fun getSpanSize(spanCount: Int) = spanCount

    override fun isItemTheSame(item: SharingActionItemUserGroup) =
        sharing.groupId == item.sharing.groupId

    override fun isContentTheSame(item: SharingActionItemUserGroup) = this == item

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.item_actionitem,
            ViewHolder::class.java
        )
    }

    class ViewHolder(val v: View) : ReadStateViewHolder<SharingActionItemUserGroup>(v) {

        private val binding = ItemActionitemBinding.bind(view)

        override fun updateView(context: Context, item: SharingActionItemUserGroup?) {
            super.updateView(context, item)
            item ?: return
            val referrer = getReferrer(item)

            updateUserGroup(referrer)

            binding.date.text = formatDateForNotification(
                item.firstDisplayedDate.toEpochMilli(),
                Instant.now().toEpochMilli()
            )
            binding.date.visibility = View.VISIBLE
            binding.title.text =
                context.getString(R.string.action_item_sharing_invitation_item_title)
            setImageDrawable(
                R.id.icon, CircleDrawable.with(
                    context = context,
                    backgroundColorRes = R.color.container_expressive_brand_quiet_idle,
                    drawableRes = R.drawable.ic_action_item_sharing_invitation,
                    drawableTintColorRes = R.color.text_brand_standard
                )
            )
        }

        private fun getReferrer(item: SharingActionItemUserGroup?): String {
            val username = item?.sessionManager?.session?.userId ?: return ""
            return item.sharing.getUser(username)?.referrer ?: ""
        }

        private fun updateUserGroup(
            referrer: String
        ) {
            binding.description.text =
                context.getString(
                    R.string.action_item_sharing_invitation_group_description,
                    referrer
                )
        }
    }
}