package com.dashlane.security.darkwebmonitoring

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.TransitionManager
import androidx.viewpager.widget.ViewPager
import com.dashlane.R
import com.dashlane.darkweb.DarkWebEmailStatus
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.security.darkwebmonitoring.item.DarkWebEmailItem
import com.dashlane.security.darkwebmonitoring.item.DarkWebEmailPlaceholderItem
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.setCurrentPageView
import com.google.android.material.tabs.TabLayout
import com.skocken.presentation.viewproxy.BaseViewProxy

class DarkWebMonitoringViewProxy(view: View, val activity: DashlaneActivity) :
    BaseViewProxy<DarkWebMonitoringContract.Presenter>(view),
    DarkWebMonitoringContract.ViewProxy {

    private val darkWebEmailsAdapter = DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>()
    private val sceneContainer = findViewByIdEfficient<FrameLayout>(R.id.scene_container)!!
    private var viewPagerAdapter: DarkWebMonitoringPagerAdapter? = null

    init {
        darkWebEmailsAdapter.setOnItemClickListener { _, _, item, _ ->
            item?.let { presenter.onClick(item) }
        }
    }

    override fun setItems(
        pendingItems: List<DashlaneRecyclerAdapter.ViewTypeProvider>,
        resolvedItems: List<DashlaneRecyclerAdapter.ViewTypeProvider>,
        emails: List<DarkWebEmailItem>
    ) {
        if (pendingItems.isEmpty() && resolvedItems.isEmpty() && emails.isEmpty()) {
            activity.setCurrentPageView(AnyPage.TOOLS_DARK_WEB_MONITORING)
            showDarkwebInactiveScene()
        } else {
            activity.setCurrentPageView(AnyPage.TOOLS_DARK_WEB_MONITORING_LIST)
            viewPagerAdapter = DarkWebMonitoringPagerAdapter(activity, context, presenter, pendingItems, resolvedItems)
            goToRecyclerScene(emails)
        }
    }

    override fun showDarkwebInactiveScene() {
        val inactiveScene = transitionIfNeeded(R.layout.item_dark_web_inactive)
        val sceneRoot = inactiveScene.sceneRoot
        
        sceneRoot.findViewById<Button>(R.id.dark_web_cta)?.setOnClickListener {
            presenter.onInactiveDarkwebCtaClick()
        }
        ViewCompat.setAccessibilityHeading(sceneRoot.findViewById(R.id.title), true)
    }

    override fun goToPendingTab() {
        val recyclerScene = transitionIfNeeded(R.layout.item_dark_web_recycler)
        val root = recyclerScene.sceneRoot
        root.findViewById<ViewPager>(R.id.viewpager).apply {
            setCurrentItem(0, true)
        }
    }

    override fun updateActionBar() {
        if (presenter.selectedItems.size == 0) {
            setupActionBar()
        } else {
            setupSelectedActionBar()
        }
    }

    private fun setupActionBar() {
        viewPagerAdapter?.resetToggledItem()
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = resources.getString(R.string.menu_v3_section_dark_web_monitoring)
            activity.actionBarUtil.restoreDefaultActionBarColor()
            activity.actionBarUtil.drawerToggle.isDrawerIndicatorEnabled = true
        }
    }

    private fun setupSelectedActionBar() {
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = resources.getQuantityString(
                R.plurals.dwm_breaches_selected,
                presenter.selectedItems.size,
                presenter.selectedItems.size
            )
            activity.actionBarUtil.setActionBarColor(context.getThemeAttrColor(R.attr.colorSurface))
            activity.actionBarUtil.drawerToggle.isDrawerIndicatorEnabled = false
        }
    }

    private fun goToRecyclerScene(emails: List<DarkWebEmailItem>) {
        val recyclerScene = transitionIfNeeded(R.layout.item_dark_web_recycler)
        val root = recyclerScene.sceneRoot
        updateMonitoredEmailsHeader(root, emails)
        updateMonitoredEmailsFooter(root, emails)
        val viewPager = root.findViewById<ViewPager>(R.id.viewpager).apply {
            this.adapter = viewPagerAdapter
        } ?: return
        root.findViewById<TabLayout>(R.id.tabs).apply {
            setupWithViewPager(viewPager)
        }
        root.findViewById<RecyclerView>(R.id.dark_web_emails_recycler).apply {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            this.adapter = darkWebEmailsAdapter
        } ?: return
        darkWebEmailsAdapter.populateItems(emails + fillWithPlaceholders(emails.size))
    }

    private fun fillWithPlaceholders(emailCount: Int): List<DarkWebEmailPlaceholderItem> {
        val placeholderCount = MAX_EMAILS - emailCount
        if (placeholderCount <= 0) return emptyList()
        return List(placeholderCount) { DarkWebEmailPlaceholderItem() }
    }

    private fun updateMonitoredEmailsHeader(root: ViewGroup, emails: List<DarkWebEmailItem>) {
        root.findViewById<TextView>(R.id.header_dark_web_monitored_emails_title).apply {
            val emailCount = emails.size
            text = context.resources.getQuantityString(R.plurals.dwm_emails_monitored_title, emailCount, emailCount)
        }
        val indicator = root.findViewById<ImageView>(R.id.header_dark_web_monitored_emails_indicator)
        val subtitle = root.findViewById<TextView>(R.id.header_dark_web_monitored_emails_subtitle)
        when {
            emails.any { it.emailStatus.status == DarkWebEmailStatus.STATUS_PENDING } -> {
                val pendingCount = emails.count { it.emailStatus.status == DarkWebEmailStatus.STATUS_PENDING }
                subtitle.text = context.resources.getQuantityString(
                    R.plurals.dwm_emails_monitored_subtitle_pending,
                    pendingCount,
                    pendingCount
                )
                indicator.imageTintList = ColorStateList.valueOf(context.getColor(R.color.text_warning_quiet))
            }
            else -> {
                subtitle.text = context.getString(R.string.dwm_emails_monitored_subtitle_scanning)
                indicator.imageTintList = ColorStateList.valueOf(context.getColor(R.color.text_positive_quiet))
            }
        }
    }

    private fun updateMonitoredEmailsFooter(root: ViewGroup, emails: List<DarkWebEmailItem>) {
        val availableSpots = MAX_EMAILS - emails.size
        root.findViewById<TextView>(R.id.dark_web_emails_spots_available).text =
            context.resources.getQuantityString(R.plurals.dwm_emails_monitored_spot, availableSpots, availableSpots)
        root.findViewById<Button>(R.id.dark_web_emails_add_email).apply {
            isEnabled = emails.size < MAX_EMAILS
            setOnClickListener {
                presenter.onAddDarkWebEmailClick()
            }
        }
    }

    private fun transitionIfNeeded(@LayoutRes layout: Int): Scene {
        TransitionManager.endTransitions(sceneContainer)
        val oldScene = Scene.getCurrentScene(sceneContainer)
        val newScene = Scene.getSceneForLayout(sceneContainer, layout, context)
        if (oldScene == null) {
            
            TransitionManager.go(newScene, null)
        } else if (oldScene != newScene) {
            TransitionManager.go(newScene)
        }
        return newScene
    }

    override fun showDeleteCompleted(breachesDeleted: Int) {
        SnackbarUtils.showSnackbar(
            activity,
            activity.resources.getQuantityString(R.plurals.dwm_breaches_deleted, breachesDeleted, breachesDeleted)
        )
    }

    companion object {
        
        private const val MAX_EMAILS = 5
    }
}