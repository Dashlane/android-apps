package com.dashlane.ui.util

import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.dashlane.util.DeviceUtils

fun DrawerLayout.setup(drawerToggle: ActionBarDrawerToggle, actionBarUtil: ActionBarUtil, menuFrame: ViewGroup) {
    actionBarUtil.setDrawerToggle(drawerToggle)
    val lp = menuFrame.layoutParams as DrawerLayout.LayoutParams
    lp.width = DeviceUtils.getNavigationDrawerWidth(context)
    menuFrame.layoutParams = lp
    addDrawerListener(drawerToggle)
}
