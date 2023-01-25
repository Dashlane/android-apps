package com.dashlane.ui.fab

import android.view.View
import android.widget.FrameLayout
import com.dashlane.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.skocken.presentation.viewproxy.BaseViewProxy

abstract class FabViewProxy(rootView: View) : BaseViewProxy<FabDef.IPresenter>(rootView), FabDef.IView,
    View.OnClickListener {
    val fabMenuHolder: FrameLayout = rootView.findViewById<FrameLayout>(R.id.fab_menu_holder).apply {
        setOnClickListener { hideFABMenu(true) }
    }
    val floatingButton: ExtendedFloatingActionButton =
        rootView.findViewById<ExtendedFloatingActionButton>(R.id.data_list_floating_button).apply {
            setOnClickListener(this@FabViewProxy)
        }

    override val isFabMenuHolderVisible: Boolean
        get() = fabMenuHolder.visibility == View.VISIBLE

    override fun showFABMenu(configureView: () -> Unit, animate: Boolean) {
        configureView()
        setFabMenuShown(animate)
    }

    override fun hideFABMenu(animate: Boolean) {
        setFabMenuHidden(animate)
    }

    override fun toggleFABMenu(configureViewOnShow: () -> Unit) {
        if (isFabMenuHolderVisible) {
            hideFABMenu(true)
        } else {
            showFABMenu(configureViewOnShow, true)
        }
    }

    protected fun setFabMenuShown(animate: Boolean) {
        FabViewUtil.animateMenuShown(fabMenuHolder)
        floatingButton.isSelected = true
        floatingButton.contentDescription = context.getString(R.string.and_accessibility_close_item_menu)
        fabMenuHolder.visibility = View.VISIBLE
        setFabMenuItemsShown(animate)
        floatingButton.shrink()
    }

    protected fun setFabMenuItemsShown(animate: Boolean) {
        FabViewUtil.showFabMenuItems(fabMenuHolder, floatingButton, animate) {
            setFabMenuHidden(animate)
        }
    }

    private fun setFabMenuHidden(animate: Boolean) {
        FabViewUtil.hideFabMenuItems(fabMenuHolder, animate, null)
        floatingButton.isSelected = false
        floatingButton.contentDescription = context.getString(R.string.and_accessibility_open_item_menu)
        FabViewUtil.animateMenuStateHidden(fabMenuHolder)
        floatingButton.extend()
    }
}