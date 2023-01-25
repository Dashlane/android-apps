package com.dashlane.ui.screens.fragments.userdata.sharing.center

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.dashlane.R
import com.dashlane.sharing.model.getUser
import com.dashlane.ui.activities.fragments.list.wrapper.toItemWrapper
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.MultiColumnViewTypeProvider
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.isNotSemanticallyNull
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder



class SharingInvitationItem(
    context: Context,
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
        val itemWrapper = summaryObject.toItemWrapper(
            ItemListContext.Container.NONE.asListContext(
                ItemListContext.Section.NONE
            )
        )
        return itemWrapper?.getTitle(context)?.text ?: "?"
    }

    private fun createDisplaySubTitleItemGroup(context: Context): String {
        val userDownload = itemInvite.itemGroup.getUser(itemInvite.login) ?: return ""
        val referrer = userDownload.referrer
        return if (referrer.isNotSemanticallyNull()) context.getString(
            R.string.sharing_pending_invite_item_group_description,
            referrer
        ) else ""
    }

    class ItemViewHolder(itemView: View) :
        EfficientViewHolder<SharingInvitationItem>(itemView) {

        override fun updateView(context: Context, item: SharingInvitationItem?) {
            item ?: return
            findViewByIdEfficient<TextView>(R.id.sharing_pending_invite_title)?.text =
                item.displayTitle
            updateViewItemGroup(context, item)
        }

        private fun updateViewItemGroup(
            context: Context,
            item: SharingInvitationItem
        ) {
            val accept = findViewByIdEfficient<Button>(R.id.sharing_pending_invite_btn_accept)
            val refuse = findViewByIdEfficient<Button>(R.id.sharing_pending_invite_btn_refuse)
            val pendingInvite = item.itemInvite
            findViewByIdEfficient<TextView>(R.id.sharing_pending_invite_title)?.text =
                item.displayTitle
            val title = item.displaySubtitle
            val vaultItem = pendingInvite.item
            val itemWrapper = vaultItem.toItemWrapper(
                ItemListContext.Container.NONE.asListContext(ItemListContext.Section.NONE)
            )!!
            val image = itemWrapper.getImageDrawable(context)
            findViewByIdEfficient<ImageView>(R.id.sharing_pending_invite_icon)?.setImageDrawable(
                image
            )
            findViewByIdEfficient<TextView>(R.id.sharing_pending_invite_description)?.text = title
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