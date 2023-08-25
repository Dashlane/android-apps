package com.dashlane.notificationcenter

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.R
import com.dashlane.notificationcenter.view.ActionItemEmptyItemViewHolder
import com.dashlane.notificationcenter.view.ActionItemSection
import com.dashlane.notificationcenter.view.HeaderItem
import com.dashlane.notificationcenter.view.NotificationItem
import com.dashlane.notificationcenter.view.SwipeToDeleteCallback
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.widgets.view.MultiColumnRecyclerView
import com.dashlane.util.SnackbarUtils
import com.google.android.material.snackbar.Snackbar
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder
import com.skocken.presentation.viewproxy.BaseViewProxy

open class NotificationCenterViewProxy(rootView: View, private val displaySectionHeader: Boolean) :
    BaseViewProxy<NotificationCenterDef.Presenter>(rootView),
    NotificationCenterDef.View {

    private val loadingView: View
        get() = findViewByIdEfficient(R.id.data_list_loading)!!
    private val recyclerView: MultiColumnRecyclerView
        get() = findViewByIdEfficient(R.id.dashboard_view)!!

    val adapter: DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>
            by lazy { recyclerView.adapter!! }

    private val emptyView = ActionItemEmptyItemViewHolder.ITEM

    override var items: List<NotificationItem> = listOf()
        set(value) {
            field = value
            val orderedItemsBySection =
                presenterOrNull?.groupActionItemsBySection(value, displaySectionHeader, null)
            adapter.populateItems(orderedItemsBySection ?: emptyList())
            showEmptyViewIfNeeded()
        }

    init {
        initView()
    }

    override fun getDisplayedItems(): List<NotificationItem> =
        adapter.objects.filterIsInstance(NotificationItem::class.java)

    override fun setLoading(isLoading: Boolean) {
        loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun updateBreachAlertHeader(breachAlertCount: Int?) {
        for ((index, item) in adapter.objects.withIndex()) {
            if (item is HeaderItem && item.section == ActionItemSection.BREACH_ALERT) {
                item.count = breachAlertCount
                adapter.notifyItemChanged(index)
                break
            }
        }
    }

    private fun initView() {
        recyclerView.apply {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            
            setupSwipeToDismiss(this)
        }
        
        adapter.setOnItemClickListener { _, _, item, _ ->
            item ?: return@setOnItemClickListener
            presenterOrNull?.click(item)
        }
    }

    private fun setupSwipeToDismiss(recyclerView: RecyclerView) {
        
        val swipeHandler = object : SwipeToDeleteCallback(context) {

            
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                if (viewHolder is ActionItemEmptyItemViewHolder ||
                    viewHolder is HeaderItem.ViewHolder
                ) {
                    return 0
                }

                return super.getMovementFlags(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                presenterOrNull?.apply {
                    val item = (viewHolder as? EfficientViewHolder<*>)?.`object` as? NotificationItem ?: return
                    items = items.minus(item)
                    dismiss(item)
                    refresh()
                    showEmptyViewIfNeeded()
                    showUndoSnackBar(item)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun showEmptyViewIfNeeded() {
        if (adapter.isEmpty) {
            adapter.add(emptyView)
        }
    }

    @Suppress("WrongConstant")
    private fun showUndoSnackBar(item: NotificationItem) {
        SnackbarUtils.showSnackbar(
            getRootView() as View,
            context.getText(R.string.action_item_undo_dismiss_description),
            Snackbar.LENGTH_LONG
        ) {
            setAction(context.getText(R.string.action_item_undo_dismiss_cta)) {
                presenterOrNull?.apply {
                    undoDismiss(item)
                    adapter.remove(emptyView)

                    
                    if (!items.contains(item)) {
                        items = items.plus(item)
                    }
                }
            }
        }
    }
}
