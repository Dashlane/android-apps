package com.dashlane.ui.activities.fragments.vault

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dashlane.R
import com.dashlane.design.component.InfoboxButton
import com.dashlane.design.component.InfoboxLarge
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.featureflipping.FeatureFlip
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.ui.activities.fragments.vault.list.VaultListFragment
import com.dashlane.ui.common.compose.components.banner.VaultBanner
import com.dashlane.util.SnackbarUtils
import com.dashlane.vault.Filter
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.skocken.presentation.viewproxy.BaseViewProxy

class VaultViewProxy(
    view: View,
    private val fragment: Fragment,
    private val featuresChecker: UserFeaturesChecker,
) : BaseViewProxy<Vault.Presenter>(view), Vault.View {
    private val searchView = findViewByIdEfficient<View>(R.id.search_view)!!
    private val filterTabLayout = findViewByIdEfficient<TabLayout>(R.id.filter_tab_layout)!!
    private val announcementView = findViewByIdEfficient<ViewGroup>(R.id.vault_announcement)!!
    private val autofillAnnouncementView = findViewByIdEfficient<ViewGroup>(R.id.vault_autofill_announcement)!!
    private val fab =
        findViewByIdEfficient<ExtendedFloatingActionButton>(R.id.data_list_floating_button)!!

    private var tabs: Map<String, Filter> = emptyMap()
    private val filters
        get() = tabs.map { it.value }

    init {
        searchView.setOnClickListener { presenter.onSearchViewClicked() }
    }

    override fun showTabs(canUseSecrets: Boolean) {
        tabs = buildMap {
            put(
                context.getString(R.string.vault_filter_all_items),
                Filter.ALL_VISIBLE_VAULT_ITEM_TYPES,
            )
            put(context.getString(R.string.vault_filter_passwords), Filter.FILTER_PASSWORD)
            put(context.getString(R.string.vault_filter_secure_notes), Filter.FILTER_SECURE_NOTE)
            put(context.getString(R.string.vault_filter_payments), Filter.FILTER_PAYMENT)
            put(context.getString(R.string.vault_filter_personal_info), Filter.FILTER_PERSONAL_INFO)
            put(context.getString(R.string.vault_filter_ids), Filter.FILTER_ID)

            if (canUseSecrets) {
                put(context.getString(R.string.vault_filter_secrets), Filter.FILTER_SECRET)
            }
            if (featuresChecker.has(FeatureFlip.WIFI)) {
                put(context.getString(R.string.vault_filter_wifi), Filter.FILTER_WIFI)
            }
        }
        val filters = tabs.map { it.value }
        val titles = tabs.map { it.key }

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
        if (filters.isEmpty()) return

        val position = filters.indexOf(filter)
        filterTabLayout.selectTab(filterTabLayout.getTabAt(position))
    }

    override fun clearAnnouncements() {
        announcementView.removeAllViews()
        autofillAnnouncementView.removeAllViews()
    }

    override fun showAnnouncement(
        iconToken: IconToken,
        title: String?,
        description: String,
        mood: Mood,
        onClick: () -> Unit,
    ) {
        clearAnnouncements()
        announcementView.addView(
            ComposeView(context).apply {
                setContent {
                    DashlaneTheme {
                        VaultBanner(
                            iconToken = iconToken,
                            title = title,
                            description = description,
                            mood = mood,
                            onClick = onClick,
                        )
                    }
                }
            },
        )
    }

    override fun showAutofillAnnouncement(
        iconToken: IconToken,
        title: String,
        description: String,
        positiveButton: String,
        negativeButton: String?,
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit,
    ) {
        clearAnnouncements()
        autofillAnnouncementView.addView(
            ComposeView(context).apply {
                setContent {
                    DashlaneTheme {
                        InfoboxLarge(
                            title = title,
                            description = description,
                            icon = iconToken,
                            primaryButton = InfoboxButton(
                                text = positiveButton,
                                onClick = onPositiveClick,
                            ),
                            secondaryButton = negativeButton?.let {
                                InfoboxButton(
                                    text = it,
                                    onClick = onNegativeClick,
                                )
                            },
                            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                            mood = Mood.Brand,
                        )
                    }
                }
            },
        )
    }

    override fun showSnackbar(stringRes: Int) {
        SnackbarUtils.showSnackbar(
            anchorView = fab,
            text = resources.getString(stringRes),
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