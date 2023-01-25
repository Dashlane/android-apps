package com.dashlane.ui.screens.fragments.userdata.sharing.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.screens.fragments.userdata.sharing.group.UserGroupTabFragment.UserGroupPagerAdapter.Companion.INDEX_ITEMS
import com.dashlane.util.setCurrentPageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class UserGroupTabFragment : AbstractContentFragment() {
    private val bundle: UserGroupTabFragmentArgs
        get() = UserGroupTabFragmentArgs.fromBundle(requireArguments())

    private val groupId: String
        get() = bundle.argsGroupId
    private val groupName: String
        get() = bundle.argsGroupName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.findViewById<Toolbar>(R.id.toolbar)?.title = groupName
        val view = inflater.inflate(R.layout.fragment_tablayout_viewpager2, container, false)
        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        viewPager.adapter = UserGroupPagerAdapter(groupId, childFragmentManager, lifecycle)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val textResId = if (position == INDEX_ITEMS) {
                R.string.user_group_tab_title_items
            } else {
                R.string.user_group_tab_title_members
            }
            tab.text = getString(textResId)
        }.attach()
        viewPager.registerOnPageChangeCallback(UserGroupOnPageChangeListener())
        return view
    }

    private inner class UserGroupOnPageChangeListener : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (position == INDEX_ITEMS) {
                setCurrentPageView(AnyPage.SHARING_GROUP_ITEM_LIST)
            } else {
                setCurrentPageView(AnyPage.SHARING_GROUP_MEMBER_LIST)
            }
        }
    }

    private class UserGroupPagerAdapter(
        private val userGroupId: String,
        fm: FragmentManager,
        lifecycle: Lifecycle,
    ) : FragmentStateAdapter(fm, lifecycle) {
        override fun createFragment(position: Int) = if (position == INDEX_ITEMS) {
            UserGroupItemsFragment.newInstance(userGroupId)
        } else {
            UserGroupMembersFragment.newInstance(userGroupId)
        }

        override fun getItemCount(): Int = NUM_ITEMS

        companion object {
            private const val NUM_ITEMS = 2
            const val INDEX_ITEMS = 0
        }
    }
}