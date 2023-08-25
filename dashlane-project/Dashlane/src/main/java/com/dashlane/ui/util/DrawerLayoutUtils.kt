package com.dashlane.ui.util

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.dashlane.ui.menu.DashlaneMenuView
import com.dashlane.util.DeviceUtils

fun DrawerLayout.setup(drawerToggle: ActionBarDrawerToggle, actionBarUtil: ActionBarUtil, menuFrame: DashlaneMenuView) {
    actionBarUtil.setDrawerToggle(drawerToggle)
    val lp = menuFrame.layoutParams as DrawerLayout.LayoutParams
    lp.width = DeviceUtils.getNavigationDrawerWidth(context)
    menuFrame.layoutParams = lp
    addDrawerListener(drawerToggle)
}
