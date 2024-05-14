package com.dashlane.ui.activities.fragments.list.wrapper

import android.content.Context
import android.graphics.drawable.Drawable
import com.dashlane.item.subview.quickaction.QuickActionProvider
import com.dashlane.navigation.Navigator
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.ui.VaultItemImageHelper
import com.dashlane.ui.activities.fragments.list.action.CopyItemFieldListItemAction
import com.dashlane.ui.activities.fragments.list.action.ListItemAction
import com.dashlane.ui.activities.fragments.list.action.QuickActionsItemAction
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextResolver
import com.dashlane.util.ViewTypeUtils
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.hasAttachments
import com.dashlane.vault.util.valueOfFromDataIdentifier
import com.dashlane.xml.domain.SyncObjectType

open class DefaultVaultItemWrapper<D : SummaryObject>(
    override val vaultItemCopyService: VaultItemCopyService,
    override val quickActionProvider: QuickActionProvider,
    override val summaryObject: D,
    override val itemListContext: ItemListContext,
    override val navigator: Navigator,
    private val dataIdentifierListTextResolver: DataIdentifierListTextResolver,
    override val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    override val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter
) : VaultItemWrapper<D> {

    override var allowTeamspaceIcon: Boolean = false
    private var overrideViewType: DashlaneRecyclerAdapter.ViewType<VaultItemWrapper<out SummaryObject>>? = null

    override val dataType: SyncObjectType?
        get() = summaryObject.valueOfFromDataIdentifier()

    override val isAttachmentIconNeeded: Boolean
        get() = summaryObject.hasAttachments()

    override fun getTitle(context: Context): StatusText =
        dataIdentifierListTextResolver.getLine1(context, summaryObject)

    override fun getDescription(context: Context): StatusText =
        dataIdentifierListTextResolver.getLine2(context, summaryObject)

    override fun getImageDrawable(context: Context): Drawable? =
        VaultItemImageHelper.getIconDrawableFromSummaryObject(context, summaryObject)

    override fun getListItemActions(): List<ListItemAction> {
        return listOfNotNull(
            CopyItemFieldListItemAction(summaryObject, itemListContext, vaultItemCopyService),
            QuickActionsItemAction(quickActionProvider, summaryObject, itemListContext, navigator)
        )
    }

    override fun getSpanSize(spanCount: Int): Int = 1

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<VaultItemWrapper<out SummaryObject>> {
        return overrideViewType ?: ViewTypeUtils.DEFAULT_ITEM_WRAPPER_VIEW_TYPE
    }

    override fun setViewType(overrideViewType: DashlaneRecyclerAdapter.ViewType<VaultItemWrapper<out SummaryObject>>) {
        this.overrideViewType = overrideViewType
    }

    override fun isItemTheSame(item: VaultItemWrapper<out SummaryObject>): Boolean {
        return item.summaryObject.id == summaryObject.id
    }

    override fun isContentTheSame(item: VaultItemWrapper<out SummaryObject>): Boolean = false
}