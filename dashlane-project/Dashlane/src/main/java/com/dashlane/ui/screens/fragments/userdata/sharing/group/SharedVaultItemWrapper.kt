package com.dashlane.ui.screens.fragments.userdata.sharing.group

import android.content.Context
import android.view.View
import android.widget.TextView
import com.dashlane.R
import com.dashlane.ui.activities.fragments.list.ItemWrapperViewHolder
import com.dashlane.ui.activities.fragments.list.action.ListItemAction
import com.dashlane.ui.activities.fragments.list.wrapper.ItemWrapperProvider
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.MultiColumnViewTypeProvider
import com.dashlane.ui.adapter.HeaderItem
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.util.ViewTypeUtils
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.DataIdentifierTypeTextFactory
import com.dashlane.xml.domain.SyncObjectType
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class SharedVaultItemWrapper(
    context: Context,
    val sharedItem: SharingModels,
    val itemWrapperProvider: ItemWrapperProvider,
    val onPendingMenuClick: (View, SharingModels) -> Unit,
    val onAcceptedMenuClick: (View, SharingModels) -> Unit
) : MultiColumnViewTypeProvider, DiffUtilComparator<SharedVaultItemWrapper> {

    val itemWrapper: VaultItemWrapper<out SummaryObject>
        get() = itemWrapperProvider(
            sharedItem.item,
            ItemListContext.Container.SHARING.asListContext(),
        )!!
    private val isAdmin: Boolean
        get() = sharedItem.isAdmin
    private val isPending: Boolean
        get() = sharedItem.isPending

    val title: String = itemWrapper.getTitle(context).text
    val description: String = itemWrapper.getDescription(context).text

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> = VIEW_TYPE
    override fun getSpanSize(spanCount: Int): Int = 1

    override fun isItemTheSame(item: SharedVaultItemWrapper): Boolean =
        item.sharedItem.item.id == sharedItem.item.id

    override fun isContentTheSame(item: SharedVaultItemWrapper): Boolean =
        item.title == title &&
            item.description == description &&
            item.sharedItem.sharingStatusResource == sharedItem.sharingStatusResource &&
            item.sharedItem.isAdmin == sharedItem.isAdmin

    class ItemViewHolder(itemView: View) : EfficientViewHolder<SharedVaultItemWrapper>(itemView) {
        private val itemWrapperViewHolder: ItemWrapperViewHolder

        init {
            itemWrapperViewHolder = ItemWrapperViewHolder(itemView)
        }

        override fun updateView(context: Context, sharedVaultItemWrapper: SharedVaultItemWrapper?) {
            sharedVaultItemWrapper ?: return
            val itemWrapper = sharedVaultItemWrapper.itemWrapper
            val sharedItem = sharedVaultItemWrapper.sharedItem
            itemWrapperViewHolder.updateView(context, itemWrapper)
            val thirdLineTextView = findViewByIdEfficient<TextView>(R.id.item_third_line)!!
            val resource = sharedItem.sharingStatusResource
            if (resource == 0) {
                thirdLineTextView.visibility = View.INVISIBLE
            } else {
                thirdLineTextView.setText(resource)
                thirdLineTextView.visibility = View.VISIBLE
            }
            val actions: List<ListItemAction> = if (sharedVaultItemWrapper.isAdmin) {
                listOf<ListItemAction>(object : ListItemAction {
                    override val visibility: Int
                        get() = View.VISIBLE
                    override val viewId: Int
                        get() = View.generateViewId()
                    override val contentDescription: Int
                        get() = R.string.and_accessibility_vault_item_menu
                    override val icon: Int
                        get() = R.drawable.ic_item_action_more

                    override fun onClickItemAction(v: View, item: SummaryObject) {
                        if (sharedVaultItemWrapper.isPending) {
                            sharedVaultItemWrapper.onPendingMenuClick(
                                v,
                                sharedVaultItemWrapper.sharedItem
                            )
                        } else {
                            sharedVaultItemWrapper.onAcceptedMenuClick(
                                v,
                                sharedVaultItemWrapper.sharedItem
                            )
                        }
                    }
                })
            } else {
                emptyList()
            }
            itemWrapperViewHolder.updateActionItem(itemWrapper, actions)
        }
    }

    companion object {
        private val VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<out SharedVaultItemWrapper> =
            DashlaneRecyclerAdapter.ViewType(
                ViewTypeUtils.DEFAULT_ITEM_WRAPPER_VIEW_TYPE.layoutResId,
                ItemViewHolder::class.java
            )

        fun comparator(): Comparator<SharedVaultItemWrapper> = compareBy(
            { it.sharedItem.item.syncObjectType.xmlObjectName },
            { it.title },
            { it.description }
        )
    }
}

fun List<MultiColumnViewTypeProvider>.addHeaders(context: Context) =
    toMutableList().apply {
        indexOfFirst {
            (it as? SharedVaultItemWrapper)?.sharedItem?.item?.syncObjectType == SyncObjectType.AUTHENTIFIANT
        }.takeIf { it != -1 }?.also { index ->
            HeaderItem(
                context.getString(DataIdentifierTypeTextFactory.getStringResId(SyncObjectType.AUTHENTIFIANT))
            ).also { add(index, it) }
        }
        indexOfFirst {
            (it as? SharedVaultItemWrapper)?.sharedItem?.item?.syncObjectType == SyncObjectType.SECURE_NOTE
        }.takeIf { it != -1 }?.also { index ->
            HeaderItem(
                context.getString(DataIdentifierTypeTextFactory.getStringResId(SyncObjectType.SECURE_NOTE))
            ).also { add(index, it) }
        }
    }
