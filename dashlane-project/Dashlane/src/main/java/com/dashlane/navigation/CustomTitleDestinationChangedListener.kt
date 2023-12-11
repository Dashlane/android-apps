package com.dashlane.navigation

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomTitleDestinationChangedListener(
    private val activity: Activity
) : NavController.OnDestinationChangedListener {
    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        val appCompatActivity = activity as? AppCompatActivity ?: return
        appCompatActivity.run {
            lifecycleScope.launch {
                
                
                delay(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
                val showCustomTitle = controller.currentDestination?.label.isNullOrEmpty()
                supportActionBar?.apply {
                    
                    setDisplayShowCustomEnabled(showCustomTitle)
                    setDisplayShowTitleEnabled(!showCustomTitle)
                }
            }
        }
    }
}