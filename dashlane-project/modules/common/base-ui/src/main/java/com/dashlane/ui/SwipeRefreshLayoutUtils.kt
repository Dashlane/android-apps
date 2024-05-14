package com.dashlane.ui

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dashlane.util.getThemeAttrColor

fun SwipeRefreshLayout.setup(onRefresh: () -> Unit) {
    setColorSchemeColors(context.getThemeAttrColor(R.attr.colorSecondary))
    setProgressBackgroundColorSchemeColor(
        context.getThemeAttrColor(R.attr.colorSurface)
    )
    setOnRefreshListener(onRefresh)
}