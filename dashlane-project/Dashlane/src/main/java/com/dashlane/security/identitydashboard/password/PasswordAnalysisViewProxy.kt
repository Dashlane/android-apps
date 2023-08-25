package com.dashlane.security.identitydashboard.password

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.dashlane.R
import com.dashlane.security.identitydashboard.PasswordAnalysisScoreViewProxy
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.widgets.view.MultiColumnRecyclerView
import com.dashlane.ui.widgets.view.empty.EmptyScreenConfiguration
import com.dashlane.ui.widgets.view.empty.EmptyScreenViewProvider
import com.google.android.material.tabs.TabLayout
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import com.skocken.presentation.viewproxy.BaseViewProxy
import kotlin.math.roundToInt

class PasswordAnalysisViewProxy(view: View) :
    BaseViewProxy<PasswordAnalysisContract.Presenter>(view),
    PasswordAnalysisContract.ViewProxy,
    EfficientAdapter.OnItemClickListener<DashlaneRecyclerAdapter.ViewTypeProvider>,
    ViewPager.OnPageChangeListener {

    private val viewSwitcher = findViewByIdEfficient<ViewSwitcher>(R.id.loader_view_switcher)!!
    private val viewPager = findViewByIdEfficient<ViewPager>(R.id.viewpager)!!
    private val tabLayout = findViewByIdEfficient<TabLayout>(R.id.tabs)!!

    private val scoreViewProxy =
        PasswordAnalysisScoreViewProxy(
            findViewByIdEfficient<ImageView>(R.id.security_score)!!
        )
    private val tipsTitle = findViewByIdEfficient<TextView>(R.id.tips_title)!!
    private val tipsBody = findViewByIdEfficient<TextView>(R.id.tips_body)!!
    private val adapter = ListPagerAdapter(view.context, this)

    init {
        findViewByIdEfficient<SwitchCompat>(R.id.sensitive_account_switch)!!
            .setOnCheckedChangeListener { _, checked -> presenter.setSensitiveAccountOnly(checked) }

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

        scoreViewProxy.showIndeterminate()
    }

    override fun onItemClick(
        adapter: EfficientAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>,
        view: View,
        item: DashlaneRecyclerAdapter.ViewTypeProvider?,
        position: Int
    ) {
        item ?: return
        presenter.onListItemClick(item)
    }

    override fun setItems(
        itemsPerMode: Map<PasswordAnalysisContract.Mode, List<Any>>,
        modeToSelect: PasswordAnalysisContract.Mode?,
        indexToHighlight: Int?
    ) {
        removeListenerPage()
        val previousItem = viewPager.currentItem
        for ((mode, items) in itemsPerMode) {
            adapter.items[mode] = items
            setTabIcon(mode.ordinal, items.distinct().count { it is VaultItemWrapper<*> })
        }
        if (indexToHighlight != null && modeToSelect != null) {
            adapter.pendingScrollToPosition = modeToSelect.ordinal to indexToHighlight
        }
        viewPager.adapter = adapter

        val index = modeToSelect?.ordinal ?: previousItem
        viewPager.currentItem = index
        onPageSelected(index)
        viewPager.addOnPageChangeListener(this)
    }

    override fun removeListenerPage() {
        viewPager.removeOnPageChangeListener(this)
    }

    private fun setTabIcon(position: Int, counter: Int) {
        val parentTabView = if (tabLayout.childCount == 1) {
            tabLayout.getChildAt(0) as? ViewGroup
        } else {
            tabLayout
        }
        val tabView = parentTabView?.getChildAt(position) as? ViewGroup ?: return
        (0..tabView.childCount).forEach {
            val childAt = tabView.getChildAt(it)
            if (childAt is TextView) {
                val icon = PasswordAnalysisCountBadgeDrawable.newStateListDrawable(childAt.context, counter)
                childAt.compoundDrawablePadding = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    4F,
                    context.resources.displayMetrics
                ).roundToInt()
                childAt.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
                
                val contentDescription = "${childAt.text} ($counter)"
                childAt.contentDescription = contentDescription
                tabView.contentDescription = contentDescription
            }
        }
    }

    override fun setSecurityScore(score: Float) {
        scoreViewProxy.showProgress(score)
        if (score > 0) {
            tipsTitle.setText(R.string.security_dashboard_password_health_advice_title)
            tipsTitle.visibility = View.VISIBLE
            tipsBody.setText(R.string.security_dashboard_password_health_body_normal)
        } else {
            tipsTitle.text = ""
            tipsTitle.visibility = View.GONE
            tipsBody.setText(R.string.security_dashboard_password_health_body_empty_state)
        }
    }

    override fun setRefreshMode(enable: Boolean) {
        viewSwitcher.displayedChild = if (enable) {
            0
        } else {
            1
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        
    }

    override fun onPageSelected(position: Int) {
        val mode = PasswordAnalysisContract.Mode.values()[position]
        presenter.onPageSelected(mode)
    }

    private class ListPagerAdapter(
        private val context: Context,
        private val securityDashboardViewProxy: PasswordAnalysisViewProxy
    ) : PagerAdapter() {

        private val modes = PasswordAnalysisContract.Mode.values()

        val items = mutableMapOf<PasswordAnalysisContract.Mode, List<Any>>()

        var pendingScrollToPosition: Pair<Int, Int>? = null

        override fun instantiateItem(collection: ViewGroup, position: Int): Any {
            val recyclerView = MultiColumnRecyclerView(collection.context)
            val adapter = recyclerView.adapter!!
            items[modes[position]]?.let { adapter.addAll(it) }
            if (adapter.isEmpty) {
                getEmptyPage(position)?.let { adapter.add(it) }
            }
            collection.addView(recyclerView)
            adapter.onItemClickListener = securityDashboardViewProxy

            pendingScrollToPosition
                ?.takeIf { it.first == position }
                ?.let {
                    recyclerView.gridLayoutManager.scrollToPosition(it.second)
                    pendingScrollToPosition = null
                }

            return recyclerView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (modes[position]) {
                PasswordAnalysisContract.Mode.WEAK -> context.getString(R.string.security_dashboard_list_weak)
                PasswordAnalysisContract.Mode.REUSED -> context.getString(R.string.security_dashboard_list_reused)
                PasswordAnalysisContract.Mode.EXCLUDED -> context.getString(R.string.security_dashboard_list_exclude)
                PasswordAnalysisContract.Mode.COMPROMISED -> context.getString(R.string.security_dashboard_list_compromised)
            }
        }

        override fun getCount(): Int {
            return modes.size
        }

        private fun getEmptyPage(position: Int): EmptyScreenViewProvider {
            return when (modes[position]) {
                PasswordAnalysisContract.Mode.WEAK -> ModeEmptyScreenViewProvider(
                    context,
                    R.string.security_dashboard_list_weak_empty_title
                )
                PasswordAnalysisContract.Mode.REUSED -> ModeEmptyScreenViewProvider(
                    context,
                    R.string.security_dashboard_list_reused_empty_title
                )
                PasswordAnalysisContract.Mode.EXCLUDED -> ModeEmptyScreenViewProvider(
                    context,
                    R.string.security_dashboard_list_exclude_empty_title,
                    R.drawable.ic_empty_password_analysis_ignored
                )
                PasswordAnalysisContract.Mode.COMPROMISED -> ModeEmptyScreenViewProvider(
                    context,
                    R.string.security_dashboard_list_compromised_empty_title
                )
            }.emptyViewProvider
        }
    }

    private class ModeEmptyScreenViewProvider(
        context: Context,
        @StringRes title: Int,
        @DrawableRes image: Int = R.drawable.ic_empty_password_analysis
    ) {

        val emptyViewProvider = EmptyScreenViewProvider(
            EmptyScreenConfiguration.Builder()
                .setImage(VectorDrawableCompat.create(context.resources, image, null))
                .setLine2(context.resources.getString(title))
                .setAlignTop(true)
                .build()
        )
    }
}
