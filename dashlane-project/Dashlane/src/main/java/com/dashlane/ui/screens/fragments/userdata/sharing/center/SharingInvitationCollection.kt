package com.dashlane.ui.screens.fragments.userdata.sharing.center

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import com.dashlane.R
import com.dashlane.databinding.SharingPendingInvitationLayoutBinding
import com.dashlane.design.component.compat.view.ThumbnailViewType
import com.dashlane.sharing.model.getUser
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.MultiColumnViewTypeProvider
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class SharingInvitationCollection(
    context: Context,
    private val collectionInvite: SharingContact.CollectionInvite,
    private val onClickAccept: () -> Unit,
    private val onClickDecline: () -> Unit
) : MultiColumnViewTypeProvider {
    val displayTitle: String = collectionInvite.collection.name
    val displaySubtitle: String

    init {
        displaySubtitle = createDisplaySubTitleCollection(context)
    }

    override fun getViewType() = VIEW_TYPE

    override fun getSpanSize(spanCount: Int): Int = spanCount

    private fun createDisplaySubTitleCollection(context: Context): String {
        val userCollectionDownload = collectionInvite.collection.getUser(collectionInvite.login)
        return if (userCollectionDownload != null) {
            context.getString(
                R.string.sharing_pending_invite_collection_description,
                userCollectionDownload.referrer
            )
        } else {
            ""
        }
    }

    class ItemViewHolder(itemView: View) :
        EfficientViewHolder<SharingInvitationCollection>(itemView) {
        private val viewBinding = SharingPendingInvitationLayoutBinding.bind(view)
        override fun updateView(context: Context, item: SharingInvitationCollection?) {
            item ?: return
            viewBinding.sharingPendingInviteTitle.text = item.displayTitle
            updateViewCollection(item)
        }

        private fun updateViewCollection(item: SharingInvitationCollection) {
            val accept = viewBinding.sharingPendingInviteBtnAccept
            val refuse = viewBinding.sharingPendingInviteBtnRefuse
            viewBinding.sharingPendingInviteThumbnail.apply {
                thumbnailType = ThumbnailViewType.ICON.value
                iconRes = R.drawable.ic_collection_outlined
                isVisible = true
            }
            viewBinding.sharingPendingInviteIcon.isVisible = false
            val userCollectionDownload =
                item.collectionInvite.collection.getUser(item.collectionInvite.login)
            if (userCollectionDownload != null) {
                viewBinding.sharingPendingInviteDescription.text = item.displaySubtitle
            }
            accept.setOnClickListener { item.onClickAccept() }
            refuse.setOnClickListener { item.onClickDecline() }
        }

        override fun isClickable() = false
    }

    companion object {
        private val VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<out SharingInvitationCollection> =
            DashlaneRecyclerAdapter.ViewType(
                R.layout.sharing_pending_invitation_layout_forwarder,
                ItemViewHolder::class.java
            )

        fun comparator(): Comparator<SharingInvitationCollection> =
            compareBy(
                { it.displayTitle },
                { it.displaySubtitle }
            )
    }
}