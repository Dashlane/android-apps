package com.dashlane.ui.activities.fragments.vault

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dashlane.R
import com.dashlane.ui.activities.fragments.vault.list.VaultListFragment
import com.dashlane.util.SnackbarUtils
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.skocken.presentation.viewproxy.BaseViewProxy

class VaultViewProxy(
    val fragment: Fragment,
    view: View
) : BaseViewProxy<Vault.Presenter>(view), Vault.View {

    private val searchView = findViewByIdEfficient<View>(R.id.search_view)!!
    private val filterTabLayout = findViewByIdEfficient<TabLayout>(R.id.filter_tab_layout)!!
    private val announcementView = findViewByIdEfficient<ViewGroup>(R.id.vault_announcement)!!
    private val fab = findViewByIdEfficient<ExtendedFloatingActionButton>(R.id.data_list_floating_button)!!

    private val textToFilter = mapOf(
        context.getString(R.string.vault_filter_all_items) to Filter.ALL_VISIBLE_VAULT_ITEM_TYPES,
        context.getString(R.string.vault_filter_passwords) to Filter.FILTER_PASSWORD,
        context.getString(R.string.vault_filter_secure_notes) to Filter.FILTER_SECURE_NOTE,
        context.getString(R.string.vault_filter_payments) to Filter.FILTER_PAYMENT,
        context.getString(R.string.vault_filter_personal_info) to Filter.FILTER_PERSONAL_INFO,
        context.getString(R.string.vault_filter_ids) to Filter.FILTER_ID
    )

    private val filters = textToFilter.map { it.value }
    private val titles = textToFilter.map { it.key }

    init {
        searchView.setOnClickListener { presenter.onSearchViewClicked() }
        val viewPager = findViewByIdEfficient<ViewPager2>(R.id.vault_view_pager)!!
        viewPager.adapter = VaultListAdapter(fragment, filters)
        TabLayoutMediator(filterTabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        for (position in 0 until filterTabLayout.tabCount) {
            filterTabLayout.getTabAt(position)?.view?.setOnClickListener {
                presenterOrNull?.onTabClicked(filters[position])
            }
        }

        filterTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                presenter.onTabReselected(filters[position])
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                presenter.onFilterSelected(filters[position])
            }
        })
    }

    override fun getSelectedPosition(): Int = filterTabLayout.selectedTabPosition

    override fun setSelectedFilterTab(filter: Filter) {
        val position = filters.indexOf(filter)
        filterTabLayout.selectTab(filterTabLayout.getTabAt(position))
    }

    override fun showAnnouncement(@LayoutRes layout: Int?, onClick: () -> Unit) {
        announcementView.removeAllViews()
        if (layout != null) {
            LayoutInflater.from(context).inflate(layout, announcementView)
            announcementView.setOnClickListener { onClick() }
        }
    }

    override fun showSnackbar(stringRes: Int) {
        SnackbarUtils.showSnackbar(
            anchorView = fab,
            text = resources.getString(stringRes)
        ) {
            
            
            
            this.anchorView = fab
        }
    }

    private class VaultListAdapter(fragment: Fragment, val filters: List<Filter>) :
        FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = filters.size

        override fun createFragment(position: Int): Fragment {
            return VaultListFragment.newInstance(filters[position])
        }
    }
}