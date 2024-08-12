package com.dashlane.ui.screens.fragments.userdata.sharing.center

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import com.dashlane.R
import com.dashlane.databinding.SharingPendingInvitationLayoutBinding
import com.dashlane.sharing.model.getUser
import com.dashlane.ui.activities.fragments.list.wrapper.ItemWrapperProvider
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.MultiColumnViewTypeProvider
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.summary.SummaryObject
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class SharingInvitationItem(
    context: Context,
    private val itemWrapperProvider: ItemWrapperProvider,
    private val itemInvite: SharingContact.ItemInvite,
    private val onClickAccept: () -> Unit,
    private val onClickDecline: () -> Unit
) : MultiColumnViewTypeProvider {
    val displayTitle: String
    val displaySubtitle: String

    init {
        displayTitle = createDisplayTitleItemGroup(context)
        displaySubtitle = createDisplaySubTitleItemGroup(context)
    }

    override fun getViewType() = VIEW_TYPE

    override fun getSpanSize(spanCount: Int): Int = spanCount

    private fun createDisplayTitleItemGroup(context: Context): String {
        val summaryObject = itemInvite.item
        val itemWrapper = itemWrapperProvider(
            summaryObject,
            ItemListContext.Container.NONE.asListContext(
                ItemListContext.Section.NONE
            )
        )
        return itemWrapper?.getTitle(context)?.text ?: "?"
    }

    private fun createDisplaySubTitleItemGroup(context: Context): String {
        val userDownload = itemInvite.itemGroup.getUser(itemInvite.login) ?: return ""
        val referrer = userDownload.referrer
        return if (referrer.isNotSemanticallyNull()) {
            context.getString(
                R.string.sharing_pending_invite_item_group_description,
                referrer
            )
        } else {
            ""
        }
    }

    class ItemViewHolder(itemView: View) :
        EfficientViewHolder<SharingInvitationItem>(itemView) {
        private val viewBinding = SharingPendingInvitationLayoutBinding.bind(view)
        override fun updateView(context: Context, item: SharingInvitationItem?) {
            item ?: return
            viewBinding.sharingPendingInviteTitle.text = item.displayTitle
            updateViewItemGroup(item)
        }

        private fun updateViewItemGroup(
            item: SharingInvitationItem
        ) {
            val accept = viewBinding.sharingPendingInviteBtnAccept
            val refuse = viewBinding.sharingPendingInviteBtnRefuse
            val pendingInvite = item.itemInvite
            val vaultItem = pendingInvite.item
            if (vaultItem is SummaryObject.Authentifiant) {
                viewBinding.sharingPendingInviteIcon.apply {
                    domainUrl = vaultItem.urlDomain
                    isVisible = true
                }
            }
            viewBinding.sharingPendingInviteIconRound.isVisible = false
            viewBinding.sharingPendingInviteDescription.text = item.displaySubtitle
            accept.setOnClickListener { item.onClickAccept() }
            refuse.setOnClickListener { item.onClickDecline() }
        }

        override fun isClickable(): Boolean {
            return false
        }
    }

    companion object {
        private val VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<out SharingInvitationItem> =
            DashlaneRecyclerAdapter.ViewType(
                R.layout.sharing_pending_invitation_layout_forwarder,
                ItemViewHolder::class.java
            )

        fun comparator(): Comparator<SharingInvitationItem> =
            compareBy(
                { it.displayTitle },
                { it.displaySubtitle }
            )
    }
}