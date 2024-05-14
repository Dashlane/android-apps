package com.dashlane.util.compose

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.core.view.MenuProvider

object BackPressedDispatcherBridge {

    fun getMenuProvider(activity: ComponentActivity) = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            val consumed = if (menuItem.itemId == android.R.id.home) {
                activity.onBackPressedDispatcher.onBackPressed() 
                true
            } else {
                false
            }
            return consumed
        }
    }
}