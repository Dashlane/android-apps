package com.dashlane.ui.activities.fragments.list

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.dashlane.R
import com.dashlane.design.component.compat.view.ThumbnailView
import com.dashlane.design.component.compat.view.ThumbnailViewType
import com.dashlane.teamspaces.TeamSpaceUtils.getTeamSpaceId
import com.dashlane.teamspaces.adapter.TeamspaceDrawableProvider
import com.dashlane.teamspaces.isSpaceItem
import com.dashlane.ui.VaultItemImageHelper
import com.dashlane.ui.activities.fragments.list.action.ListItemAction
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.thumbnail.ThumbnailDomainIconView
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.getThemeAttrDrawable
import com.dashlane.util.toHighlightedSpannable
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText
import com.dashlane.vault.util.isProtected
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

open class ItemWrapperViewHolder(itemView: View) : EfficientViewHolder<VaultItemWrapper<out SummaryObject>>(itemView) {

    public override fun updateView(context: Context, item: VaultItemWrapper<out SummaryObject>?) {
        item?.let {
            updateText(context, item)
            updateImage(item)
            updateActionItem(item)
            updateSharingItem(item)
            updateTeamspaceIcon(item)
            updateAttachmentIcon(item)
            updateLockIcon(item)
            updatePasskeyIcon(item)
        }
    }

    fun updateActionItem(item: VaultItemWrapper<out SummaryObject>, itemActions: List<ListItemAction>) {
        val constraintLayout = findViewByIdEfficient<ConstraintLayout>(R.id.item_layout)!!
        val flowActionsMenu = findViewByIdEfficient<Flow>(R.id.flow_actions)!!

        
        flowActionsMenu.referencedIds.forEach {
            val viewToRemove = constraintLayout.findViewById<View>(it)
            constraintLayout.removeView(viewToRemove)
            flowActionsMenu.removeView(viewToRemove)
        }

        
        itemActions.forEach { itemAction ->
            ImageButton(context).apply {
                id = itemAction.viewId
                setImageResource(itemAction.icon)
                contentDescription = context.getString(itemAction.contentDescription)
                setOnClickListener { v ->
                    itemAction.onClickItemAction(v, item.summaryObject)
                }
                visibility = itemAction.visibility
                background = context.getThemeAttrDrawable(android.R.attr.selectableItemBackground)
                layoutParams = LinearLayout.LayoutParams(
                    context.resources.getDimensionPixelSize(R.dimen.minimum_clickable_area_size),
                    context.resources.getDimensionPixelSize(R.dimen.minimum_clickable_area_size)
                )
                constraintLayout.addView(this)
                flowActionsMenu.addView(this)
            }
        }
    }

    fun updateText(context: Context, item: VaultItemWrapper<out SummaryObject>) {
        val colorHighlight = context.getThemeAttrColor(R.attr.colorSecondary)
        val title = item.getTitle(context)
        setText(R.id.item_title, statusTextToSpannable(title, colorHighlight))
        val description = item.getDescription(context)
        setText(R.id.item_subtitle, statusTextToSpannable(description, colorHighlight))
        val subtitleColorAttr = if (description.isWarning) R.attr.colorError else R.attr.colorOnBackgroundMedium
        setTextColor(R.id.item_subtitle, context.getThemeAttrColor(subtitleColorAttr))
    }

    fun updateImage(item: VaultItemWrapper<out SummaryObject>) {
        val iconView = findViewByIdEfficient<ThumbnailView>(R.id.item_icon) ?: return
        val domainThumbnailView = findViewByIdEfficient<ThumbnailDomainIconView>(R.id.item_thumbnail) ?: return

        val thumbnailViewConfiguration = VaultItemImageHelper.getThumbnailViewConfiguration(item.summaryObject)

        if (thumbnailViewConfiguration.type == ThumbnailViewType.VAULT_ITEM_DOMAIN_ICON) {
            iconView.isVisible = false
            if (domainThumbnailView.domainUrl != thumbnailViewConfiguration.urlDomain) {
                domainThumbnailView.thumbnailUrl = null
            }
            domainThumbnailView.apply {
                isVisible = true
                domainUrl = thumbnailViewConfiguration.urlDomain
            }
        } else {
            domainThumbnailView.isVisible = false
            iconView.apply {
                isVisible = true
                thumbnailType = thumbnailViewConfiguration.type.value
                thumbnailViewConfiguration.iconRes?.let { iconRes = it }
                thumbnailViewConfiguration.colorRes?.let {
                    color = ResourcesCompat.getColor(view.resources, it, null)
                }
            }
        }
    }

    fun updateSharingItem(item: VaultItemWrapper<out SummaryObject>) {
        findViewByIdEfficient<ImageView>(R.id.shared_image)?.isVisible = item.summaryObject.isShared
    }

    fun updateLockIcon(item: VaultItemWrapper<out SummaryObject>) {
        findViewByIdEfficient<ImageView>(R.id.lock_image)?.isVisible = item.summaryObject.isProtected
    }

    fun updatePasskeyIcon(item: VaultItemWrapper<out SummaryObject>) {
        findViewByIdEfficient<ImageView>(R.id.passkey_image)?.isVisible = item.summaryObject is SummaryObject.Passkey
    }

    fun updateTeamspaceIcon(item: VaultItemWrapper<out SummaryObject>) {
        val teamspaceDrawable = getTeamspaceDrawable(item)
        findViewByIdEfficient<ImageView>(R.id.teamspace_image)?.let {
            it.isVisible = teamspaceDrawable != null
            it.setImageDrawable(teamspaceDrawable)
        }
    }

    fun updateAttachmentIcon(item: VaultItemWrapper<out SummaryObject>) {
        findViewByIdEfficient<ImageView>(R.id.attachment_image)?.isVisible = item.isAttachmentIconNeeded
    }

    fun updateActionItem(item: VaultItemWrapper<out SummaryObject>) {
        updateActionItem(item, item.getListItemActions())
    }

    private fun getTeamspaceDrawable(item: VaultItemWrapper<*>): Drawable? {
        if (!item.allowTeamspaceIcon) {
            return null
        }
        val vaultItem = item.summaryObject
        if (!vaultItem.isSpaceItem()) {
            return null
        }
        val teamspaceId = getTeamSpaceId(vaultItem)
        item.teamSpaceAccessorProvider.get()?.let { teamSpaceAccessor ->
            val currentSpaceFilter = item.currentTeamSpaceUiFilter.currentFilter
            if (currentSpaceFilter.teamSpace.teamId == teamspaceId) {
                return null
            }
            val teamspace = teamSpaceAccessor.availableSpaces.firstOrNull { it.teamId == teamspaceId } ?: return null
            return TeamspaceDrawableProvider.getIcon(context, teamspace, R.dimen.teamspace_icon_size_small)
        }
        return null
    }

    private fun statusTextToSpannable(statusText: StatusText, colorHighlight: Int): Spannable {
        val spannable = SpannableString(statusText.text)
        val textToHighlight = statusText.textToHighlight
        return if (textToHighlight != null) {
            spannable.toHighlightedSpannable(textToHighlight, colorHighlight, true)
        } else {
            spannable
        }
    }
}