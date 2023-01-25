package com.dashlane.navigation

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.dashlane.util.logD
import com.dashlane.util.usagelogs.ViewLogger



class ViewLoggerDestinationChangedListener(
    private val viewLogger: ViewLogger = ViewLogger()
) : NavController.OnDestinationChangedListener {
    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        logD { "Navigated to : ${destination.id} with label: ${destination.label}" }
        
        viewLogger.log(destination)
    }
}