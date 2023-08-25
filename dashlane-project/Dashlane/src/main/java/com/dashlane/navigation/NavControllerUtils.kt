package com.dashlane.navigation

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.dashlane.R
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.ui.activities.fragments.checklist.ChecklistHelper

object NavControllerUtils {
    val TOP_LEVEL_DESTINATIONS = setOf(
        R.id.nav_home,
        R.id.nav_personal_plan,
        R.id.nav_password_generator,
        R.id.nav_identity_dashboard,
        R.id.nav_password_analysis,
        R.id.nav_notif_center,
        R.id.nav_sharing_center,
        R.id.nav_vpn_third_party,
        R.id.nav_dark_web_monitoring,
        R.id.nav_authenticator_dashboard,
        R.id.nav_authenticator_suggestions
    )

    fun NavController.setup(
        activity: Activity,
        openedFromDeepLink: Boolean,
        checklistHelper: ChecklistHelper,
        topLevelDestinations: Set<Int> = TOP_LEVEL_DESTINATIONS
    ) {
        
        
        if (activity is HomeActivity && !openedFromDeepLink) {
            setupStartDestination(checklistHelper)
        }
        setupActionBar(activity, topLevelDestinations)
    }

    private fun NavController.setupStartDestination(checklistHelper: ChecklistHelper) {
        if (graph.id != R.id.drawer_navigation || previousBackStackEntry != null) {
            
            return
        }
        val startDestination =
            if (checklistHelper.shouldDisplayChecklist()) R.id.nav_personal_plan else R.id.nav_home
        if (graph.startDestinationId != startDestination) {
            
            val newGraph = navInflater.inflate(R.navigation.drawer_navigation).apply {
                setStartDestination(startDestination)
            }
            this.graph = newGraph
        }
    }

    fun NavController.setupActionBar(activity: Activity, topLevelDestinations: Set<Int>) {
        val appCompatActivity = activity as? AppCompatActivity ?: return
        val toolbar = appCompatActivity.findViewById<Toolbar>(R.id.toolbar) ?: return
        ViewCompat.setAccessibilityHeading(toolbar, true)
        appCompatActivity.setSupportActionBar(toolbar)
        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinations,
            activity.findViewById<DrawerLayout>(R.id.drawer_layout)
        )
        appCompatActivity.setupActionBarWithNavController(this, appBarConfiguration)
    }
}