package com.dashlane.notificationcenter.view

import android.content.Context
import android.view.View
import com.dashlane.R
import com.dashlane.core.sharing.SharingDao
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.databinding.ItemActionitemBinding
import com.dashlane.notificationcenter.NotificationCenterDef
import com.dashlane.notificationcenter.NotificationCenterRepository
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.getUser
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.dashlane.ui.drawable.CircleDrawable
import com.dashlane.vault.model.titleForList
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummaryOrNull
import java.time.Instant

data class SharingActionItemItemGroup(
    val sharing: ItemGroup,
    val xmlConverter: DataIdentifierSharingXmlConverter,
    val sessionManager: SessionManager,
    val sharingDao: SharingDao,
    override val actionItemsRepository: NotificationCenterRepository,
    override val section: ActionItemSection = ActionItemSection.SHARING,
    override val type: ActionItemType = ActionItemType.SHARING,
    override val trackingKey: String = sharing.groupId
) : NotificationItem, DiffUtilComparator<SharingActionItemItemGroup> {
    override val action: NotificationCenterDef.Presenter.() -> Unit = { startSharingRedirection() }

    var firstDisplayedDate: Instant = Instant.now()

    override fun getViewType() = VIEW_TYPE

    override fun getSpanSize(spanCount: Int) = spanCount

    override fun isItemTheSame(item: SharingActionItemItemGroup) =
        sharing.groupId == item.sharing.groupId

    override fun isContentTheSame(item: SharingActionItemItemGroup) = this == item

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.item_actionitem,
            ViewHolder::class.java
        )
    }

    class ViewHolder(val v: View) : ReadStateViewHolder<SharingActionItemItemGroup>(v) {

        private val binding = ItemActionitemBinding.bind(view)

        override fun updateView(context: Context, item: SharingActionItemItemGroup?) {
            super.updateView(context, item)
            item ?: return
            val sharingItem = item.sharing
            val referrer = getReferrer(item)
            updateItemGroup(sharingItem, item, referrer)

            binding.date.text = formatDateForNotification(
                item.firstDisplayedDate.toEpochMilli(),
                Instant.now().toEpochMilli()
            )
            binding.date.visibility = View.VISIBLE
            binding.title.text =
                context.getString(R.string.action_item_sharing_invitation_item_title)
            setImageDrawable(
                R.id.icon,
                CircleDrawable.with(
                    context = context,
                    backgroundColorRes = R.color.container_expressive_brand_quiet_idle,
                    drawableRes = R.drawable.ic_action_item_sharing_invitation,
                    drawableTintColorRes = R.color.text_brand_standard
                )
            )
        }

        private fun getReferrer(item: SharingActionItemItemGroup?): String {
            val username = item?.sessionManager?.session?.userId ?: return ""
            return item.sharing.getUser(username)?.referrer ?: ""
        }

        private fun updateItemGroup(
            itemGroup: ItemGroup,
            actionItem: SharingActionItemItemGroup,
            referrer: String
        ) {
            val itemId = itemGroup.items?.firstOrNull()?.itemId ?: return
            val extraData = actionItem.sharingDao.loadItemContentExtraDataLegacy(itemId)
            val vaultItem = actionItem.xmlConverter.fromXml(itemId, extraData)?.vaultItem
            val item: SummaryObject? = vaultItem?.toSummaryOrNull()

            binding.description.text = when (item) {
                is SummaryObject.Authentifiant -> context.getString(
                    R.string
                        .action_item_sharing_invitation_item_description_password,
                    referrer,
                    item.titleForList
                )
                is SummaryObject.SecureNote -> context.getString(
                    R.string.action_item_sharing_invitation_item_description_secure_note,
                    referrer
                )
                else -> ""
            }
        }
    }
}