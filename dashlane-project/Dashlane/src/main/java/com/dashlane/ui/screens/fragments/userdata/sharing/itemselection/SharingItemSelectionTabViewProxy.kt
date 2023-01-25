package com.dashlane.ui.screens.fragments.userdata.sharing.itemselection

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dashlane.R
import com.dashlane.ui.widgets.view.RecyclerViewFloatingActionButton
import com.dashlane.xml.domain.SyncObjectType
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SharingItemSelectionTabViewProxy(
    view: View,
    val fragment: Fragment,
    val viewModel: NewShareItemViewModel
) {

    private val coroutineScope: CoroutineScope
        get() = fragment.lifecycle.coroutineScope

    private val context: Context = view.context

    private val floatingButton =
        view.findViewById<RecyclerViewFloatingActionButton>(R.id.data_list_floating_button)

    init {
        showFABDependingOnSelectedItems(0)
        floatingButton.setImageResource(R.drawable.ic_share)
        floatingButton.contentDescription =
            context.getString(R.string.and_accessibility_share_password_button)
        floatingButton.setOnClickListener { viewModel.onClickNewShare() }
        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        viewPager.adapter =
            ItemSelectionPagerAdapter(fragment.childFragmentManager, fragment.lifecycle)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val textResId = if (position == ItemSelectionPagerAdapter.INDEX_PASSWORD) {
                R.string.sharing_share_new_item_tab_authentifiant
            } else {
                R.string.sharing_share_new_item_tab_secure_notes
            }
            tab.text = context.getString(textResId)
        }.attach()

        coroutineScope.launch {
            viewModel.selectionState.collect {
                showFABDependingOnSelectedItems(it.totalCount)
            }
        }
    }

    private fun showFABDependingOnSelectedItems(count: Int) {
        if (count > 0) {
            floatingButton.unlockPosition()
            floatingButton.show(true)
        } else {
            floatingButton.unlockPosition()
            floatingButton.hide(true)
            floatingButton.lockPosition()
        }
    }

    private class ItemSelectionPagerAdapter(
        fm: FragmentManager,
        lifecycle: Lifecycle,
    ) : FragmentStateAdapter(fm, lifecycle) {

        override fun getItemCount(): Int = NUM_ITEMS

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                INDEX_SECURE_NOTE -> SharingNewShareItemFragment.newInstance(SyncObjectType.SECURE_NOTE)
                else -> SharingNewShareItemFragment.newInstance(SyncObjectType.AUTHENTIFIANT)
            }
        }

        companion object {
            private const val NUM_ITEMS = 2
            const val INDEX_PASSWORD = 0
            const val INDEX_SECURE_NOTE = 1
        }
    }
}
