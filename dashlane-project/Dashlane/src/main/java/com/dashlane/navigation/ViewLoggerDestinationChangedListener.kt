package com.dashlane.navigation

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.dashlane.util.usagelogs.ViewLogger
import javax.inject.Inject

class ViewLoggerDestinationChangedListener @Inject constructor(
    private val viewLogger: ViewLogger
) : NavController.OnDestinationChangedListener {
    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        
        viewLogger.log(destination)
    }
}
