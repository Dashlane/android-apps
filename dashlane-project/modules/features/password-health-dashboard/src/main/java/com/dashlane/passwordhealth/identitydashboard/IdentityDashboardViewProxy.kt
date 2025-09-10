package com.dashlane.passwordhealth.identitydashboard

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.passwordhealth.R
import com.dashlane.passwordhealth.identitydashboard.item.IdentityDashboardItem
import com.dashlane.passwordhealth.identitydashboard.view.IdentityDashboardOnboardingView
import com.dashlane.ui.legacy.recyclerview.DashlaneRecyclerAdapter
import com.skocken.presentation.viewproxy.BaseViewProxy

class IdentityDashboardViewProxy(view: View) :
    BaseViewProxy<IdentityDashboardContract.Presenter>(view),
    IdentityDashboardContract.ViewProxy {
    private val recyclerView = findViewByIdEfficient<RecyclerView>(R.id.recyclerview)!!
    private val emptyContentView = findViewByIdEfficient<IdentityDashboardOnboardingView>(R.id.password_health_empty_state)!!
    private val adapter = DashlaneRecyclerAdapter<IdentityDashboardItem>()

    init {
        val linearLayoutManager = LinearLayoutManager(recyclerView.context)
        linearLayoutManager.findFirstCompletelyVisibleItemPosition()
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
    }

    override fun setItems(items: List<IdentityDashboardItem>) {
        recyclerView.isVisible = true
        emptyContentView.isVisible = false
        val shouldScrollTop = (recyclerView.layoutManager as LinearLayoutManager)
            .findFirstVisibleItemPosition() == 0
        adapter.populateItems(items)
        if (shouldScrollTop) {
            recyclerView.scrollToPosition(0)
        }
    }

    override fun setOnboardingEmptyState() {
        emptyContentView.onAddLoginClick = presenter::onAddLoginClick
        recyclerView.isVisible = false
        emptyContentView.isVisible = true
    }

    override fun remove(item: IdentityDashboardItem) {
        adapter.remove(item)
    }
}