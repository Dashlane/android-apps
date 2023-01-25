package com.dashlane.ui.widgets.view

import android.app.Activity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.util.DeviceUtils
import com.dashlane.util.announceForAccessibility
import com.dashlane.util.logPageView



class MainDrawerToggle(
    private val activity: Activity,
    private val drawerLayout: DrawerLayout,
    toolbar: Toolbar
) :
    ActionBarDrawerToggle(
        activity,
        drawerLayout,
        toolbar,
        R.string.and_accessibility_open_drawer_description,
        R.string.close
    ) {

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (drawerLayout.getDrawerLockMode(GravityCompat.START) == DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDrawerOpened(drawerView: View) {
        super.onDrawerOpened(drawerView)
        drawerView.context.announceForAccessibility(
            drawerView.context.getString(R.string.and_accessibility_navigation_drawer_open),
            true
        )
        DeviceUtils.hideKeyboard(drawerView)
        activity.invalidateOptionsMenu()
        activity.logPageView(AnyPage.MENU)
    }

    override fun onDrawerClosed(drawerView: View) {
        super.onDrawerClosed(drawerView)
        drawerView.context.announceForAccessibility(
            drawerView.context.getString(R.string.and_accessibility_navigation_drawer_close),
            true
        )
        drawerLayout.requestDisallowInterceptTouchEvent(false)
        activity.invalidateOptionsMenu()
    }
}