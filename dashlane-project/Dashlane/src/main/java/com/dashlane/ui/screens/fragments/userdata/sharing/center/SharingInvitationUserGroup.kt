package com.dashlane.ui.screens.fragments.userdata.sharing.center

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.dashlane.R
import com.dashlane.sharing.model.getUser
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.MultiColumnViewTypeProvider
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.graphics.RoundRectDrawable
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
        override fun updateView(context: Context, item: SharingInvitationUserGroup?) {
            item ?: return
            (findViewByIdEfficient<View>(R.id.sharing_pending_invite_title) as TextView?)!!.text =
                item.displayTitle
            updateViewUserGroup(context, item)
        }

        private fun updateViewUserGroup(
            context: Context,
            item: SharingInvitationUserGroup
        ) {
            val accept = findViewByIdEfficient<Button>(R.id.sharing_pending_invite_btn_accept)
            val refuse = findViewByIdEfficient<Button>(R.id.sharing_pending_invite_btn_refuse)

            val pendingInvite = item.groupInvite
            val userGroup = pendingInvite.userGroup

            val image = RoundRectDrawable(context, context.getThemeAttrColor(R.attr.colorSecondary))
            image.image = AppCompatResources.getDrawable(context, R.drawable.ic_sharing_user_group)
            findViewByIdEfficient<ImageView>(R.id.sharing_pending_invite_icon)?.setImageDrawable(
                image
            )

            val userDownload = userGroup.getUser(item.groupInvite.login)
            if (userDownload != null) {
                findViewByIdEfficient<TextView>(R.id.sharing_pending_invite_description)?.text =
                    item.displaySubtitle
            }
            accept?.setOnClickListener {
                item.onClickAccept()
            }
            refuse?.setOnClickListener {
                item.onClickDecline()
            }
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