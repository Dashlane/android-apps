package com.dashlane.security.identitydashboard

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.R
import com.dashlane.security.identitydashboard.item.IdentityDashboardItem
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.skocken.presentation.viewproxy.BaseViewProxy

class IdentityDashboardViewProxy(view: View) : BaseViewProxy<IdentityDashboardContract.Presenter>(view),
    IdentityDashboardContract.ViewProxy {

    private val recyclerView = findViewByIdEfficient<RecyclerView>(R.id.recyclerview)!!
    private val adapter = DashlaneRecyclerAdapter<IdentityDashboardItem>()

    init {
        val linearLayoutManager = LinearLayoutManager(recyclerView.context)
        linearLayoutManager.findFirstCompletelyVisibleItemPosition()
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener { _, _, item, _ ->
            item?.let { presenter.onClick(item) }
        }
    }

    override fun setItems(items: List<IdentityDashboardItem>) {
        val shouldScrollTop = (recyclerView.layoutManager as LinearLayoutManager)
            .findFirstVisibleItemPosition() == 0
        adapter.populateItems(items)
        if (shouldScrollTop) {
            recyclerView.scrollToPosition(0)
        }
    }

    override fun remove(item: IdentityDashboardItem) {
        adapter.remove(item)
    }
}