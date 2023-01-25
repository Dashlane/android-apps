package com.dashlane.ui.activities.fragments.list.wrapper

import android.content.Context
import android.graphics.drawable.Drawable
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.ui.VaultItemImageHelper
import com.dashlane.ui.activities.fragments.list.action.CopyItemFieldListItemAction
import com.dashlane.ui.activities.fragments.list.action.ListItemAction
import com.dashlane.ui.activities.fragments.list.action.QuickActionsItemAction
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextResolver
import com.dashlane.util.ViewTypeUtils
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.IdentityUtil
import com.dashlane.vault.util.attachmentsAllowed
import com.dashlane.vault.util.hasAttachments
import com.dashlane.vault.util.valueOfFromDataIdentifier
import com.dashlane.xml.domain.SyncObjectType



open class DefaultVaultItemWrapper<D : SummaryObject> constructor(
    override val itemObject: D,
    override val itemListContext: ItemListContext
) : VaultItemWrapper<D> {
    private var overrideViewType: DashlaneRecyclerAdapter.ViewType<VaultItemWrapper<out SummaryObject>>? = null
    private val dataIdentifierListTextResolver =
        DataIdentifierListTextResolver(IdentityUtil(SingletonProvider.getMainDataAccessor()))
    private val userFeaturesChecker = SingletonProvider.getUserFeatureChecker()
    override var allowTeamspaceIcon: Boolean = false

    override val dataType: SyncObjectType?
        get() = itemObject.valueOfFromDataIdentifier()

    override val isAttachmentIconNeeded: Boolean
        get() = itemObject.attachmentsAllowed(userFeaturesChecker) && itemObject.hasAttachments()

    override fun getTitle(context: Context): StatusText =
        dataIdentifierListTextResolver.getLine1(context, itemObject)

    override fun getDescription(context: Context): StatusText =
        dataIdentifierListTextResolver.getLine2(context, itemObject)

    override fun getImageDrawable(context: Context): Drawable? =
        VaultItemImageHelper.getIconDrawableFromSummaryObject(context, itemObject)

    override fun getListItemActions(): List<ListItemAction> {
        return listOfNotNull(
            CopyItemFieldListItemAction(itemObject, itemListContext),
            QuickActionsItemAction(itemObject, itemListContext)
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
        return item.itemObject.id == itemObject.id
    }

    override fun isContentTheSame(item: VaultItemWrapper<out SummaryObject>): Boolean = false
}