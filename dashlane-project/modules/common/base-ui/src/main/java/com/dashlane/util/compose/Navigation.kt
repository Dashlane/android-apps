package com.dashlane.util.compose

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

inline fun <reified T : Any> NavHostController.navigateAndPopupToStart(route: T) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
    }
}
