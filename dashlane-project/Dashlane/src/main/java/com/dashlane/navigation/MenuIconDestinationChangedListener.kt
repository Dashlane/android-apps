package com.dashlane.navigation

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.MenuContainer
import com.dashlane.ui.screens.settings.SettingsFragmentArgs.Companion.fromBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MenuIconDestinationChangedListener(
    private val activity: Activity,
    var topLevelDestinations: Set<Int>
) : NavController.OnDestinationChangedListener {

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        
        val dashlaneActivity = activity as? DashlaneActivity ?: return
        val drawable = dashlaneActivity.actionBarUtil.drawerArrowDrawable ?: return
        dashlaneActivity.lifecycleScope.launch(Dispatchers.Main.immediate) {
            
            
            delay(1)
            val shouldShowMenu = dashlaneActivity.shouldShowMenu(arguments)
            if (shouldShowMenu) {
                dashlaneActivity.drawerToggleDelegate?.setActionBarUpIndicator(
                    drawable,
                    R.string.and_accessibility_open_drawer_description
                )
            }
            
            if (activity is MenuContainer) {
                activity.disableMenuAccess(!shouldShowMenu)
            }
        }
    }

    private fun DashlaneActivity.shouldShowMenu(arguments: Bundle?): Boolean {
        val currentDestination = navigator.currentDestination
        val isSettingsMainSection = currentDestination?.id == R.id.nav_settings &&
            arguments != null && fromBundle(arguments).id == null
        if (!isSettingsMainSection && !topLevelDestinations.contains(currentDestination?.id)) {
            return false
        }
        return true
    }
}