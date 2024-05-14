package com.dashlane.premium.offer.list.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.premium.R
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OffersActivity : DashlaneActivity() {

    @Inject
    lateinit var announcementCenter: AnnouncementCenter

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment_offers)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offers)
        actionBarUtil.setup()
        announcementCenter.disable()
        
        
        setupActionBarWithNavController(navController, AppBarConfiguration.Builder().build())
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!navController.popBackStack()) finish()
        return true
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (!navController.popBackStack()) finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            announcementCenter.restorePreviousState()
        }
    }
}