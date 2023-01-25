package com.dashlane.security.darkwebmonitoring

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.dashlane.R
import com.dashlane.security.darkwebmonitoring.item.DarkWebBreachItem
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.widgets.view.MultiColumnRecyclerView

class DarkWebMonitoringPagerAdapter(
    private val activity: Activity,
    private val context: Context,
    private val presenter: DarkWebMonitoringContract.Presenter,
    private val pendingItems: List<DashlaneRecyclerAdapter.ViewTypeProvider>,
    private val resolvedItems: List<DashlaneRecyclerAdapter.ViewTypeProvider>
) : PagerAdapter() {

    

    private val pendingAdapter = DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>()
    private val resolvedAdapter = DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>()

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val recyclerView = MultiColumnRecyclerView(collection.context)
        val adapter: DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>
        if (position == 0) {
            adapter = pendingAdapter
            adapter.clear()
            adapter.addAll(pendingItems)
        } else {
            adapter = resolvedAdapter
            adapter.clear()
            adapter.addAll(resolvedItems)
        }
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener { _, _, item, itemPosition ->
            if (presenter.selectedItems.size > 0 && item is DarkWebBreachItem) {
                toggleItem(item, itemPosition, recyclerView)
            } else {
                if (item is DashlaneRecyclerAdapter.ViewTypeProvider) {
                    presenter.onClick(item)
                }
            }
        }
        adapter.setOnItemLongClickListener { _, _, item, itemPosition ->
            if (item is DarkWebBreachItem) {
                toggleItem(item, itemPosition, recyclerView)
            }
        }
        collection.addView(recyclerView)
        return recyclerView
    }

    private fun toggleItem(item: DarkWebBreachItem, position: Int, recyclerView: MultiColumnRecyclerView) {
        item.selected = !item.selected
        if (item.selected) {
            presenter.selectedItems.add(item)
        } else {
            presenter.selectedItems.remove(item)
        }
        recyclerView.adapter?.notifyItemChanged(position)
        activity.invalidateOptionsMenu()
    }

    

    @SuppressLint("NotifyDataSetChanged")
    fun resetToggledItem() {
        resetToggledForAdapter(pendingAdapter)
        resetToggledForAdapter(resolvedAdapter)
    }

    private fun resetToggledForAdapter(adapter: DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>) {
        val itemCount = adapter.itemCount
        for (index in 0 until itemCount) {
            val item = adapter.get(index)
            if (item is DarkWebBreachItem && item.selected) {
                item.selected = false
                adapter.notifyItemChanged(index)
            }
        }
    }

    override fun getCount(): Int = 2

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> context.getString(
            R.string.dwm_status_pending,
            pendingItems.filterIsInstance<DarkWebBreachItem>().size.toString()
        )
        else -> context.getString(
            R.string.dwm_status_resolved,
            resolvedItems.filterIsInstance<DarkWebBreachItem>().size.toString()
        )
    }
}