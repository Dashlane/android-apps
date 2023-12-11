package com.dashlane.ui.activities.fragments.list.wrapper

import android.content.Context
import android.graphics.drawable.Drawable
import com.dashlane.item.subview.quickaction.QuickActionProvider
import com.dashlane.navigation.Navigator
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.ui.activities.fragments.list.action.ListItemAction
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.MultiColumnViewTypeProvider
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapter.ItemListContextProvider
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType

interface VaultItemWrapper<D : SummaryObject> :
    MultiColumnViewTypeProvider,
    DiffUtilComparator<VaultItemWrapper<out SummaryObject>>,
    ItemListContextProvider {

    override val itemListContext: ItemListContext
    var allowTeamspaceIcon: Boolean
    val summaryObject: D
    val dataType: SyncObjectType?
    val isAttachmentIconNeeded: Boolean
    val vaultItemCopyService: VaultItemCopyService
    val navigator: Navigator
    val quickActionProvider: QuickActionProvider
    val teamspaceManager: TeamspaceManager?

    fun getListItemActions(): List<ListItemAction>
    fun getTitle(context: Context): StatusText
    fun getDescription(context: Context): StatusText
    fun getImageDrawable(context: Context): Drawable?
    override fun getSpanSize(spanCount: Int): Int
    fun setViewType(overrideViewType: DashlaneRecyclerAdapter.ViewType<VaultItemWrapper<out SummaryObject>>)
}