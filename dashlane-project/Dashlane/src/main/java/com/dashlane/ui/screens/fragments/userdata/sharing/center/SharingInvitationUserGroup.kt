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

class SharingInvitationUserGroup(
    context: Context,
    private val groupInvite: SharingContact.UserGroupInvite,
    private val onClickAccept: () -> Unit,
    private val onClickDecline: () -> Unit
) : MultiColumnViewTypeProvider {
    val displayTitle: String = groupInvite.userGroup.name
    val displaySubtitle: String

    init {
        displaySubtitle = createDisplaySubTitleUserGroup(context)
    }

    override fun getViewType() = VIEW_TYPE

    override fun getSpanSize(spanCount: Int): Int = spanCount

    private fun createDisplaySubTitleUserGroup(context: Context): String {
        val userDownload = groupInvite.userGroup.getUser(groupInvite.login)
        return if (userDownload != null) {
            context.getString(
            R.string.sharing_pending_invite_user_group_description,
            userDownload.referrer
        )
        } else {
            ""
        }
    }

    class ItemViewHolder(itemView: View) :
        EfficientViewHolder<SharingInvitationUserGroup>(itemView) {
        private val viewBinding = SharingPendingInvitationLayoutBinding.bind(view)
        override fun updateView(context: Context, item: SharingInvitationUserGroup?) {
            item ?: return
            viewBinding.sharingPendingInviteTitle.text = item.displayTitle
            updateViewUserGroup(item)
        }

        private fun updateViewUserGroup(
            item: SharingInvitationUserGroup
        ) {
            val accept = viewBinding.sharingPendingInviteBtnAccept
            val refuse = viewBinding.sharingPendingInviteBtnRefuse

            val pendingInvite = item.groupInvite
            val userGroup = pendingInvite.userGroup
            viewBinding.sharingPendingInviteThumbnail.apply {
                thumbnailType = ThumbnailViewType.ICON.value
                iconRes = R.drawable.ic_group_outlined
                isVisible = true
            }
            viewBinding.sharingPendingInviteIcon.isVisible = false
            val userDownload = userGroup.getUser(item.groupInvite.login)
            if (userDownload != null) {
                viewBinding.sharingPendingInviteDescription.text = item.displaySubtitle
            }
            accept.setOnClickListener { item.onClickAccept() }
            refuse.setOnClickListener { item.onClickDecline() }
        }

        override fun isClickable(): Boolean {
            return false
        }
    }

    companion object {
        private val VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<out SharingInvitationUserGroup> =
            DashlaneRecyclerAdapter.ViewType(
                R.layout.sharing_pending_invitation_layout_forwarder,
                ItemViewHolder::class.java
            )

        fun comparator(): Comparator<SharingInvitationUserGroup> =
            compareBy(
                { it.displayTitle },
                { it.displaySubtitle }
            )
    }
}